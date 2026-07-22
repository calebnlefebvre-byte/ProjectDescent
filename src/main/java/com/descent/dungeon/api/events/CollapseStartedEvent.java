package com.descent.dungeon.api.events;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;

/** Posted alongside {@link TimerExpiredEvent} when a floor's collapse hazard sequence begins. */
public final class CollapseStartedEvent extends Event {

    private final ServerLevel level;
    private final int floorNumber;

    public CollapseStartedEvent(ServerLevel level, int floorNumber) {
        this.level = level;
        this.floorNumber = floorNumber;
    }

    public ServerLevel level() {
        return level;
    }

    public int floorNumber() {
        return floorNumber;
    }
}
