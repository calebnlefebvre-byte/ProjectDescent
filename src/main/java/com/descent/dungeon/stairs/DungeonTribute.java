package com.descent.dungeon.stairs;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The Early Descent Tribute: destroys one random equipment item from a
 * player's inventory, weighted by {@link TributeWeightRegistry}, when they
 * take a staircase before their floor's timer has expired. Per the design
 * document, the destroyed item is gone entirely — no drop, no recovery.
 */
public final class DungeonTribute {

    private DungeonTribute() {
    }

    /** Destroys one weighted-random equipment item from {@code player}'s inventory; empty if they carry nothing eligible. */
    public static Optional<ItemStack> apply(ServerPlayer player) {
        Inventory inventory = player.getInventory();

        List<ItemStack> pool = new ArrayList<>();
        List<Integer> weights = new ArrayList<>();
        List<Runnable> removers = new ArrayList<>();
        collectFrom(inventory.items, pool, weights, removers);
        collectFrom(inventory.armor, pool, weights, removers);
        collectFrom(inventory.offhand, pool, weights, removers);

        int totalWeight = weights.stream().mapToInt(Integer::intValue).sum();
        if (totalWeight <= 0) {
            return Optional.empty();
        }

        int roll = player.getRandom().nextInt(totalWeight);
        int cursor = 0;
        for (int i = 0; i < pool.size(); i++) {
            cursor += weights.get(i);
            if (roll < cursor) {
                ItemStack destroyed = pool.get(i).copy();
                removers.get(i).run();
                return Optional.of(destroyed);
            }
        }
        return Optional.empty();
    }

    private static void collectFrom(List<ItemStack> slots, List<ItemStack> pool, List<Integer> weights, List<Runnable> removers) {
        for (int i = 0; i < slots.size(); i++) {
            ItemStack stack = slots.get(i);
            if (stack.isEmpty()) {
                continue;
            }
            int weight = TributeWeightRegistry.getWeight(stack);
            if (weight <= 0) {
                continue;
            }
            int index = i;
            pool.add(stack);
            weights.add(weight);
            removers.add(() -> slots.set(index, ItemStack.EMPTY));
        }
    }
}
