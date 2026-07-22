package com.descent.dungeon.collapse;

/**
 * The catalogue of collapse hazards {@code CollapseHazards} knows how to
 * strike with. A {@link CollapseProfile} decides how often each one comes up
 * on a given floor; this enum is just the fixed vocabulary they're expressed
 * in. Adding a new hazard means adding a case here and in
 * {@code CollapseHazards#strike} — profiles referencing it by name (JSON or
 * the hardcoded defaults) don't need to change.
 */
public enum HazardType {
    EXPLOSION,
    FIRE,
    POISON_GAS,
    DARKNESS,
    FALLING_DEBRIS,
    EARTHQUAKE,
    BLOCK_COLLAPSE,
    LAVA_FISSURE,
    LIGHTNING,
    PHANTOM_ATTACK
}
