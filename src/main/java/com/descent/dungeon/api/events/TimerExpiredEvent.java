package com.descent.dungeon.api.events;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;

/**
 * Posted the instant a floor's timer reaches zero — the {@code timer.FloorClock}
 * transition into {@code Phase.COLLAPSING}. Posted alongside {@link CollapseStartedEvent}
 * at the same moment; this one exists for listeners that only care "did the
 * clock run out," not the hazard sequence that follows it.
 */
public final class TimerExpiredEvent extends Event {

    private final ServerLevel level;
    private final int floorNumber;

    public TimerExpiredEvent(ServerLevel level, int floorNumber) {
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
