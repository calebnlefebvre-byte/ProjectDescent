package com.descent.dungeon.api.reward;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Extension point for a future server-wide announcement system (milestone
 * broadcasts, sponsor gifts, "crowd popularity" callouts, statistics
 * summaries). "Future support only" per the design document — build the
 * interface, do not implement the behavior yet. Gameplay code should
 * announce through {@link com.descent.dungeon.hooks.DungeonHooks#announcer()}
 * so a richer implementation can replace {@link NullRewardAnnouncer} later.
 */
public interface IRewardAnnouncer {

    /** Announce {@code message} to every player on the server. */
    void announceServerWide(Component message);

    /** Announce a personal milestone to a single player. */
    void announceToPlayer(ServerPlayer player, Component message);
}
