package com.descent.dungeon.api.director;

/**
 * Neutral {@link IDungeonDirector} that observes nothing and adjusts
 * nothing. This is the default installed in {@link com.descent.dungeon.hooks.DungeonHooks}
 * until a real Director is built; it exists so every call site can be wired
 * up now without a null-check.
 */
public final class NullDungeonDirector implements IDungeonDirector {

    public static final NullDungeonDirector INSTANCE = new NullDungeonDirector();

    private NullDungeonDirector() {
    }

    @Override
    public void onFloorStart(DungeonDirectorContext context) {
        // no-op
    }

    @Override
    public void onFloorTick(DungeonDirectorContext context) {
        // no-op
    }

    @Override
    public double difficultyMultiplier(DungeonDirectorContext context) {
        return 1.0;
    }

    @Override
    public boolean shouldTriggerEnvironmentalEvent(DungeonDirectorContext context) {
        return false;
    }

    @Override
    public void onFloorEnd(DungeonDirectorContext context) {
        // no-op
    }
}
