package com.descent.dungeon.api.achievement;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/** Grants nothing and remembers nothing. Default until a real achievement system exists. */
public final class NullAchievementSystem implements IAchievementSystem {

    public static final NullAchievementSystem INSTANCE = new NullAchievementSystem();

    private NullAchievementSystem() {
    }

    @Override
    public void grant(ServerPlayer player, ResourceLocation achievementId) {
        // no-op
    }

    @Override
    public boolean hasGranted(ServerPlayer player, ResourceLocation achievementId) {
        return false;
    }
}
