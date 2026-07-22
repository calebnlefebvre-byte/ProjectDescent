package com.descent.dungeon.api.director;

/**
 * Extension point for a future adaptive "Dungeon Director": an AI-ish system
 * that would own dynamic encounters, difficulty adjustment, environmental
 * events, spawn manipulation, psychological pressure, and player analysis.
 * <p>
 * Nothing implements this beyond {@link NullDungeonDirector} yet — per the
 * design document, the Director itself is a future system. Gameplay code
 * (timers, collapse, mob spawning, loot) should already call through
 * {@link com.descent.dungeon.hooks.DungeonHooks#director()} wherever the
 * design calls for Director involvement, so that dropping in a real
 * implementation later requires no changes to those call sites.
 */
public interface IDungeonDirector {

    /** Called once when players first enter a floor and its timer starts. */
    void onFloorStart(DungeonDirectorContext context);

    /** Called periodically (roughly once per second of game time) while a floor is active. */
    void onFloorTick(DungeonDirectorContext context);

    /**
     * Additional difficulty multiplier layered on top of the formula-based
     * per-floor scaling in {@link com.descent.dungeon.config.DescentCommonConfig}.
     * A neutral Director returns {@code 1.0}.
     */
    double difficultyMultiplier(DungeonDirectorContext context);

    /**
     * Whether the Director wants to inject an unscheduled environmental event
     * this tick (beyond the deterministic collapse sequence). A neutral
     * Director never does.
     */
    boolean shouldTriggerEnvironmentalEvent(DungeonDirectorContext context);

    /** Called once when a floor is left behind (descent, collapse death, or evacuation). */
    void onFloorEnd(DungeonDirectorContext context);
}
