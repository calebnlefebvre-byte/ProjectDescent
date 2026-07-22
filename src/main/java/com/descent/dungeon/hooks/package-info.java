/**
 * The runtime registry ({@link com.descent.dungeon.hooks.DungeonHooks}) that
 * holds the currently-installed implementation of each {@code api} extension
 * point, defaulting to the no-op {@code Null*} implementations, plus
 * {@link com.descent.dungeon.hooks.HookBridgeEvents}, the one place that
 * translates posted {@code api.events} into calls on those extension points
 * (a player's victory becomes an achievement grant and an announcement, for
 * instance) so gameplay code never needs to know the hooks exist at all.
 */
package com.descent.dungeon.hooks;
