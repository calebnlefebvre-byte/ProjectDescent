package com.descent.dungeon.api.rpg;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/** Future extension point for a talent tree grouping {@link ISkill}s. Not implemented yet — see {@link IPlayerClass}. */
public interface ITalentTree {

    ResourceLocation id();

    List<ISkill> skills();

    boolean isUnlocked(ServerPlayer player, ISkill skill);
}
