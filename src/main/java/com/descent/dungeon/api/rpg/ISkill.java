package com.descent.dungeon.api.rpg;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Future extension point for a passive or active skill (talent-tree node,
 * class ability, etc). Not implemented yet — see {@link IPlayerClass}.
 */
public interface ISkill {

    enum Type { PASSIVE, ACTIVE }

    ResourceLocation id();

    Type type();

    /** Ticks remaining before this skill can be activated again; 0 if ready or passive. */
    int getRemainingCooldown(ServerPlayer player);

    /** No-op for passive skills; activates the skill's effect for active ones. */
    void activate(ServerPlayer player);
}
