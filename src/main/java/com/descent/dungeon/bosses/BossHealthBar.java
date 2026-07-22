package com.descent.dungeon.bosses;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;

/** Thin wrapper over vanilla's {@link ServerBossEvent} so boss code deals in intent (add a player, update progress) rather than boss-bar plumbing. */
public final class BossHealthBar {

    private final ServerBossEvent bossEvent;

    public BossHealthBar(Component name, BossEvent.BossBarColor color) {
        this.bossEvent = new ServerBossEvent(name, color, BossEvent.BossBarOverlay.PROGRESS);
    }

    public void addPlayer(ServerPlayer player) {
        bossEvent.addPlayer(player);
    }

    public void removePlayer(ServerPlayer player) {
        bossEvent.removePlayer(player);
    }

    /** @param fraction 0.0 (dead) to 1.0 (full health) */
    public void updateProgress(float fraction) {
        bossEvent.setProgress(Math.max(0.0F, Math.min(1.0F, fraction)));
    }

    public void removeAllPlayers() {
        bossEvent.removeAllPlayers();
    }
}
