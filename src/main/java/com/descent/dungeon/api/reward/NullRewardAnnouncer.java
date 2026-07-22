package com.descent.dungeon.api.reward;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** Announces nothing. Default until a real announcement system exists. */
public final class NullRewardAnnouncer implements IRewardAnnouncer {

    public static final NullRewardAnnouncer INSTANCE = new NullRewardAnnouncer();

    private NullRewardAnnouncer() {
    }

    @Override
    public void announceServerWide(Component message) {
        // no-op
    }

    @Override
    public void announceToPlayer(ServerPlayer player, Component message) {
        // no-op
    }
}
