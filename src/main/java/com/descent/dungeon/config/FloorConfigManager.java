package com.descent.dungeon.config;

import com.descent.dungeon.DescentMod;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holds the effective {@link FloorDefinition} for every floor number.
 * <p>
 * The dungeon always has exactly {@link #FLOOR_COUNT} floors — that is a
 * fixed design invariant, not a config option (see design doc: "exactly 18
 * dungeon floors", "no Floor 19"). What <em>is</em> configurable per floor is
 * its timer length, staircase count, theme, and town/boss flag, and those are
 * loaded from {@code data/descent/floors/*.json} on every resource reload,
 * falling back to {@link #defaultFloorSchedule()} for any floor a data pack
 * does not override.
 * <p>
 * Reads of {@link #get(int)} are safe from any thread: the backing map is
 * replaced atomically on reload, never mutated in place.
 */
public final class FloorConfigManager {

    public static final int FLOOR_COUNT = 18;

    /** Floors on which towns (and therefore bosses) occur. */
    private static final java.util.Set<Integer> TOWN_FLOORS = java.util.Set.of(3, 6, 9, 12, 15, 18);

    private static volatile Map<Integer, FloorDefinition> floors = defaultFloorSchedule();

    private FloorConfigManager() {
    }

    /** Returns the effective definition for a floor, falling back to the design default if unset. */
    public static FloorDefinition get(int floorNumber) {
        FloorDefinition definition = floors.get(floorNumber);
        if (definition == null) {
            throw new IllegalArgumentException("No floor definition for floor " + floorNumber
                    + " (valid range is 1.." + FLOOR_COUNT + ")");
        }
        return definition;
    }

    public static Map<Integer, FloorDefinition> all() {
        return floors;
    }

    public static void onAddReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(ResourceLocation.fromNamespaceAndPath(DescentMod.MODID, "floors"), new ReloadListener());
    }

    /**
     * The hardcoded default 18-floor schedule from the design document:
     * timer days, staircase counts, town/boss floors on 3/6/9/12/15/18, and a
     * distinct theme per non-town floor. Data packs override entries in this
     * map on a per-floor basis; they do not need to redefine every floor.
     */
    static Map<Integer, FloorDefinition> defaultFloorSchedule() {
        int[] timerDays = {5, 6, 8, 10, 15, 17, 19, 21, 30, 18, 22, 26, 32, 40, 50, 60, 75, 100};
        int[] stairCounts = {8, 7, 6, 5, 5, 4, 4, 3, 3, 3, 2, 2, 2, 2, 1, 1, 1, 1};
        String[] themes = {
                "crystal_caverns",   // 1
                "frozen_tunnels",    // 2
                "town",              // 3
                "volcanic_depths",   // 4
                "poison_marsh",      // 5
                "town",              // 6
                "ancient_fortress",  // 7
                "spider_nest",       // 8
                "town",              // 9
                "underground_jungle",// 10
                "haunted_catacombs", // 11
                "town",              // 12
                "sunken_aqueduct",   // 13
                "fungal_hollow",     // 14
                "town",              // 15
                "obsidian_rift",     // 16
                "bone_quarry",       // 17
                "town"               // 18, final floor
        };

        Map<Integer, FloorDefinition> schedule = new LinkedHashMap<>(FLOOR_COUNT);
        for (int i = 0; i < FLOOR_COUNT; i++) {
            int floorNumber = i + 1;
            boolean townFloor = TOWN_FLOORS.contains(floorNumber);
            schedule.put(floorNumber, new FloorDefinition(
                    floorNumber,
                    timerDays[i],
                    stairCounts[i],
                    ResourceLocation.fromNamespaceAndPath(DescentMod.MODID, themes[i]),
                    townFloor,
                    townFloor, // boss floors coincide with town floors per the design document
                    java.util.List.of() // no floor modifiers by default; see api.modifier.IFloorModifier
            ));
        }
        return Map.copyOf(schedule);
    }

    private static final class ReloadListener extends SimpleJsonResourceReloadListener<FloorDefinition> {

        ReloadListener() {
            super(FloorDefinition.CODEC, FileToIdConverter.json("descent/floors"));
        }

        @Override
        protected void apply(Map<ResourceLocation, FloorDefinition> loaded, ResourceManager manager, ProfilerFiller profiler) {
            Map<Integer, FloorDefinition> next = new LinkedHashMap<>(defaultFloorSchedule());
            for (FloorDefinition definition : loaded.values()) {
                if (definition.floorNumber() < 1 || definition.floorNumber() > FLOOR_COUNT) {
                    DescentMod.LOGGER.warn("Ignoring floor definition for out-of-range floor {} (valid range is 1..{})",
                            definition.floorNumber(), FLOOR_COUNT);
                    continue;
                }
                next.put(definition.floorNumber(), definition);
            }
            floors = Map.copyOf(next);
            DescentMod.LOGGER.info("Loaded floor configuration for {} floors ({} from data packs)",
                    next.size(), loaded.size());
        }
    }
}
