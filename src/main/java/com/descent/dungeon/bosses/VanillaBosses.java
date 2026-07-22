package com.descent.dungeon.bosses;

import com.descent.dungeon.DescentMod;
import com.descent.dungeon.loot.LootTables;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

/**
 * The six {@link VanillaBoss}es registered at startup, one per boss floor
 * (3/6/9/12/15/18). Reads on the town-floor identities in
 * {@code FloorIdentity.md}: a starter boss for the first settlement,
 * escalating in both mob choice and loot tier through to a deliberately
 * dramatic final-floor pick.
 */
public final class VanillaBosses {

    private VanillaBosses() {
    }

    public static void bootstrap() {
        register(3, "rotbound_chieftain", EntityType.ZOMBIE, "Rotbound Chieftain", LootTables.UNCOMMON);
        register(6, "vindicator_overseer", EntityType.VINDICATOR, "Vindicator Overseer", LootTables.RARE);
        register(9, "siege_ravager", EntityType.RAVAGER, "Siege Ravager", LootTables.RARE);
        register(12, "evoker_warlord", EntityType.EVOKER, "Evoker Warlord", LootTables.EPIC);
        register(15, "bone_champion", EntityType.WITHER_SKELETON, "Bone Champion", LootTables.EPIC);
        register(18, "depths_warden", EntityType.WARDEN, "The Depths Warden", LootTables.LEGENDARY);

        DescentMod.LOGGER.info("Registered 6 vanilla-mob bosses");
    }

    private static void register(int floorNumber, String path, EntityType<? extends Mob> entityType, String name, ResourceLocation lootTable) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(DescentMod.MODID, "boss/" + path);
        BossLoot loot = new VanillaBossLoot(lootTable, 3, floorNumber);
        BossRegistry.register(new VanillaBoss(id, floorNumber, entityType, Component.literal(name), loot));
    }
}
