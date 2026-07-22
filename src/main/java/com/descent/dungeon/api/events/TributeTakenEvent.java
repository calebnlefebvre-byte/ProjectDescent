package com.descent.dungeon.api.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

/** Posted after the Early Descent Tribute destroys an item (see {@code stairs.DungeonTribute}). Not posted if the player carried nothing eligible. */
public final class TributeTakenEvent extends Event {

    private final ServerPlayer player;
    private final int floorNumber;
    private final ItemStack destroyedItem;

    public TributeTakenEvent(ServerPlayer player, int floorNumber, ItemStack destroyedItem) {
        this.player = player;
        this.floorNumber = floorNumber;
        this.destroyedItem = destroyedItem;
    }

    public ServerPlayer player() {
        return player;
    }

    public int floorNumber() {
        return floorNumber;
    }

    /** The destroyed stack, retained only for inspection/logging/announcement — it has already been removed from the player's inventory. */
    public ItemStack destroyedItem() {
        return destroyedItem;
    }
}
