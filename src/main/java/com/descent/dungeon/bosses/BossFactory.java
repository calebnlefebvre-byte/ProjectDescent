package com.descent.dungeon.bosses;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;

import java.util.Optional;

/**
 * Turns an {@link IBoss} definition into an actual, configured, spawned
 * {@link Mob} entity. Separated from {@link BossSpawnController} so "how do
 * I build this boss's entity" can change (e.g. to support multi-part bosses,
 * or arena setup before the entity appears) without touching "when should a
 * boss spawn" logic.
 */
public final class BossFactory {

    private BossFactory() {
    }

    /** Creates, positions, configures, and spawns {@code boss}'s entity at {@code position}. Returns the spawned entity, or empty if the entity type failed to instantiate. */
    public static Optional<Mob> spawn(ServerLevel level, IBoss boss, BlockPos position) {
        Mob entity = boss.entityType().create(level, EntitySpawnReason.EVENT);
        if (entity == null) {
            return Optional.empty();
        }
        entity.moveTo(position.getX() + 0.5, position.getY(), position.getZ() + 0.5, 0.0F, 0.0F);
        boss.configure(level, entity);
        level.addFreshEntity(entity);
        return Optional.of(entity);
    }
}
