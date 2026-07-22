/**
 * Extension-point interfaces for systems the design document explicitly
 * defers: the adaptive Dungeon Director ({@link com.descent.dungeon.api.director}),
 * achievements/rewards ({@link com.descent.dungeon.api.achievement},
 * {@link com.descent.dungeon.api.reward}), the RPG framework
 * ({@link com.descent.dungeon.api.rpg}), equipment tribute weighting
 * ({@link com.descent.dungeon.api.equipment}), and modular special-floor
 * behaviors ({@link com.descent.dungeon.api.modifier}). Also home to the
 * domain events ({@link com.descent.dungeon.api.events}) gameplay code posts
 * so those future systems (and others) can react without requiring a change
 * to the code that posted them.
 * <p>
 * Everything under {@code api} is a contract, not a feature: gameplay code
 * depends on these interfaces so a future concrete implementation can be
 * swapped in (see {@link com.descent.dungeon.hooks.DungeonHooks}) without
 * refactoring the systems that already call through them.
 */
package com.descent.dungeon.api;
