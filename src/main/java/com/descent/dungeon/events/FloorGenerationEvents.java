package com.descent.dungeon.events;

import com.descent.dungeon.DescentMod;
import com.descent.dungeon.api.director.DungeonDirectorContext;
import com.descent.dungeon.bosses.BossSpawnController;
import com.descent.dungeon.config.DescentCommonConfig;
import com.descent.dungeon.config.FloorConfigManager;
import com.descent.dungeon.config.FloorDefinition;
import com.descent.dungeon.context.DungeonContext;
import com.descent.dungeon.generation.DescentDimensions;
import com.descent.dungeon.generation.FloorModifierRegistry;
import com.descent.dungeon.generation.TownFurnisher;
import com.descent.dungeon.hooks.DungeonHooks;
import com.descent.dungeon.loot.TreasureChestPlacer;
import com.descent.dungeon.persistence.DescentSavedData;
import com.descent.dungeon.persistence.FloorSaveState;
import com.descent.dungeon.persistence.StaircaseRecord;
import com.descent.dungeon.stairs.StaircasePlacer;
import com.descent.dungeon.timer.FloorClock;
import com.descent.dungeon.util.DeterministicSeed;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.List;
import java.util.OptionalInt;

/**
 * Bridges the game loop to floor generation: the first time any player sets
 * foot on a floor, this places that floor's staircases if they have not been
 * placed yet, then runs the "apply floor modifiers" step
 * ({@code generation.FloorModifierRegistry}) before the floor is considered
 * finalized. Placement is idempotent (guarded by
 * {@link FloorSaveState#staircasesPlaced()}), so re-entering an
 * already-generated floor is a no-op here.
 */
public final class FloorGenerationEvents {

    private FloorGenerationEvents() {
    }

    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        OptionalInt floorNumber = DescentDimensions.floorNumberFromKey(event.getTo());
        if (floorNumber.isEmpty()) {
            return;
        }

        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }

        int floor = floorNumber.getAsInt();
        DescentSavedData saveData = DescentSavedData.get(server);
        saveData.setPlayerFloor(player.getUUID(), floor);
        ensureStaircasesPlaced(server, saveData, floor);
    }

    private static void ensureStaircasesPlaced(MinecraftServer server, DescentSavedData saveData, int floorNumber) {
        ServerLevel floorLevel = DescentDimensions.getFloorLevel(server, floorNumber);
        long seed = DeterministicSeed.deriveFloorSeed(server.overworld().getSeed(), floorNumber);
        FloorSaveState state = saveData.getFloor(floorNumber)
                .orElseGet(() -> FloorSaveState.empty(floorNumber, seed, floorLevel.getGameTime()));
        if (state.staircasesPlaced()) {
            return;
        }

        FloorDefinition definition = FloorConfigManager.get(floorNumber);

        // Generate floor -> apply floor modifiers -> finalize floor.
        List<StaircaseRecord> staircases = StaircasePlacer.placeStaircases(floorLevel, floorNumber, state.seed(), definition.stairCount());
        TreasureChestPlacer.placeChests(floorLevel, floorNumber, state.seed(), DescentCommonConfig.chestsPerFloor);
        if (definition.townFloor()) {
            TownFurnisher.furnish(floorLevel, floorNumber, state.seed());
        }
        FloorClock freshClock = FloorClock.compute(definition.timerDays(), floorLevel.getGameTime(), floorLevel.getGameTime());
        DungeonContext context = DungeonContext.of(floorLevel, definition, freshClock, seed);
        FloorModifierRegistry.applyModifiers(context);
        BossSpawnController.trySpawnBoss(floorLevel, definition, state.bossDefeated());
        saveData.putFloor(state.withStaircases(staircases));

        DescentMod.LOGGER.info("Floor {}: placed {} staircase(s)", floorNumber, staircases.size());
        DungeonHooks.director().onFloorStart(new DungeonDirectorContext(floorLevel, floorNumber, 0L,
                definition.timerDays() * FloorClock.TICKS_PER_DAY));
    }
}
