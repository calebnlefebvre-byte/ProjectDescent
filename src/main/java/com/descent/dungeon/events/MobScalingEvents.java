package com.descent.dungeon.events;

import com.descent.dungeon.difficulty.DifficultyCalculator;
import com.descent.dungeon.generation.DescentDimensions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

import java.util.OptionalInt;

/**
 * Scales every hostile mob's health and attack damage the moment it spawns
 * on a floor, by that floor's {@code difficulty.DifficultyCalculator}
 * multipliers — the "monster health/damage scaling" half of the design
 * document's difficulty requirement. Boss mobs get an additional bonus on
 * top of this same base scaling; see {@code bosses.VanillaBoss#configure}.
 * <p>
 * Monster <em>count</em> scaling (the design document's other axis) isn't
 * hooked up here — vanilla's spawn-cap system doesn't have a simple,
 * low-risk seam to scale per-dimension without touching global spawn
 * internals, so {@code DifficultyCalculator#monsterCountMultiplier} exists
 * and is config-driven but currently has no consumer. Flagged rather than
 * silently dropped.
 */
public final class MobScalingEvents {

    private MobScalingEvents() {
    }

    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        OptionalInt floorNumber = DescentDimensions.floorNumberFromKey(level.dimension());
        if (floorNumber.isEmpty()) {
            return;
        }

        Mob mob = event.getEntity();
        if (!(mob instanceof Monster)) {
            return;
        }

        int floor = floorNumber.getAsInt();
        double healthMultiplier = DifficultyCalculator.monsterHealthMultiplier(floor);
        double damageMultiplier = DifficultyCalculator.monsterDamageMultiplier(floor);

        AttributeInstance maxHealth = mob.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(maxHealth.getBaseValue() * healthMultiplier);
            mob.setHealth(mob.getMaxHealth());
        }

        AttributeInstance attackDamage = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.setBaseValue(attackDamage.getBaseValue() * damageMultiplier);
        }
    }
}
