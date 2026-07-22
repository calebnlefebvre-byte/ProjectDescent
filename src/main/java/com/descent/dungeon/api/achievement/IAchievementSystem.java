package com.descent.dungeon.api.achievement;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Extension point for a future achievement / loot-box / sponsor-gift /
 * milestone reward system. Per the design document this is "future support
 * only" for now — no achievements are defined yet, and nothing should be
 * implemented beyond {@link NullAchievementSystem}. Gameplay code that
 * reaches a milestone (floor cleared, boss defeated, dungeon completed)
 * should still report it through {@link com.descent.dungeon.hooks.DungeonHooks#achievements()}
 * so a real system can be dropped in later without touching those call sites.
 */
public interface IAchievementSystem {

    /** Reports that {@code player} reached the milestone identified by {@code achievementId}. */
    void grant(ServerPlayer player, ResourceLocation achievementId);

    /** Whether {@code player} has already been granted {@code achievementId}. */
    boolean hasGranted(ServerPlayer player, ResourceLocation achievementId);
}
