package com.descent.dungeon.util;

/**
 * Derives a per-floor generation seed from the world seed and a floor
 * number. Every floor is generated from {@code (worldSeed, floorNumber)}
 * alone — same inputs, same dungeon, forever — which is what "generation
 * must always be deterministic" means in the design document. Floor
 * generation (Phase 2) is the only intended caller; it is a separate,
 * dependency-free utility so anything else that needs a stable per-floor
 * seed (loot rolls, mob placement) can derive its own sub-seed from the same
 * root without coupling to the generation package.
 */
public final class DeterministicSeed {

    private DeterministicSeed() {
    }

    /**
     * Mixes the world seed with a floor number into a well-distributed 64-bit
     * seed, using the SplitMix64 finalizer so adjacent floor numbers do not
     * produce correlated seeds.
     */
    public static long deriveFloorSeed(long worldSeed, int floorNumber) {
        long x = worldSeed ^ (0x9E3779B97F4A7C15L * (floorNumber + 1L));
        x = (x ^ (x >>> 30)) * 0xBF58476D1CE4E5B9L;
        x = (x ^ (x >>> 27)) * 0x94D049BB133111EBL;
        return x ^ (x >>> 31);
    }
}
