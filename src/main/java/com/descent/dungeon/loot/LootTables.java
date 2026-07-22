package com.descent.dungeon.loot;

import com.descent.dungeon.DescentMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * The five tiered {@link WeightedLootTable}s content actually rolls from —
 * registered once at mod startup via {@link #bootstrap()}. Real vanilla
 * items, scaled in power from COMMON to LEGENDARY; no enchanted stacks yet
 * (applying random enchantments at runtime is its own bit of
 * version-sensitive API this pass didn't need to take on — plain items
 * already give a real sense of progression floor-to-floor).
 */
public final class LootTables {

    public static final ResourceLocation COMMON = id("common");
    public static final ResourceLocation UNCOMMON = id("uncommon");
    public static final ResourceLocation RARE = id("rare");
    public static final ResourceLocation EPIC = id("epic");
    public static final ResourceLocation LEGENDARY = id("legendary");

    private LootTables() {
    }

    /** Maps a {@link LootTier} to the table that rolls it — the bridge between {@code difficulty.DifficultyCalculator#lootTierForFloor} and an actual table id. */
    public static ResourceLocation forTier(LootTier tier) {
        return switch (tier) {
            case COMMON -> COMMON;
            case UNCOMMON -> UNCOMMON;
            case RARE -> RARE;
            case EPIC -> EPIC;
            case LEGENDARY -> LEGENDARY;
        };
    }

    public static void bootstrap() {
        LootRegistry.register(new WeightedLootTable(COMMON, LootTier.COMMON, List.of(
                LootEntry.of(Items.BREAD, 3, 20),
                LootEntry.of(Items.APPLE, 2, 15),
                LootEntry.of(Items.IRON_INGOT, 2, 10),
                LootEntry.of(Items.TORCH, 8, 15),
                LootEntry.of(Items.ARROW, 12, 15),
                LootEntry.of(Items.LEATHER, 3, 10),
                LootEntry.of(Items.COAL, 4, 15)
        )));

        LootRegistry.register(new WeightedLootTable(UNCOMMON, LootTier.UNCOMMON, List.of(
                LootEntry.of(Items.IRON_SWORD, 1, 10),
                LootEntry.of(Items.IRON_CHESTPLATE, 1, 8),
                LootEntry.of(Items.IRON_INGOT, 3, 15),
                LootEntry.of(Items.GOLD_INGOT, 2, 12),
                LootEntry.of(Items.GOLDEN_APPLE, 1, 10),
                LootEntry.of(Items.SHIELD, 1, 8),
                LootEntry.of(Items.EXPERIENCE_BOTTLE, 4, 12)
        )));

        LootRegistry.register(new WeightedLootTable(RARE, LootTier.RARE, List.of(
                LootEntry.of(Items.DIAMOND, 1, 10),
                LootEntry.of(Items.DIAMOND_SWORD, 1, 6),
                LootEntry.of(Items.DIAMOND_CHESTPLATE, 1, 5),
                LootEntry.of(Items.ENDER_PEARL, 2, 10),
                LootEntry.of(Items.GOLDEN_APPLE, 1, 8),
                LootEntry.of(Items.EXPERIENCE_BOTTLE, 6, 12)
        )));

        LootRegistry.register(new WeightedLootTable(EPIC, LootTier.EPIC, List.of(
                LootEntry.of(Items.DIAMOND, 3, 12),
                LootEntry.of(Items.NETHERITE_SCRAP, 1, 8),
                LootEntry.of(Items.DIAMOND_PICKAXE, 1, 6),
                LootEntry.of(Items.DIAMOND_CHESTPLATE, 1, 6),
                LootEntry.of(Items.TOTEM_OF_UNDYING, 1, 4),
                LootEntry.of(Items.ENCHANTED_GOLDEN_APPLE, 1, 4),
                LootEntry.of(Items.EXPERIENCE_BOTTLE, 8, 10)
        )));

        LootRegistry.register(new WeightedLootTable(LEGENDARY, LootTier.LEGENDARY, List.of(
                LootEntry.of(Items.NETHERITE_INGOT, 1, 10),
                LootEntry.of(Items.NETHERITE_SWORD, 1, 6),
                LootEntry.of(Items.NETHERITE_CHESTPLATE, 1, 5),
                LootEntry.of(Items.TOTEM_OF_UNDYING, 1, 8),
                LootEntry.of(Items.ENCHANTED_GOLDEN_APPLE, 1, 6),
                LootEntry.of(Items.ELYTRA, 1, 3)
        )));

        DescentMod.LOGGER.info("Registered {} tiered loot tables", LootTier.values().length);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(DescentMod.MODID, "loot/" + path);
    }
}
