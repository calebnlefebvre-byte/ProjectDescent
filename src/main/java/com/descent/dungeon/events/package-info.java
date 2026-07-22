/**
 * NeoForge event listeners that glue gameplay systems to the game loop
 * (server tick, player join/leave, dimension change, death). Kept separate
 * from the systems themselves so e.g. {@link com.descent.dungeon.timer} stays
 * testable without a live event bus. Populated incrementally from Phase 2
 * onward as each system needs a hook into the game loop.
 */
package com.descent.dungeon.events;
