/**
 * {@link com.descent.dungeon.context.DungeonContext} — the shared immutable
 * snapshot (floor, theme, timer phase, collapse profile, seed, player count,
 * active modifiers) that systems needing "everything about where we are
 * right now" consume instead of assembling their own subset from several
 * different places. Currently built by {@code events.FloorGenerationEvents}
 * and {@code events.FloorTimerEvents}, consumed by
 * {@code api.modifier.IFloorModifier} and {@code collapse.CollapseHazards}.
 */
package com.descent.dungeon.context;
