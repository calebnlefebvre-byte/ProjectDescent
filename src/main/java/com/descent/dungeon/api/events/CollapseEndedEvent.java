package com.descent.dungeon.api.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

import java.util.List;

/** Posted when a floor's collapse finishes; {@code playersCaught} were on the floor at that instant and have been killed. */
public final class CollapseEndedEvent extends Event {

    private final ServerLevel level;
    private final int floorNumber;
    private final List<ServerPlayer> playersCaught;

    public CollapseEndedEvent(ServerLevel level, int floorNumber, List<ServerPlayer> playersCaught) {
        this.level = level;
        this.floorNumber = floorNumber;
        this.playersCaught = List.copyOf(playersCaught);
    }

    public ServerLevel level() {
        return level;
    }

    public int floorNumber() {
        return floorNumber;
    }

    public List<ServerPlayer> playersCaught() {
        return playersCaught;
    }
}
