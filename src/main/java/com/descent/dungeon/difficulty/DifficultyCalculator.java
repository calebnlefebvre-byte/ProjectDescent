package com.descent.dungeon.difficulty;

import com.descent.dungeon.config.DescentCommonConfig;
import com.descent.dungeon.loot.LootTier;

/**
 * The single place every gameplay system asks "how hard should floor N be."
 * Per the design review, mob scaling, loot quality, boss scaling, tribute
 * scaling, and collapse intensity all query this rather than each
 * reimplementing {@code (1 + growthRate) ^ (floorNumber - 1)} against
 * {@code DescentCommonConfig} directly — one formula per axis, read from
 * config, applied consistently everywhere.
 * <p>
 * This does not fold in {@code api.director.IDungeonDirector#difficultyMultiplier},
 * on purpose: that hook takes a live {@code DungeonDirectorContext} (a
 * level, elapsed/remaining ticks) that most callers here don't have handy,
 * and this class only needs a floor number. A caller that <em>does</em> have
 * a live context (currently only {@code events.FloorTimerEvents}) should
 * multiply this class's result by the Director's on top, when a real
 * Director exists to return anything other than {@code 1.0}.
 */
public final class DifficultyCalculator {

    private DifficultyCalculator() {
    }

    public static double monsterHealthMultiplier(int floorNumber) {
        return growth(DescentCommonConfig.monsterHealthGrowthPerFloor, floorNumber);
    }

    public static double monsterDamageMultiplier(int floorNumber) {
        return growth(DescentCommonConfig.monsterDamageGrowthPerFloor, floorNumber);
    }

    public static double monsterCountMultiplier(int floorNumber) {
        return growth(DescentCommonConfig.monsterCountGrowthPerFloor, floorNumber);
    }

    /**
     * Health multiplier for a boss mob specifically — deliberately gentler
     * per-floor growth (half the normal monster rate) than
     * {@link #monsterHealthMultiplier}, because a boss is already a tanky,
     * hand-picked mob rather than a trash spawn. Applying the full trash-mob
     * curve on top of a flat boss bonus compounds into absurd numbers by
     * Floor 18 (a sanity check during the difficulty tuning pass found a
     * ~34x-base-health Warden under the naive formula); this is that fix.
     */
    public static double bossHealthMultiplier(int floorNumber) {
        return growth(DescentCommonConfig.monsterHealthGrowthPerFloor * 0.5, floorNumber) * DescentCommonConfig.bossHealthBonus;
    }

    /** See {@link #bossHealthMultiplier} — same reasoning, applied to boss attack damage. */
    public static double bossDamageMultiplier(int floorNumber) {
        return growth(DescentCommonConfig.monsterDamageGrowthPerFloor * 0.5, floorNumber) * DescentCommonConfig.bossDamageBonus;
    }

    public static double trapDensityMultiplier(int floorNumber) {
        return growth(DescentCommonConfig.trapDensityGrowthPerFloor, floorNumber);
    }

    public static double resourceScarcityMultiplier(int floorNumber) {
        return growth(DescentCommonConfig.resourceScarcityGrowthPerFloor, floorNumber);
    }

    /** Global collapse hazard intensity, scaled up on later floors by the same trap-density growth rate environmental hazards use. */
    public static double collapseIntensityMultiplier(int floorNumber) {
        return DescentCommonConfig.collapseHazardIntensity * trapDensityMultiplier(floorNumber);
    }

    /** Buckets a floor number into a loot rarity tier. A simple, adjustable default — not meant to be the last word on loot balance. */
    public static LootTier lootTierForFloor(int floorNumber) {
        if (floorNumber <= 3) {
            return LootTier.COMMON;
        } else if (floorNumber <= 7) {
            return LootTier.UNCOMMON;
        } else if (floorNumber <= 11) {
            return LootTier.RARE;
        } else if (floorNumber <= 15) {
            return LootTier.EPIC;
        }
        return LootTier.LEGENDARY;
    }

    private static double growth(double growthPerFloor, int floorNumber) {
        return Math.pow(1.0 + growthPerFloor, Math.max(0, floorNumber - 1));
    }
}
