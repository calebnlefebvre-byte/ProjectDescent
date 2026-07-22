package com.descent.dungeon.loot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * A named, tiered pool of possible drops. Reserved extension point — no
 * concrete loot tables exist yet; {@code LootGenerator} is where they'll be
 * looked up and rolled once floor loot is actually built. Deliberately not
 * vanilla's own {@code net.minecraft.world.level.storage.loot.LootTable}:
 * this project's loot needs to scale continuously with floor number (per
 * {@code DescentCommonConfig#resourceScarcityGrowthPerFloor}), which doesn't
 * map cleanly onto vanilla's static datapack loot table format.
 */
public interface LootTable {

    ResourceLocation id();

    LootTier tier();

    List<ItemStack> roll(RandomSource random, int rolls);
}
