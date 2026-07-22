package com.descent.dungeon.generation;

import com.descent.dungeon.DescentMod;
import com.descent.dungeon.api.modifier.IFloorModifier;
import com.descent.dungeon.context.DungeonContext;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry of installed {@link IFloorModifier}s and the place that runs them
 * over a freshly-generated floor. Empty today — no modifier is implemented
 * yet, per the design review's "does not need to be fully implemented now,
 * only design the framework." {@link #applyModifiers} is still called from
 * {@code events.FloorGenerationEvents} right after staircase placement (the
 * "generate floor -&gt; apply floor modifiers -&gt; finalize floor" pipeline),
 * so plugging in the first real modifier later is a one-line
 * {@link #register} call, not a change to the generation flow.
 */
public final class FloorModifierRegistry {

    private static final Map<ResourceLocation, IFloorModifier> MODIFIERS = new HashMap<>();

    private FloorModifierRegistry() {
    }

    public static void register(IFloorModifier modifier) {
        MODIFIERS.put(modifier.id(), modifier);
    }

    /** Applies every modifier {@code context.activeModifiers()} lists, in order, skipping (and logging) any that aren't registered. */
    public static void applyModifiers(DungeonContext context) {
        for (ResourceLocation modifierId : context.activeModifiers()) {
            IFloorModifier modifier = MODIFIERS.get(modifierId);
            if (modifier == null) {
                DescentMod.LOGGER.warn("Floor {} requests unknown modifier '{}' — skipping", context.floorNumber(), modifierId);
                continue;
            }
            modifier.apply(context);
        }
    }
}
