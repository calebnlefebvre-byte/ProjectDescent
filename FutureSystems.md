# Future Systems

These systems are explicitly **not implemented**. Per the design document,
Phase 1 builds only their extension points — interfaces and, where a default
is needed at runtime, a no-op implementation — so that building the real
thing later is an addition, not a refactor. This document exists so nobody
mistakes "there's an interface for it" as "it's built."

## Dungeon Director

**Interfaces:** `api.director.IDungeonDirector` · **Default:**
`api.director.NullDungeonDirector` (installed in `hooks.DungeonHooks` by
default) · **Not built:** any actual director logic.

The intended shape: an adaptive system that watches how a floor is going
(time remaining, players present, deaths, hesitation at a discovered
staircase) and can respond with dynamic encounters, difficulty nudges,
environmental events, spawn manipulation, or "psychological pressure" (sound
cues, lighting changes, false alarms) based on player analysis. None of that
exists — `NullDungeonDirector` returns neutral values (`difficultyMultiplier`
= 1.0, `shouldTriggerEnvironmentalEvent` = false) and otherwise does nothing.

**To build it later:** implement `IDungeonDirector`, call
`DungeonHooks.setDirector(...)` during mod setup. Every call site that should
consult the Director (floor start/tick/end, difficulty scaling, hazard
triggers) already calls through `DungeonHooks.director()` from whichever
phase introduces that call site — check `Architecture.md`'s package map for
which phase owns each.

## Achievement & Reward System

**Interfaces:** `api.achievement.IAchievementSystem`,
`api.reward.IRewardAnnouncer` · **Defaults:** `NullAchievementSystem`,
`NullRewardAnnouncer` · **Not built:** achievements, loot boxes, sponsor
gifts, "crowd popularity," statistics tracking, or any announcement
delivery.

The intended shape: gameplay milestones (floor cleared, boss defeated, first
blood on a floor, full dungeon completion) get reported through
`IAchievementSystem.grant(player, achievementId)` regardless of whether
anything is listening. A separate `IRewardAnnouncer` decouples "something
happened" from "how it's broadcast" (chat, title card, a future overlay,
whatever), because milestone detection and announcement presentation are
different concerns with different iteration speed.

**To build it later:** define concrete `ResourceLocation` achievement IDs
(none exist yet — there is no achievement registry, just the interface),
implement both interfaces, install via `DungeonHooks.setAchievements(...)` /
`.setAnnouncer(...)`.

## RPG Framework

**Interfaces:** `api.rpg.IPlayerClass`, `IExperienceProvider`,
`IAttributeProvider`, `IResourcePool`, `ISpellSystem`, `ISkill`,
`ITalentTree`, `IStatusEffectProvider` · **Defaults:** none — nothing calls
these yet, so no `Null*` is needed until a caller exists · **Not built:**
classes, experience, levels, attributes, mana, stamina, spells, cooldowns,
passive/active skills, status effects, equipment bonuses, or talent trees.

Design notes for when this is built:
- Mana and stamina are the same shape (a regenerating pool with a current and
  max value, consumed by actions) — both should be `IResourcePool` instances
  distinguished by a `poolId`, not two parallel interfaces.
- `ISkill` covers both passive and active skills via `ISkill.Type`, again to
  avoid two near-identical interfaces.
- None of Phases 2-4 (floors, generation, timers, collapse, loot, bosses,
  multiplayer) depend on the RPG framework existing. It is additive: base
  equipment durability (Phase 1 onward) works exactly like vanilla until
  attribute/class bonuses are layered on top later.

## Artifact Equipment

**Interface:** `loot.ArtifactPlaceholder` — a bare marker (just an `id()`),
added as part of the pre-Phase-4 loot framework so there's a named seam
rather than nothing. **Not built:** any artifact item, indestructible-
equipment behavior, unique-ability system, or anything that actually
implements the marker yet.

The design document is explicit that crafted equipment behaves like normal
Minecraft equipment (loses durability, breaks) for now, and that Artifact
equipment (never breaks, unique abilities, awarded through achievements/
bosses/quests/milestones) is reserved architecture only. When this is built,
it should integrate with `api.equipment.TributeWeighted` like any other
equipment — an Artifact with high tribute weight is exactly the kind of item
the tribute system exists to threaten, which is part of what should make
losing one to an early descent sting.
