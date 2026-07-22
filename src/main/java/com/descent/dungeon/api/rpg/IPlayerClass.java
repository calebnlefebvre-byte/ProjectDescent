package com.descent.dungeon.api.rpg;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Extension point for a future RPG class system. Per the design document, the
 * full RPG framework (classes, experience, levels, attributes, mana,
 * stamina, spells, cooldowns, skills, status effects, equipment bonuses,
 * talent trees) is future support only — this package defines the shape
 * other systems will eventually call through, so it can be wired in with
 * minimal refactoring. Nothing implements these interfaces yet.
 */
public interface IPlayerClass {

    ResourceLocation id();

    Component displayName();
}
