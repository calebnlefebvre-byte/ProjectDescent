package com.descent.dungeon.api.rules;

import java.util.EnumSet;
import java.util.Set;

/**
 * Holds which {@link DungeonRule}s are currently active. A future challenge
 * mode becomes {@code DungeonRules + IFloorModifier}s layered together —
 * this class is the "rules" half, reserved ahead of any content or
 * enforcement logic existing. Nothing currently calls {@link #isActive} to
 * gate anything; it's a placeholder, not a behavior.
 */
public final class DungeonRules {

    private static volatile Set<DungeonRule> active = EnumSet.noneOf(DungeonRule.class);

    private DungeonRules() {
    }

    public static boolean isActive(DungeonRule rule) {
        return active.contains(rule);
    }

    public static void setActive(Set<DungeonRule> rules) {
        active = rules.isEmpty() ? EnumSet.noneOf(DungeonRule.class) : EnumSet.copyOf(rules);
    }
}
