/**
 * Boss encounters, gating progression on town floors (3/6/9/12/15/18).
 * The framework is built — {@link com.descent.dungeon.bosses.IBoss},
 * {@link com.descent.dungeon.bosses.BossPhase}, {@link com.descent.dungeon.bosses.BossAbility},
 * {@link com.descent.dungeon.bosses.BossLoot}, {@link com.descent.dungeon.bosses.BossArena},
 * {@link com.descent.dungeon.bosses.BossHealthBar}, and three cooperating
 * pieces: {@link com.descent.dungeon.bosses.BossRegistry} (registration/lookup),
 * {@link com.descent.dungeon.bosses.BossFactory} (builds the actual entity),
 * and {@link com.descent.dungeon.bosses.BossSpawnController} (decides when —
 * already wired into floor generation for every boss floor). No concrete
 * boss is registered yet. Per the pre-Phase-4 design review, the framework
 * comes before content: a real boss (initially a modified vanilla mob)
 * simply calls {@code BossRegistry.register(...)} once written.
 */
package com.descent.dungeon.bosses;
