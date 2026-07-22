package com.descent.dungeon.bosses;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntityType;

import java.util.List;

/**
 * A boss encounter pluggable into {@link BossSpawnController}. Per the
 * design document, boss floors (3/6/9/12/15/18) initially use modified
 * vanilla mobs — {@link #entityType()} names which vanilla mob backs this
 * boss, and {@link #configure} is where health/damage/equipment tweaks turn
 * it into something that reads as "the boss of this floor" rather than a
 * stray hostile. Nothing implements this interface yet: per the pre-Phase-4
 * design review, this framework is built before any content plugs into it.
 */
public interface IBoss {

    ResourceLocation id();

    int floorNumber();

    EntityType<? extends Mob> entityType();

    /** Ordered by descending {@link BossPhase#healthFractionThreshold()} — phase 0 is the fight's opening state. */
    List<BossPhase> phases();

    BossLoot loot();

    /** Applies this boss's health/damage/equipment tweaks to a freshly spawned entity, before it's presented to players. */
    void configure(ServerLevel level, Mob entity);
}
