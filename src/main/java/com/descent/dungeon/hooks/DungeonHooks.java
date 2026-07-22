package com.descent.dungeon.hooks;

import com.descent.dungeon.api.achievement.IAchievementSystem;
import com.descent.dungeon.api.achievement.NullAchievementSystem;
import com.descent.dungeon.api.director.IDungeonDirector;
import com.descent.dungeon.api.director.NullDungeonDirector;
import com.descent.dungeon.api.reward.IRewardAnnouncer;
import com.descent.dungeon.api.reward.NullRewardAnnouncer;

/**
 * Single point of access for the mod's swappable future-system extension
 * points. Every gameplay system that needs to consult the Dungeon Director,
 * grant an achievement, or announce a milestone should go through here
 * instead of holding its own reference — that way a real implementation can
 * be installed later (by this mod or an add-on) without touching any of
 * those call sites.
 * <p>
 * All three default to no-op implementations. Installing a replacement is a
 * deliberate, explicit action (see the setters below); nothing auto-detects
 * or reflectively discovers implementations.
 */
public final class DungeonHooks {

    private static volatile IDungeonDirector director = NullDungeonDirector.INSTANCE;
    private static volatile IAchievementSystem achievements = NullAchievementSystem.INSTANCE;
    private static volatile IRewardAnnouncer announcer = NullRewardAnnouncer.INSTANCE;

    private DungeonHooks() {
    }

    public static IDungeonDirector director() {
        return director;
    }

    public static void setDirector(IDungeonDirector director) {
        DungeonHooks.director = java.util.Objects.requireNonNull(director);
    }

    public static IAchievementSystem achievements() {
        return achievements;
    }

    public static void setAchievements(IAchievementSystem achievements) {
        DungeonHooks.achievements = java.util.Objects.requireNonNull(achievements);
    }

    public static IRewardAnnouncer announcer() {
        return announcer;
    }

    public static void setAnnouncer(IRewardAnnouncer announcer) {
        DungeonHooks.announcer = java.util.Objects.requireNonNull(announcer);
    }
}
