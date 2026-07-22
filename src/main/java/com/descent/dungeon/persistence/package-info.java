/**
 * World save data: current floor per player/party, remaining timer per
 * floor, generated seeds, boss state, loot state, discovered staircases, and
 * player progress, persisted via NeoForge's {@code SavedData}. Built
 * incrementally alongside the systems that need it, starting in development
 * Phase 2 (floor/seed/staircase data) and extended in Phases 3-4 (timer,
 * boss, loot state).
 */
package com.descent.dungeon.persistence;
