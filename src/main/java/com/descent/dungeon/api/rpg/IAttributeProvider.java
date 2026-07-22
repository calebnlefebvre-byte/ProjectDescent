package com.descent.dungeon.api.rpg;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/** Future extension point for RPG attributes (strength, agility, etc). Not implemented yet — see {@link IPlayerClass}. */
public interface IAttributeProvider {

    double getAttributeValue(ServerPlayer player, ResourceLocation attributeId);
}
