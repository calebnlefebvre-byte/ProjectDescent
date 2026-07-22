package com.descent.dungeon.api.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** Posted when a floor's boss is defeated (see {@code bosses.IBoss}). Not fired yet — no boss exists until Phase 4's framework has at least one plugged in. */
public final class BossDefeatedEvent extends Event {

    private final ServerPlayer killer;
    private final int floorNumber;
    private final ResourceLocation bossId;

    public BossDefeatedEvent(ServerPlayer killer, int floorNumber, ResourceLocation bossId) {
        this.killer = killer;
        this.floorNumber = floorNumber;
        this.bossId = bossId;
    }

    public ServerPlayer killer() {
        return killer;
    }

    public int floorNumber() {
        return floorNumber;
    }

    public ResourceLocation bossId() {
        return bossId;
    }
}
