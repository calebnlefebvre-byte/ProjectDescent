package com.descent.dungeon.api.events;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** Posted when a player descends Floor 18's final staircase and completes the dungeon. */
public final class PlayerVictoryEvent extends Event {

    private final ServerPlayer player;

    public PlayerVictoryEvent(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer player() {
        return player;
    }
}
