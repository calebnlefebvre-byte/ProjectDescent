package com.descent.dungeon.bosses;

import net.minecraft.core.BlockPos;

/** A boss fight's bounds — center plus radius, used for leash/reset logic (e.g. a boss that resets if a player flees too far). Not yet consulted by anything; reserved for when a concrete boss needs it. */
public record BossArena(BlockPos center, int radius) {

    public boolean contains(BlockPos pos) {
        return center.distSqr(pos) <= (double) radius * radius;
    }
}
