package com.descent.dungeon.events;

import com.descent.dungeon.DescentMod;
import com.descent.dungeon.api.events.FloorDiscoveredEvent;
import com.descent.dungeon.api.events.PlayerDescendedFloorEvent;
import com.descent.dungeon.api.events.PlayerVictoryEvent;
import com.descent.dungeon.api.events.TributeTakenEvent;
import com.descent.dungeon.config.DescentCommonConfig;
import com.descent.dungeon.config.FloorConfigManager;
import com.descent.dungeon.config.FloorDefinition;
import com.descent.dungeon.generation.DescentDimensions;
import com.descent.dungeon.persistence.DescentSavedData;
import com.descent.dungeon.persistence.FloorSaveState;
import com.descent.dungeon.persistence.StaircaseRecord;
import com.descent.dungeon.stairs.DungeonTribute;
import com.descent.dungeon.stairs.StaircasePlacer;
import com.descent.dungeon.timer.FloorClock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

/**
 * Staircase discovery and the descend/Early Descent Tribute/victory flow —
 * the two gameplay-facing halves of Phase 3's stair system.
 * <p>
 * Discovery is a periodic proximity check (piggybacking on the same tick
 * cadence as {@link FloorTimerEvents}, but kept in its own listener since it
 * reacts to players, not floors): any online player within a few blocks of
 * an undiscovered staircase's entrance flips it to discovered, permanently
 * and world-wide, and upgrades its marker blocks so it reads as "found" from
 * then on.
 * <p>
 * Descending is a deliberate action — right-clicking a staircase's landing
 * marker — rather than a proximity trigger, so it can never happen by
 * accident. What that click does depends on {@code timer.FloorClock}: during
 * {@link FloorClock.Phase#ACTIVE} it costs an Early Descent Tribute; once
 * the timer has expired it's free. Floor 18's single staircase triggers
 * victory instead of moving to a Floor 19 that does not exist.
 */
public final class StaircaseEvents {

    private static final long CHECK_INTERVAL_TICKS = 20L;
    private static final double DISCOVERY_RADIUS_BLOCKS = 5.0;

    private StaircaseEvents() {
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % CHECK_INTERVAL_TICKS != 0) {
            return;
        }
        DescentSavedData saveData = DescentSavedData.get(server);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            checkDiscovery(server, saveData, player);
        }
    }

    private static void checkDiscovery(MinecraftServer server, DescentSavedData saveData, ServerPlayer player) {
        OptionalInt floorNumber = DescentDimensions.floorNumberFromKey(player.level().dimension());
        if (floorNumber.isEmpty()) {
            return;
        }
        int floor = floorNumber.getAsInt();
        Optional<FloorSaveState> maybeState = saveData.getFloor(floor);
        if (maybeState.isEmpty()) {
            return;
        }
        FloorSaveState state = maybeState.get();
        List<StaircaseRecord> staircases = state.staircases();

        for (int i = 0; i < staircases.size(); i++) {
            StaircaseRecord staircase = staircases.get(i);
            if (staircase.discovered()) {
                continue;
            }
            double distSqr = player.blockPosition().distSqr(staircase.position());
            if (distSqr <= DISCOVERY_RADIUS_BLOCKS * DISCOVERY_RADIUS_BLOCKS) {
                ServerLevel level = DescentDimensions.getFloorLevel(server, floor);
                StaircasePlacer.markDiscovered(level, staircase.position());
                saveData.putFloor(state.withStaircaseAt(i, staircase.withDiscovered()));

                player.displayClientMessage(Component.literal("Staircase discovered! Its location is marked forever.")
                        .withStyle(ChatFormatting.AQUA), false);
                level.playSound(null, staircase.position(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.2F);
                DescentMod.LOGGER.info("Floor {}: staircase at {} discovered by {}", floor, staircase.position(), player.getGameProfile().getName());
                NeoForge.EVENT_BUS.post(new FloorDiscoveredEvent(player, floor, staircase.position()));
                return; // saveData/state are now stale; re-check the rest next tick
            }
        }
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        OptionalInt floorNumber = DescentDimensions.floorNumberFromKey(player.level().dimension());
        if (floorNumber.isEmpty()) {
            return;
        }
        int floor = floorNumber.getAsInt();

        DescentSavedData saveData = DescentSavedData.get(server);
        Optional<FloorSaveState> maybeState = saveData.getFloor(floor);
        if (maybeState.isEmpty()) {
            return;
        }
        FloorSaveState state = maybeState.get();

        BlockPos clicked = event.getPos();
        StaircaseRecord staircase = null;
        for (StaircaseRecord candidate : state.staircases()) {
            if (candidate.landingPosition().equals(clicked)) {
                staircase = candidate;
                break;
            }
        }
        if (staircase == null) {
            return;
        }
        event.setCanceled(true);

        ServerLevel level = DescentDimensions.getFloorLevel(server, floor);
        FloorDefinition definition = FloorConfigManager.get(floor);
        FloorClock clock = FloorClock.compute(definition.timerDays(), state.startGameTime(), level.getGameTime());

        if (clock.isEarlyDescent() && DescentCommonConfig.tributeEnabled) {
            applyTribute(player, floor);
        }

        if (staircase.finalStaircase()) {
            triggerVictory(server, saveData, player);
        } else {
            descendToNextFloor(server, player, floor);
        }
    }

    private static void applyTribute(ServerPlayer player, int floorNumber) {
        Optional<ItemStack> destroyed = DungeonTribute.apply(player);
        if (destroyed.isPresent()) {
            player.displayClientMessage(Component.literal("Dungeon Tribute claims your ")
                    .append(destroyed.get().getHoverName())
                    .append(Component.literal(". It is gone forever.")).withStyle(ChatFormatting.DARK_RED), false);
            NeoForge.EVENT_BUS.post(new TributeTakenEvent(player, floorNumber, destroyed.get()));
        } else {
            player.displayClientMessage(Component.literal("Dungeon Tribute finds nothing worth taking.")
                    .withStyle(ChatFormatting.GRAY), false);
        }
    }

    private static void descendToNextFloor(MinecraftServer server, ServerPlayer player, int currentFloor) {
        int nextFloor = currentFloor + 1;
        ServerLevel nextLevel = DescentDimensions.getFloorLevel(server, nextFloor);
        BlockPos entry = DescentDimensions.findSafeEntryPosition(nextLevel);
        player.teleportTo(nextLevel, entry.getX() + 0.5, entry.getY(), entry.getZ() + 0.5, Set.of(), player.getYRot(), player.getXRot(), false);
        player.displayClientMessage(Component.literal("You descend to Floor " + nextFloor + "."), false);
        NeoForge.EVENT_BUS.post(new PlayerDescendedFloorEvent(player, currentFloor, nextFloor));
    }

    private static void triggerVictory(MinecraftServer server, DescentSavedData saveData, ServerPlayer player) {
        saveData.setDungeonCompleted(true);
        ServerLevel overworld = server.overworld();
        BlockPos spawn = overworld.getSharedSpawnPos();

        if (DescentCommonConfig.returnToOverworldOnVictory) {
            player.teleportTo(overworld, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, Set.of(), player.getYRot(), player.getXRot(), false);
        }
        player.displayClientMessage(Component.literal("=== YOU HAVE ESCAPED THE DUNGEON ===")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
        DescentMod.LOGGER.info("{} completed the dungeon", player.getGameProfile().getName());
        NeoForge.EVENT_BUS.post(new PlayerVictoryEvent(player));
    }
}
