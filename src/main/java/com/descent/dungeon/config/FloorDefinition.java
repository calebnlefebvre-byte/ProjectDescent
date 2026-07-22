package com.descent.dungeon.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Data-driven description of a single dungeon floor: how long its timer runs
 * before collapse, how many staircases it generates, which theme it uses,
 * whether it is a town floor / boss floor, and which
 * {@code api.modifier.IFloorModifier}s it applies.
 * <p>
 * Instances are produced two ways:
 * <ul>
 *     <li>{@link FloorConfigManager#defaultFloorSchedule()} — the 18-floor
 *     default schedule described in the design document, used when no data
 *     pack overrides a given floor.</li>
 *     <li>JSON files under {@code data/descent/floors/*.json}, decoded with
 *     {@link #CODEC}, which override or extend the defaults on datapack
 *     reload. This is what "everything configurable" means for floor
 *     scheduling: server owners and future content packs can add or reshape
 *     floors without touching Java code.</li>
 * </ul>
 */
public record FloorDefinition(
        int floorNumber,
        int timerDays,
        int stairCount,
        ResourceLocation theme,
        boolean townFloor,
        boolean bossFloor,
        List<ResourceLocation> modifiers
) {

    public static final Codec<FloorDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("floor_number").forGetter(FloorDefinition::floorNumber),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("timer_days").forGetter(FloorDefinition::timerDays),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("stair_count").forGetter(FloorDefinition::stairCount),
            ResourceLocation.CODEC.fieldOf("theme").forGetter(FloorDefinition::theme),
            Codec.BOOL.optionalFieldOf("town_floor", false).forGetter(FloorDefinition::townFloor),
            Codec.BOOL.optionalFieldOf("boss_floor", false).forGetter(FloorDefinition::bossFloor),
            ResourceLocation.CODEC.listOf().optionalFieldOf("modifiers", List.of()).forGetter(FloorDefinition::modifiers)
    ).apply(instance, FloorDefinition::new));
}
