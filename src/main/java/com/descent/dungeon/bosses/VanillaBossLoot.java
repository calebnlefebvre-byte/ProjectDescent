package com.descent.dungeon.bosses;

import com.descent.dungeon.loot.LootGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Rolls a boss's drops from one of the shared tiered loot tables (see {@code loot.LootTables}) rather than a hand-authored table — good enough until a boss needs a guaranteed unique drop. */
public record VanillaBossLoot(ResourceLocation tableId, int rolls, int floorNumber) implements BossLoot {

    @Override
    public List<ItemStack> generateLoot(ServerLevel level, RandomSource random) {
        return LootGenerator.generate(tableId, rolls, floorNumber, random);
    }
}
