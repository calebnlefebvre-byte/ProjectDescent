/**
 * Procedural, scaling loot: rarity and quality both grow with floor number
 * via {@link com.descent.dungeon.difficulty.DifficultyCalculator#lootTierForFloor}.
 * The framework is built — {@link com.descent.dungeon.loot.LootTier},
 * {@link com.descent.dungeon.loot.LootTable}, {@link com.descent.dungeon.loot.LootModifier},
 * {@link com.descent.dungeon.loot.LootRegistry} (registration/lookup),
 * {@link com.descent.dungeon.loot.LootGenerator} (rolls + applies modifiers),
 * {@link com.descent.dungeon.loot.ChestGenerator}, and the reserved
 * {@link com.descent.dungeon.loot.ArtifactPlaceholder} seam for future
 * Artifact equipment — but no concrete loot table is registered yet and
 * nothing places a chest. (Tribute-weight resolution for vanilla equipment
 * lives in {@code stairs.TributeWeightRegistry}, not here — that's a
 * different concern from floor loot generation.)
 */
package com.descent.dungeon.loot;
