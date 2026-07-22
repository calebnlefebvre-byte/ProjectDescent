/**
 * Configuration for Project Descent, split by shape rather than lumped into
 * one file:
 * <ul>
 *     <li>{@link com.descent.dungeon.config.DescentCommonConfig} — global
 *     formula inputs (difficulty growth rates, collapse duration/intensity,
 *     tribute toggle, debug permission gating) via NeoForge's
 *     {@code ModConfigSpec}, editable in the generated {@code .toml} file.</li>
 *     <li>{@link com.descent.dungeon.config.FloorDefinition} /
 *     {@link com.descent.dungeon.config.FloorConfigManager} — per-floor data
 *     (timer length, staircase count, theme, town/boss flag), data-driven
 *     from {@code data/descent/floors/*.json} and reloadable, because
 *     eighteen distinct floors are naturally data, not a handful of knobs.</li>
 * </ul>
 */
package com.descent.dungeon.config;
