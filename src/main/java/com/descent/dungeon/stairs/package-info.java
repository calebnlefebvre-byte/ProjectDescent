/**
 * Staircase placement and discovery. Staircase count per floor is
 * data-driven (see {@link com.descent.dungeon.config.FloorDefinition#stairCount()}),
 * front-loaded on early floors and scarce by Floor 18's single final exit.
 * Discovery is permanent and per-world (not per-player): once any player
 * finds a staircase it is revealed on the dungeon map and saved forever, so
 * the remaining decision is when to descend, never where. Also owns the
 * Early Descent Tribute penalty for leaving before a floor's timer expires.
 * Built in development Phase 2 (placement/discovery) and Phase 3 (tribute).
 */
package com.descent.dungeon.stairs;
