package com.descent.dungeon.collapse;

import com.descent.dungeon.DescentMod;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds the effective {@link CollapseProfile} for each theme that has one,
 * falling back to {@link #DEFAULT_PROFILE} (uniform across every
 * {@link HazardType}, matching the original Phase 3 behavior) for any theme
 * that doesn't. Only three representative themes ship a hardcoded profile —
 * per the design review, this is meant to prove the framework works, not to
 * hand-tune all eighteen floors before there's gameplay feedback to tune
 * against. Data packs can add or override profiles under
 * {@code data/descent/collapse_profiles/*.json} without touching Java.
 */
public final class CollapseProfileRegistry {

    public static final CollapseProfile DEFAULT_PROFILE = new CollapseProfile(
            themeId("default"), uniformWeights());

    private static volatile Map<ResourceLocation, CollapseProfile> profiles = defaultProfiles();

    private CollapseProfileRegistry() {
    }

    public static CollapseProfile forTheme(ResourceLocation themeId) {
        return profiles.getOrDefault(themeId, DEFAULT_PROFILE);
    }

    public static void onAddReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(ResourceLocation.fromNamespaceAndPath(DescentMod.MODID, "collapse_profiles"), new ReloadListener());
    }

    private static Map<HazardType, Integer> uniformWeights() {
        Map<HazardType, Integer> weights = new HashMap<>();
        for (HazardType type : HazardType.values()) {
            weights.put(type, 10);
        }
        return Map.copyOf(weights);
    }

    /**
     * Three representative profiles matching the design review's examples:
     * a cave-in/debris-heavy profile for Crystal Caverns (Floor 1), a
     * lava/fire-heavy profile for Volcanic Depths, and a darkness/lightning/
     * phantom profile for Haunted Catacombs. The review's own examples used
     * illustrative floor numbers (8, 14) that don't match this project's
     * actual floor-to-theme schedule from {@code FloorConfigManager}; since
     * profiles key by theme rather than floor number, the same flavor is
     * applied to the theme it actually fits (Volcanic Depths, Haunted
     * Catacombs) instead.
     */
    private static Map<ResourceLocation, CollapseProfile> defaultProfiles() {
        Map<ResourceLocation, CollapseProfile> map = new HashMap<>();

        map.put(themeId("crystal_caverns"), new CollapseProfile(themeId("crystal_caverns"), Map.of(
                HazardType.BLOCK_COLLAPSE, 35,
                HazardType.FALLING_DEBRIS, 35,
                HazardType.EARTHQUAKE, 20,
                HazardType.POISON_GAS, 10)));

        map.put(themeId("volcanic_depths"), new CollapseProfile(themeId("volcanic_depths"), Map.of(
                HazardType.LAVA_FISSURE, 30,
                HazardType.EXPLOSION, 25,
                HazardType.FIRE, 20,
                HazardType.EARTHQUAKE, 15,
                HazardType.BLOCK_COLLAPSE, 10)));

        map.put(themeId("haunted_catacombs"), new CollapseProfile(themeId("haunted_catacombs"), Map.of(
                HazardType.DARKNESS, 30,
                HazardType.PHANTOM_ATTACK, 25,
                HazardType.LIGHTNING, 20,
                HazardType.EARTHQUAKE, 15,
                HazardType.FALLING_DEBRIS, 10)));

        return Map.copyOf(map);
    }

    private static ResourceLocation themeId(String path) {
        return ResourceLocation.fromNamespaceAndPath(DescentMod.MODID, path);
    }

    private static final class ReloadListener extends SimpleJsonResourceReloadListener<CollapseProfile> {

        ReloadListener() {
            super(CollapseProfile.CODEC, FileToIdConverter.json("descent/collapse_profiles"));
        }

        @Override
        protected void apply(Map<ResourceLocation, CollapseProfile> loaded, ResourceManager manager, ProfilerFiller profiler) {
            Map<ResourceLocation, CollapseProfile> next = new HashMap<>(defaultProfiles());
            for (CollapseProfile profile : loaded.values()) {
                next.put(profile.id(), profile);
            }
            profiles = Map.copyOf(next);
            DescentMod.LOGGER.info("Loaded {} collapse profile(s) ({} from data packs)", next.size(), loaded.size());
        }
    }
}
