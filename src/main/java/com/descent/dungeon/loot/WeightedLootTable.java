package com.descent.dungeon.loot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** The one concrete {@link LootTable}: a flat weighted pool of {@link LootEntry} rolled independently {@code rolls} times. Good enough for every tier so far — a table needing structure beyond "N independent weighted rolls" can implement {@link LootTable} directly instead. */
public final class WeightedLootTable implements LootTable {

    private final ResourceLocation id;
    private final LootTier tier;
    private final List<LootEntry> entries;
    private final int totalWeight;

    public WeightedLootTable(ResourceLocation id, LootTier tier, List<LootEntry> entries) {
        this.id = id;
        this.tier = tier;
        this.entries = List.copyOf(entries);
        this.totalWeight = this.entries.stream().mapToInt(LootEntry::weight).sum();
    }

    @Override
    public ResourceLocation id() {
        return id;
    }

    @Override
    public LootTier tier() {
        return tier;
    }

    @Override
    public List<ItemStack> roll(RandomSource random, int rolls) {
        List<ItemStack> result = new ArrayList<>(rolls);
        for (int i = 0; i < rolls; i++) {
            ItemStack stack = rollOne(random);
            if (!stack.isEmpty()) {
                result.add(stack);
            }
        }
        return result;
    }

    private ItemStack rollOne(RandomSource random) {
        if (totalWeight <= 0) {
            return ItemStack.EMPTY;
        }
        int roll = random.nextInt(totalWeight);
        int cursor = 0;
        for (LootEntry entry : entries) {
            cursor += entry.weight();
            if (roll < cursor) {
                return entry.factory().get();
            }
        }
        return ItemStack.EMPTY;
    }
}
