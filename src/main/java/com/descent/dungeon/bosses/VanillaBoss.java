package com.descent.dungeon.bosses;

import com.descent.dungeon.difficulty.DifficultyCalculator;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

import java.util.List;

/**
 * A boss backed by a modified vanilla mob, per the design document ("boss
 * floors initially use modified vanilla mobs... architecture must allow
 * complete replacement with custom bosses later"). Single-phase, no special
 * abilities yet — {@link #phases()} returns one phase with an empty ability
 * list, which is a valid, if minimal, implementation of the framework.
 * {@link #configure} is where "modified" happens: health and damage use
 * {@link DifficultyCalculator}'s boss-specific curve — deliberately gentler
 * per-floor growth than a regular mob's, plus a flat "this is a boss" bonus,
 * rather than the full trash-mob curve on top of a bonus (which compounds
 * into an unkillable Floor 18 boss — see that method's Javadoc for the
 * numbers that caught this during the difficulty tuning pass).
 */
public final class VanillaBoss implements IBoss {

    private final ResourceLocation id;
    private final int floorNumber;
    private final EntityType<? extends Mob> entityType;
    private final Component displayName;
    private final BossLoot loot;

    public VanillaBoss(ResourceLocation id, int floorNumber, EntityType<? extends Mob> entityType, Component displayName, BossLoot loot) {
        this.id = id;
        this.floorNumber = floorNumber;
        this.entityType = entityType;
        this.displayName = displayName;
        this.loot = loot;
    }

    @Override
    public ResourceLocation id() {
        return id;
    }

    @Override
    public int floorNumber() {
        return floorNumber;
    }

    @Override
    public EntityType<? extends Mob> entityType() {
        return entityType;
    }

    @Override
    public List<BossPhase> phases() {
        return List.of(new BossPhase(1.0, List.of()));
    }

    @Override
    public BossLoot loot() {
        return loot;
    }

    @Override
    public void configure(ServerLevel level, Mob entity) {
        entity.setCustomName(displayName);
        entity.setCustomNameVisible(true);
        entity.setPersistenceRequired();
        entity.addTag(bossTag(floorNumber));

        double healthMultiplier = DifficultyCalculator.bossHealthMultiplier(floorNumber);
        double damageMultiplier = DifficultyCalculator.bossDamageMultiplier(floorNumber);

        AttributeInstance maxHealth = entity.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(maxHealth.getBaseValue() * healthMultiplier);
        }
        entity.setHealth(entity.getMaxHealth());

        AttributeInstance attackDamage = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.setBaseValue(attackDamage.getBaseValue() * damageMultiplier);
        }
    }

    /** Shared with {@code events.BossEvents}, which reads this exact tag format to recognize a dying boss and which floor it belonged to. */
    public static String bossTag(int floorNumber) {
        return "descent_boss_" + floorNumber;
    }
}
