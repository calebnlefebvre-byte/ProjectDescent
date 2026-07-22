package com.descent.dungeon.loot;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.function.Supplier;

/** One weighted possibility in a {@link WeightedLootTable}: a way to produce a stack, and how often it should come up relative to the table's other entries. */
public record LootEntry(Supplier<ItemStack> factory, int weight) {

    public static LootEntry of(ItemLike item, int count, int weight) {
        return new LootEntry(() -> new ItemStack(item, count), weight);
    }
}
