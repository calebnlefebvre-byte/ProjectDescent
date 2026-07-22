package com.descent.dungeon.api.rules;

/**
 * A named, world-wide on/off toggle for a future challenge-mode system.
 * Distinct from {@code api.modifier.IFloorModifier}: a modifier is a
 * per-floor <em>behavior</em> with code attached ({@code apply(...)});
 * a rule is a global, static <em>constraint</em> other systems check against
 * ("is block placement allowed right now") — no behavior of its own.
 * Reserved for a future challenge-mode system (see the design review);
 * nothing currently checks or enforces any of these.
 */
public enum DungeonRule {
    NO_BLOCK_PLACEMENT,
    NO_WATER_BUCKETS,
    NO_SLEEPING,
    NO_TOTEMS,
    KEEP_INVENTORY_DISABLED,
    FRIENDLY_FIRE_ENABLED
}
