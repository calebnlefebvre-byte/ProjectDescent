/**
 * The floor collapse sequence: once a floor's timer (see
 * {@link com.descent.dungeon.timer}) expires, this package runs the
 * roughly one-Minecraft-day collapse — explosions, falling ceiling, fire,
 * lava fissures, poison gas, earthquakes, darkness, lightning, phantom
 * attacks, block destruction, warning sounds/visuals — and kills players
 * still present when it ends. Which hazards a floor favors is
 * {@link com.descent.dungeon.collapse.CollapseProfile}'s job (see
 * {@link com.descent.dungeon.collapse.CollapseProfileRegistry#forTheme}); how
 * often they strike is scaled by
 * {@link com.descent.dungeon.difficulty.DifficultyCalculator#collapseIntensityMultiplier}.
 * Built in development Phase 3; profiles added in the pre-Phase-4 design
 * review.
 */
package com.descent.dungeon.collapse;
