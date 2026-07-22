package com.descent.dungeon.loot;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Registration/lookup for installed {@link LootTable}s and {@link LootModifier}s.
 * Deliberately separate from {@link LootGenerator} (which decides how a roll
 * is put together) so a future datapack-driven or add-on loot table only
 * ever needs to call {@link #register}, never touch the generation logic.
 */
public final class LootRegistry {

    private static final Map<ResourceLocation, LootTable> TABLES = new HashMap<>();
    private static final List<LootModifier> MODIFIERS = new ArrayList<>();

    private LootRegistry() {
    }

    public static void register(LootTable table) {
        TABLES.put(table.id(), table);
    }

    public static void register(LootModifier modifier) {
        MODIFIERS.add(modifier);
    }

    public static Optional<LootTable> get(ResourceLocation tableId) {
        return Optional.ofNullable(TABLES.get(tableId));
    }

    public static List<LootModifier> modifiers() {
        return List.copyOf(MODIFIERS);
    }
}
