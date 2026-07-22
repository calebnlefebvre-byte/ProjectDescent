/**
 * Per-floor countdown timers, measured in Minecraft days, starting the
 * moment players enter a floor. Configured per floor by
 * {@link com.descent.dungeon.config.FloorDefinition#timerDays()} (data-driven,
 * see {@link com.descent.dungeon.config.FloorConfigManager}). Modeled as six
 * phases — {@link com.descent.dungeon.timer.FloorClock.Phase} — rather than
 * three, expanded in the pre-Phase-4 design review specifically so a future
 * phase can be inserted without restructuring the lookup logic. Reaching
 * {@code COLLAPSING} hands off to {@link com.descent.dungeon.collapse}.
 * Shared across all players on a floor in multiplayer. Built in development
 * Phase 3.
 */
package com.descent.dungeon.timer;
