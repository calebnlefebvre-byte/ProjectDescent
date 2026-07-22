package com.descent.dungeon.events;

import com.descent.dungeon.DescentMod;
import com.descent.dungeon.api.events.CollapseEndedEvent;
import com.descent.dungeon.api.events.CollapseStartedEvent;
import com.descent.dungeon.api.events.TimerExpiredEvent;
import com.descent.dungeon.api.director.DungeonDirectorContext;
import com.descent.dungeon.collapse.CollapseHazards;
import com.descent.dungeon.config.FloorConfigManager;
import com.descent.dungeon.config.FloorDefinition;
import com.descent.dungeon.context.DungeonContext;
import com.descent.dungeon.difficulty.DifficultyCalculator;
import com.descent.dungeon.generation.DescentDimensions;
import com.descent.dungeon.hooks.DungeonHooks;
import com.descent.dungeon.persistence.DescentSavedData;
import com.descent.dungeon.persistence.FloorSaveState;
import com.descent.dungeon.timer.FloorClock;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.List;

/**
 * Drives every started floor's timer/collapse phase forward. Runs on a
 * server-tick listener rather than per-floor scheduled tasks so there is one
 * obvious place that owns "what phase is this floor in right now" — see
 * {@code timer.FloorClock} for the actual phase math, which this class only
 * calls and reacts to.
 * <p>
 * A floor's phase is a pure function of elapsed game time, so nothing needs
 * to persist "we're currently collapsing" — only which phases
 * {@code persistence.FloorSaveState#announcedPhases()} has already reacted
 * to, so each is reacted to exactly once.
 */
public final class FloorTimerEvents {

    /** How often (in server ticks) floor phases are checked. Once a second is plenty for a Minecraft-day-scale timer. */
    private static final long CHECK_INTERVAL_TICKS = 20L;

    private FloorTimerEvents() {
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % CHECK_INTERVAL_TICKS != 0) {
            return;
        }

        DescentSavedData saveData = DescentSavedData.get(server);
        for (int floorNumber = 1; floorNumber <= FloorConfigManager.FLOOR_COUNT; floorNumber++) {
            saveData.getFloor(floorNumber).ifPresent(state -> tickFloor(server, saveData, state));
        }
    }

    private static void tickFloor(MinecraftServer server, DescentSavedData saveData, FloorSaveState state) {
        ServerLevel level = DescentDimensions.getFloorLevel(server, state.floorNumber());
        FloorDefinition definition = FloorConfigManager.get(state.floorNumber());
        FloorClock clock = FloorClock.compute(definition.timerDays(), state.startGameTime(), level.getGameTime());

        DungeonHooks.director().onFloorTick(directorContext(level, state, clock));

        FloorSaveState updated = state;
        updated = announceOnce(level, updated, clock, FloorClock.Phase.WARNING,
                "This floor feels unstable. It won't hold much longer.");
        updated = announceOnce(level, updated, clock, FloorClock.Phase.FINAL_WARNING,
                "The floor is about to give way. Get to a staircase NOW.");
        updated = reactToCollapseStart(level, updated, clock);
        if (clock.isCollapsing()) {
            DungeonContext context = DungeonContext.of(level, definition, clock, server.overworld().getSeed());
            strikeHazards(context, clock);
        }
        updated = announceOnce(level, updated, clock, FloorClock.Phase.FINAL_COLLAPSE,
                "The collapse is reaching its peak!");
        updated = reactToCollapseEnd(level, updated, clock);

        if (updated != state) {
            saveData.putFloor(updated);
        }
    }

    private static DungeonDirectorContext directorContext(ServerLevel level, FloorSaveState state, FloorClock clock) {
        return new DungeonDirectorContext(level, state.floorNumber(), clock.elapsedTicks(), clock.remainingTicksUntilCollapse());
    }

    private static FloorSaveState announceOnce(ServerLevel level, FloorSaveState state, FloorClock clock,
                                                FloorClock.Phase phase, String message) {
        if (clock.phase() != phase || state.hasAnnounced(phase)) {
            return state;
        }
        for (ServerPlayer player : level.players()) {
            player.displayClientMessage(Component.literal(message), false);
        }
        return state.withAnnounced(phase);
    }

    private static FloorSaveState reactToCollapseStart(ServerLevel level, FloorSaveState state, FloorClock clock) {
        if (clock.phase() != FloorClock.Phase.COLLAPSING || state.hasAnnounced(FloorClock.Phase.COLLAPSING)) {
            return state;
        }
        for (ServerPlayer player : level.players()) {
            player.displayClientMessage(Component.literal("The floor is collapsing! Find a staircase — now."), false);
        }
        DescentMod.LOGGER.info("Floor {}: collapse begun", state.floorNumber());
        NeoForge.EVENT_BUS.post(new TimerExpiredEvent(level, state.floorNumber()));
        NeoForge.EVENT_BUS.post(new CollapseStartedEvent(level, state.floorNumber()));
        return state.withAnnounced(FloorClock.Phase.COLLAPSING);
    }

    private static void strikeHazards(DungeonContext context, FloorClock clock) {
        ServerLevel level = context.level();
        double finalCollapseBoost = clock.phase() == FloorClock.Phase.FINAL_COLLAPSE ? 1.5 : 1.0;
        double intensity = DifficultyCalculator.collapseIntensityMultiplier(context.floorNumber()) * finalCollapseBoost
                * (0.4 + 0.6 * clock.collapseProgress());
        for (ServerPlayer player : level.players()) {
            if (level.getRandom().nextDouble() < intensity * 0.5) {
                CollapseHazards.strike(context, player, level.getRandom());
            }
        }
    }

    private static FloorSaveState reactToCollapseEnd(ServerLevel level, FloorSaveState state, FloorClock clock) {
        if (clock.phase() != FloorClock.Phase.ENDED || state.hasAnnounced(FloorClock.Phase.ENDED)) {
            return state;
        }
        List<ServerPlayer> caught = List.copyOf(level.players());
        for (ServerPlayer player : caught) {
            player.displayClientMessage(Component.literal("The floor gives way beneath you."), false);
            player.kill(level);
        }
        DescentMod.LOGGER.info("Floor {}: collapse ended, {} player(s) caught inside", state.floorNumber(), caught.size());
        DungeonHooks.director().onFloorEnd(directorContext(level, state, clock));
        NeoForge.EVENT_BUS.post(new CollapseEndedEvent(level, state.floorNumber(), caught));
        return state.withAnnounced(FloorClock.Phase.ENDED);
    }
}
