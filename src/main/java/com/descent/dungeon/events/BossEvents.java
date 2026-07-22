package com.descent.dungeon.events;

import com.descent.dungeon.DescentMod;
import com.descent.dungeon.api.events.BossDefeatedEvent;
import com.descent.dungeon.bosses.BossRegistry;
import com.descent.dungeon.bosses.IBoss;
import com.descent.dungeon.bosses.VanillaBoss;
import com.descent.dungeon.persistence.DescentSavedData;
import com.descent.dungeon.persistence.FloorSaveState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.OptionalInt;

/**
 * Reacts to a boss dying: marks its floor's {@code FloorSaveState#bossDefeated()},
 * drops its {@code BossLoot}, and posts {@link BossDefeatedEvent}. Recognizes
 * a boss by the {@code descent_boss_<floor>} scoreboard tag
 * {@link VanillaBoss#configure} applies at spawn — a plain tag rather than a
 * side map, so it survives exactly as long as the entity does with no
 * separate bookkeeping to fall out of sync.
 */
public final class BossEvents {

    private static final String BOSS_TAG_PREFIX = "descent_boss_";

    private BossEvents() {
    }

    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        OptionalInt floorNumber = bossFloorFromTags(dead);
        if (floorNumber.isEmpty()) {
            return;
        }
        if (!(dead.level() instanceof ServerLevel level)) {
            return;
        }

        int floor = floorNumber.getAsInt();
        MinecraftServer server = level.getServer();
        if (server == null) {
            return;
        }

        DescentSavedData saveData = DescentSavedData.get(server);
        saveData.getFloor(floor).ifPresent(state -> saveData.putFloor(state.withBossDefeated()));

        BossRegistry.forFloor(floor).ifPresent(boss -> dropLootAndAnnounce(event, level, dead, floor, boss));
    }

    private static void dropLootAndAnnounce(LivingDeathEvent event, ServerLevel level, LivingEntity dead, int floor, IBoss boss) {
        RandomSource random = level.getRandom();
        for (ItemStack stack : boss.loot().generateLoot(level, random)) {
            level.addFreshEntity(new ItemEntity(level, dead.getX(), dead.getY(), dead.getZ(), stack));
        }

        DescentMod.LOGGER.info("Floor {}: boss '{}' defeated", floor, boss.id());

        if (event.getSource().getEntity() instanceof ServerPlayer killer) {
            NeoForge.EVENT_BUS.post(new BossDefeatedEvent(killer, floor, boss.id()));
        }
    }

    private static OptionalInt bossFloorFromTags(LivingEntity entity) {
        for (String tag : entity.getTags()) {
            if (tag.startsWith(BOSS_TAG_PREFIX)) {
                try {
                    return OptionalInt.of(Integer.parseInt(tag.substring(BOSS_TAG_PREFIX.length())));
                } catch (NumberFormatException ignored) {
                    // fall through to empty
                }
            }
        }
        return OptionalInt.empty();
    }
}
