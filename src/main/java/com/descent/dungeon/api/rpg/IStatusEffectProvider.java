package com.descent.dungeon.api.rpg;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Future extension point for RPG-driven status effects (buffs/debuffs from
 * spells, skills, or equipment bonuses, layered on top of vanilla
 * {@code MobEffect}s). Not implemented yet — see {@link IPlayerClass}.
 */
public interface IStatusEffectProvider {

    void apply(ServerPlayer player, ResourceLocation statusId, int durationTicks, int amplifier);

    boolean isActive(ServerPlayer player, ResourceLocation statusId);

    void clear(ServerPlayer player, ResourceLocation statusId);
}
