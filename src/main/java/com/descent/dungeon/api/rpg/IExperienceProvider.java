package com.descent.dungeon.api.rpg;

import net.minecraft.server.level.ServerPlayer;

/** Future extension point for RPG experience/leveling. Not implemented yet — see {@link IPlayerClass}. */
public interface IExperienceProvider {

    long getExperience(ServerPlayer player);

    void addExperience(ServerPlayer player, long amount);

    int getLevel(ServerPlayer player);
}
