/**
 * Floor lifecycle: entering, tracking, and leaving one of the 18 dungeon
 * floors. Owns the per-floor save data (current occupants, state flags) that
 * ties together {@link com.descent.dungeon.generation}, {@link com.descent.dungeon.timer},
 * and {@link com.descent.dungeon.stairs}. Built in development Phase 2.
 */
package com.descent.dungeon.floors;
