package com.descent.dungeon.bosses;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** What a boss drops on defeat. Deliberately separate from {@code loot.LootGenerator} — a boss's drop table is hand-authored, not procedurally scaled like a room's. */
public interface BossLoot {

    List<ItemStack> generateLoot(ServerLevel level, RandomSource random);
}
