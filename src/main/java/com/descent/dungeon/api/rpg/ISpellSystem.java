package com.descent.dungeon.api.rpg;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/** Future extension point for castable spells, layered on {@link IResourcePool} costs and {@link ISkill} cooldowns. Not implemented yet — see {@link IPlayerClass}. */
public interface ISpellSystem {

    /** @return whether the spell was successfully cast (resources consumed, cooldown started) */
    boolean cast(ServerPlayer caster, ResourceLocation spellId);
}
