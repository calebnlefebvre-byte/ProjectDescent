package com.descent.dungeon.loot;

/** Coarse loot-quality bucket a {@code LootTable} rolls within; how a floor number maps to a tier is {@code LootGenerator}'s job, not this enum's. */
public enum LootTier {
    COMMON,
    UNCOMMON,
    RARE,
    EPIC,
    LEGENDARY
}
