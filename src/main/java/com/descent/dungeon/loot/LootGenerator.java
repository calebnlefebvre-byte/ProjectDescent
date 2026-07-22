package com.descent.dungeon.loot;

import com.descent.dungeon.difficulty.DifficultyCalculator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Rolls a registered {@link LootTable} (see {@link LootRegistry}) and runs
 * the result through every registered {@link LootModifier}. Loot quality is
 * meant to come from here rather than each call site picking a tier ad hoc —
 * see {@link DifficultyCalculator#lootTierForFloor}.
 */
public final class LootGenerator {

    private LootGenerator() {
    }

    /** Rolls {@code tableId} (if registered) and runs the result through every installed {@link LootModifier} in registration order. */
    public static List<ItemStack> generate(ResourceLocation tableId, int rolls, int floorNumber, RandomSource random) {
        List<ItemStack> loot = new ArrayList<>(LootRegistry.get(tableId)
                .map(table -> table.roll(random, rolls))
                .orElseGet(List::of));
        for (LootModifier modifier : LootRegistry.modifiers()) {
            loot = modifier.modify(loot, floorNumber, random);
        }
        return loot;
    }
}
