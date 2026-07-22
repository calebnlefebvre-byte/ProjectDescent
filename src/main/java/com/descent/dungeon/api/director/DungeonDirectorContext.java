package com.descent.dungeon.api.director;

import net.minecraft.server.level.ServerLevel;

/**
 * Snapshot of state passed to {@link IDungeonDirector} hooks. Kept minimal
 * and additive on purpose: the real Dungeon Director does not exist yet, so
 * this context should grow only when a concrete caller needs a new field,
 * not speculatively.
 *
 * @param level               the floor dimension this hook concerns
 * @param floorNumber         1..{@link com.descent.dungeon.config.FloorConfigManager#FLOOR_COUNT}
 * @param floorElapsedTicks   ticks since the floor's timer started
 * @param floorRemainingTicks ticks remaining before collapse begins
 */
public record DungeonDirectorContext(
        ServerLevel level,
        int floorNumber,
        long floorElapsedTicks,
        long floorRemainingTicks
) {
}
