package com.descent.dungeon.api.modifier;

import com.descent.dungeon.config.FloorDefinition;
import com.descent.dungeon.context.DungeonContext;
import net.minecraft.resources.ResourceLocation;

/**
 * Extension point for a modular special-floor behavior (double monsters,
 * darkness, no healing, blessed/cursed floor, double loot, reduced
 * visibility, permanent hunger, mana suppression — per the design review).
 * None of these are implemented yet; this interface only exists so the
 * generation pipeline has a real seam to apply them through once they are:
 *
 * <pre>
 * generate floor -&gt; apply floor modifiers ({@link #apply}) -&gt; finalize floor
 * </pre>
 * <p>
 * A floor opts into modifiers by listing their IDs in its
 * {@link FloorDefinition} (data-driven, empty by default), so which floors
 * get which modifiers is content, not code. See
 * {@code generation.FloorModifierRegistry} for where implementations
 * register themselves and where {@link #apply} is actually invoked
 * (currently a no-op loop, since {@code FloorDefinition.modifiers()} is
 * always empty until a data pack or a future phase populates it).
 */
public interface IFloorModifier {

    ResourceLocation id();

    /** Applies this modifier's effect to a freshly-generated floor. Called once, after staircase placement, before players can enter. */
    void apply(DungeonContext context);
}
