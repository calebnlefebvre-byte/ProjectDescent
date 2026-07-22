/**
 * The single shared difficulty-scaling service — {@link com.descent.dungeon.difficulty.DifficultyCalculator}
 * — every gameplay system queries instead of recomputing
 * {@code (1 + growthRate) ^ (floorNumber - 1)} against
 * {@code config.DescentCommonConfig} independently. Added in the
 * pre-Phase-4 design review specifically to guarantee consistent difficulty
 * progression across mob scaling, loot quality, collapse intensity, and
 * whatever Phase 4/5 systems come next.
 */
package com.descent.dungeon.difficulty;
