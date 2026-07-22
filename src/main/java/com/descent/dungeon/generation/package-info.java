/**
 * Deterministic per-floor dimension generation. Each floor is its own
 * generated dimension/world instance (not physically stacked with the
 * others), produced from {@code (worldSeed, floorNumber)} alone via
 * {@link com.descent.dungeon.util.DeterministicSeed} — same inputs, same
 * dungeon, always. Also owns theme selection (town floors on 3/6/9/12/15/18,
 * distinct non-town themes elsewhere) and staircase placement. Built in
 * development Phase 2.
 */
package com.descent.dungeon.generation;
