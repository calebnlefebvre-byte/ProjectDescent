package com.descent.dungeon.bosses;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;

import java.util.List;

/** One action a boss can take during a {@link BossPhase} — an attack, a summon, an environmental effect, whatever the boss defines. */
public interface BossAbility {

    ResourceLocation id();

    int cooldownTicks();

    void execute(ServerLevel level, Mob boss, List<ServerPlayer> targets);
}
