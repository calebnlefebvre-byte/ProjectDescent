package com.descent.dungeon.api.events;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** Posted after a player descends from one floor to the next (not fired for the Floor 18 -> victory case; see {@link PlayerVictoryEvent}). */
public final class PlayerDescendedFloorEvent extends Event {

    private final ServerPlayer player;
    private final int fromFloor;
    private final int toFloor;

    public PlayerDescendedFloorEvent(ServerPlayer player, int fromFloor, int toFloor) {
        this.player = player;
        this.fromFloor = fromFloor;
        this.toFloor = toFloor;
    }

    public ServerPlayer player() {
        return player;
    }

    public int fromFloor() {
        return fromFloor;
    }

    public int toFloor() {
        return toFloor;
    }
}
