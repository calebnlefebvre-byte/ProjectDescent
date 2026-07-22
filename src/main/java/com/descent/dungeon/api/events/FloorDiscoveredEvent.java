package com.descent.dungeon.api.events;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** Posted when a staircase is discovered for the first time (world-shared — see {@code persistence.StaircaseRecord}). */
public final class FloorDiscoveredEvent extends Event {

    private final ServerPlayer discoverer;
    private final int floorNumber;
    private final BlockPos staircasePosition;

    public FloorDiscoveredEvent(ServerPlayer discoverer, int floorNumber, BlockPos staircasePosition) {
        this.discoverer = discoverer;
        this.floorNumber = floorNumber;
        this.staircasePosition = staircasePosition;
    }

    public ServerPlayer discoverer() {
        return discoverer;
    }

    public int floorNumber() {
        return floorNumber;
    }

    public BlockPos staircasePosition() {
        return staircasePosition;
    }
}
