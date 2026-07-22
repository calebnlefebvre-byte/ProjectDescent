package com.descent.dungeon.loot;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

import java.util.ArrayList;
import java.util.List;

/** Places a chest at a position and fills it with a rolled loot list, scattered across random slots rather than packed from slot 0. Not called from anywhere yet — no floor content places treasure rooms until loot tables exist. */
public final class ChestGenerator {

    private static final int CHEST_SLOTS = 27;

    private ChestGenerator() {
    }

    public static void placeChest(ServerLevel level, BlockPos pos, List<ItemStack> loot, RandomSource random) {
        level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 3);
        if (!(level.getBlockEntity(pos) instanceof ChestBlockEntity chest)) {
            return;
        }

        List<Integer> slots = new ArrayList<>(CHEST_SLOTS);
        for (int i = 0; i < CHEST_SLOTS; i++) {
            slots.add(i);
        }
        for (ItemStack stack : loot) {
            if (slots.isEmpty() || stack.isEmpty()) {
                continue;
            }
            int slot = slots.remove(random.nextInt(slots.size()));
            chest.setItem(slot, stack);
        }
    }
}
