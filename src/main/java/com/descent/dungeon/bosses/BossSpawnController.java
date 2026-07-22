package com.descent.dungeon.bosses;

import com.descent.dungeon.config.FloorDefinition;
import com.descent.dungeon.generation.DescentDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * Decides *when* a floor's boss should spawn and delegates the *how* to
 * {@link BossFactory}, looking the boss up via {@link BossRegistry}. Empty
 * today — per the design review, this framework is built before any
 * concrete boss plugs into it, so {@link #trySpawnBoss} is currently a
 * no-op for every floor. {@code events.FloorGenerationEvents} already calls
 * it for every boss floor (see {@link FloorDefinition#bossFloor()}), so
 * registering the first real boss later needs no change to the generation
 * flow — only a {@link BossRegistry#register} call.
 */
public final class BossSpawnController {

    private BossSpawnController() {
    }

    /** Spawns {@code definition}'s boss if one is registered and its floor's boss hasn't already been defeated. A no-op if not a boss floor or no boss is registered for it yet. */
    public static void trySpawnBoss(ServerLevel level, FloorDefinition definition, boolean alreadyDefeated) {
        if (!definition.bossFloor() || alreadyDefeated) {
            return;
        }
        BossRegistry.forFloor(definition.floorNumber()).ifPresent(boss -> {
            BlockPos spawnPos = DescentDimensions.findSafeEntryPosition(level);
            BossFactory.spawn(level, boss, spawnPos);
        });
    }
}
