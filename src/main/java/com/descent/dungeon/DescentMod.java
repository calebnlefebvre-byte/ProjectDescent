package com.descent.dungeon;

import com.descent.dungeon.bosses.VanillaBosses;
import com.descent.dungeon.collapse.CollapseProfileRegistry;
import com.descent.dungeon.commands.DescentCommands;
import com.descent.dungeon.config.DescentCommonConfig;
import com.descent.dungeon.config.FloorConfigManager;
import com.descent.dungeon.events.BossEvents;
import com.descent.dungeon.events.FloorGenerationEvents;
import com.descent.dungeon.events.FloorTimerEvents;
import com.descent.dungeon.events.MobScalingEvents;
import com.descent.dungeon.events.StaircaseEvents;
import com.descent.dungeon.hooks.HookBridgeEvents;
import com.descent.dungeon.loot.LootTables;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

/**
 * Mod entry point. Wires configuration, data reload listeners, and command
 * registration. Gameplay systems (floors, timers, collapse, stairs, bosses,
 * loot) are added in later development phases and register themselves through
 * {@link com.descent.dungeon.hooks.DungeonHooks} and their own event listeners
 * rather than growing this class.
 */
@Mod(DescentMod.MODID)
public final class DescentMod {

    public static final String MODID = "descent";
    public static final Logger LOGGER = LogUtils.getLogger();

    public DescentMod(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, DescentCommonConfig.SPEC);

        LootTables.bootstrap();
        VanillaBosses.bootstrap();

        NeoForge.EVENT_BUS.addListener(FloorConfigManager::onAddReloadListeners);
        NeoForge.EVENT_BUS.addListener(CollapseProfileRegistry::onAddReloadListeners);
        NeoForge.EVENT_BUS.addListener(DescentCommands::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(FloorGenerationEvents::onPlayerChangedDimension);
        NeoForge.EVENT_BUS.addListener(FloorTimerEvents::onServerTick);
        NeoForge.EVENT_BUS.addListener(StaircaseEvents::onServerTick);
        NeoForge.EVENT_BUS.addListener(StaircaseEvents::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(HookBridgeEvents::onPlayerVictory);
        NeoForge.EVENT_BUS.addListener(HookBridgeEvents::onBossDefeated);
        NeoForge.EVENT_BUS.addListener(BossEvents::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(MobScalingEvents::onFinalizeSpawn);

        LOGGER.info("Project Descent initializing ({} floors in the default schedule)",
                FloorConfigManager.FLOOR_COUNT);
    }
}
