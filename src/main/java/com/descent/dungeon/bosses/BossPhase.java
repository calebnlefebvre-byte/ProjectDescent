package com.descent.dungeon.bosses;

import java.util.List;

/**
 * One stage of a boss fight: once the boss's health fraction drops at or
 * below {@code healthFractionThreshold}, this phase's abilities become the
 * ones in rotation. A single-phase boss just has one entry with threshold
 * {@code 1.0}.
 */
public record BossPhase(double healthFractionThreshold, List<BossAbility> abilities) {
}
