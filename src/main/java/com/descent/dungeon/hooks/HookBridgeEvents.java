package com.descent.dungeon.hooks;

import com.descent.dungeon.DescentMod;
import com.descent.dungeon.api.events.BossDefeatedEvent;
import com.descent.dungeon.api.events.PlayerVictoryEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Translates domain events (see {@code api.events}) into calls on the
 * installed {@code api.*} extension points. This is the one place that
 * knows both "a milestone happened" and "who should be told about it" —
 * everything else either posts an event or subscribes to one, never both,
 * which is what lets a future achievement/announcer implementation change
 * without touching {@code events.StaircaseEvents} or any other gameplay
 * code that posts these events.
 */
public final class HookBridgeEvents {

    private static final ResourceLocation DUNGEON_COMPLETED = ResourceLocation.fromNamespaceAndPath(DescentMod.MODID, "dungeon_completed");

    private HookBridgeEvents() {
    }

    public static void onPlayerVictory(PlayerVictoryEvent event) {
        DungeonHooks.achievements().grant(event.player(), DUNGEON_COMPLETED);
        DungeonHooks.announcer().announceServerWide(Component.literal(
                event.player().getGameProfile().getName() + " has escaped the dungeon!"));
    }

    public static void onBossDefeated(BossDefeatedEvent event) {
        ResourceLocation achievementId = ResourceLocation.fromNamespaceAndPath(DescentMod.MODID,
                "boss_defeated/" + event.bossId().getPath());
        DungeonHooks.achievements().grant(event.killer(), achievementId);
        DungeonHooks.announcer().announceServerWide(Component.literal(
                event.killer().getGameProfile().getName() + " has defeated Floor " + event.floorNumber() + "'s boss!"));
    }
}
