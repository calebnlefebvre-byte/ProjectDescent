package com.descent.dungeon.loot;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Post-processes a rolled loot list — quantity/quality adjustments, floor-scaling boosts, a future "double loot" {@code IFloorModifier} hooking in here, etc. Reserved; nothing implements it yet. */
public interface LootModifier {

    List<ItemStack> modify(List<ItemStack> loot, int floorNumber, RandomSource random);
}
