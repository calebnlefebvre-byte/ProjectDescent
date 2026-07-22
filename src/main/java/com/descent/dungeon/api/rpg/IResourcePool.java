package com.descent.dungeon.api.rpg;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Future extension point for a regenerating player resource pool. Mana and
 * stamina are structurally identical (current/max, regenerate over time,
 * consumed by actions), so both are modeled as instances of this one
 * interface distinguished by {@code poolId}, rather than duplicating the
 * contract per resource. Not implemented yet — see {@link IPlayerClass}.
 */
public interface IResourcePool {

    double getCurrent(ServerPlayer player, ResourceLocation poolId);

    double getMax(ServerPlayer player, ResourceLocation poolId);

    /** @return whether the full amount was available and consumed */
    boolean consume(ServerPlayer player, ResourceLocation poolId, double amount);

    void restore(ServerPlayer player, ResourceLocation poolId, double amount);
}
