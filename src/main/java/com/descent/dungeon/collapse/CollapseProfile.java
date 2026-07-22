package com.descent.dungeon.collapse;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.Map;

/**
 * A floor's (or theme's) weighted collapse-hazard table — the design
 * review's request that "collapse behavior should become floor-specific."
 * Keyed by theme rather than floor number in
 * {@link CollapseProfileRegistry#forTheme(ResourceLocation)}: a theme's
 * collapse flavor (volcanic floors lean on lava/fire, haunted floors on
 * darkness/phantoms) should follow the theme wherever it's assigned, the
 * same way biome selection already does in {@code generation}, rather than
 * being pinned to a specific floor number.
 * <p>
 * Data-driven the same way {@code config.FloorDefinition} is: hardcoded
 * defaults for a few representative themes, overridable/extensible via
 * {@code data/descent/collapse_profiles/*.json}.
 */
public record CollapseProfile(ResourceLocation id, Map<HazardType, Integer> weights) {

    private static final Codec<HazardType> HAZARD_TYPE_CODEC = Codec.STRING.xmap(HazardType::valueOf, Enum::name);

    public static final Codec<CollapseProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(CollapseProfile::id),
            Codec.unboundedMap(HAZARD_TYPE_CODEC, Codec.INT).fieldOf("weights").forGetter(CollapseProfile::weights)
    ).apply(instance, CollapseProfile::new));

    /** Picks one hazard, weighted by this profile's table. Falls back to a uniform pick if all weights are zero/empty. */
    public HazardType pickHazard(RandomSource random) {
        int total = weights.values().stream().mapToInt(Integer::intValue).sum();
        if (total <= 0) {
            HazardType[] all = HazardType.values();
            return all[random.nextInt(all.length)];
        }
        int roll = random.nextInt(total);
        int cursor = 0;
        for (Map.Entry<HazardType, Integer> entry : weights.entrySet()) {
            cursor += entry.getValue();
            if (roll < cursor) {
                return entry.getKey();
            }
        }
        // Unreachable given roll < total, but the compiler can't prove that from a Map's iteration.
        return weights.keySet().iterator().next();
    }
}
