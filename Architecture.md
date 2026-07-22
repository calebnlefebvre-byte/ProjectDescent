# Architecture

## Principles

- **Composition over inheritance.** Systems talk to each other through small
  interfaces (`api/`), not shared base classes.
- **Small classes, one job each.** A class that both derives a seed and
  places blocks is two classes wearing a trenchcoat.
- **No duplicate logic.** Where two things are structurally the same thing
  (mana and stamina, for instance — see `IResourcePool`), they share one
  contract instead of two near-identical ones.
- **Config over constants.** If the design document calls a number a
  "default" or says a system should "use formulas instead of hardcoded
  values," it lives in `config/`, not as a `static final` buried in gameplay
  code.
- **Extension points are real interfaces, not TODOs.** Every future system
  named in the design document (Dungeon Director, achievements, RPG
  framework) has an interface today, even though nothing but a no-op
  implements it yet. Gameplay code added in later phases calls through
  `hooks.DungeonHooks`, never a concrete class, so a real implementation can
  replace the no-op without touching the caller.

## Package map

| Package | Responsibility | Phase |
|---|---|---|
| `api.director` | `IDungeonDirector` extension point + `NullDungeonDirector` | 1 |
| `api.achievement` | `IAchievementSystem` extension point + `NullAchievementSystem` | 1 |
| `api.reward` | `IRewardAnnouncer` extension point + `NullRewardAnnouncer` | 1 |
| `api.rpg` | RPG framework interfaces (class, XP, attributes, resource pools, spells, skills, talent trees, status effects) — no implementations | 1 |
| `api.equipment` | `TributeWeighted`, the interface custom equipment implements to define its own tribute weight | 1 |
| `api.modifier` | **Built (framework only).** `IFloorModifier` — the "double monsters / darkness / blessed floor" extension point, now taking a `context.DungeonContext`. No modifier implemented yet. | pre-4 |
| `api.rules` | **Built (placeholder only).** `DungeonRule` enum + `DungeonRules` holder for a future challenge-mode rule system (no block placement, keep inventory, etc). Nothing enforces any rule yet. | pre-4 (2nd review) |
| `api.events` | **Built.** Domain events (`PlayerDescendedFloorEvent`, `FloorDiscoveredEvent`, `TimerExpiredEvent`, `CollapseStartedEvent`, `CollapseEndedEvent`, `TributeTakenEvent`, `BossDefeatedEvent`, `PlayerVictoryEvent`) posted on `NeoForge.EVENT_BUS`. | pre-4 |
| `config` | `DescentCommonConfig` (global formula inputs, `ModConfigSpec`-backed) and `FloorDefinition`/`FloorConfigManager` (data-driven per-floor schedule, now including a `modifiers` list) | 1 |
| `context` | **Built.** `DungeonContext` — the shared immutable per-floor snapshot (level, floor, theme, timer phase, collapse profile, seed, player count, active modifiers). Built by `events.FloorGenerationEvents`/`FloorTimerEvents`, consumed by `IFloorModifier` and `collapse.CollapseHazards`. | pre-4 (2nd review) |
| `difficulty` | **Built.** `DifficultyCalculator` — the one shared formula service every scaling system queries. | pre-4 |
| `hooks` | **Built.** `DungeonHooks` (installed `api.*` implementation per extension point) + `HookBridgeEvents` (translates `api.events` into hook calls). | 1 (bridge: pre-4) |
| `commands` | `/descent` and `/debug` developer command tree | 1 (stubs), filled in per-phase |
| `util` | Dependency-free helpers; currently `DeterministicSeed` | 1 |
| `floors` | Floor lifecycle: enter/track/leave, tying generation + timer + stairs together — still nothing to tie together that `persistence.FloorSaveState` + `events` don't already cover | — |
| `generation` | **Built.** `DescentDimensions` resolves each floor's `ResourceKey<Level>`/`ServerLevel`, plus a safe entry position. `FloorModifierRegistry` runs the (currently empty) modifier pipeline over a `DungeonContext`. Dimension jsons + biomes live in resources, not Java. | 2 (modifiers: pre-4) |
| `persistence` | **Built.** `DescentSavedData` (now with a `version` tag, `SAVE_VERSION = 1`) + `FloorSaveState` (seed, staircases, a `Set<FloorClock.Phase>` of reacted-to milestones, boss-defeated flag) + `StaircaseRecord`. Loot state still to come. | 2-3 (extended pre-4) |
| `timer` | **Built.** `FloorClock` — pure phase/progress math across six phases (ACTIVE/WARNING/FINAL_WARNING/COLLAPSING/FINAL_COLLAPSE/ENDED) from elapsed game time. No mutable state of its own. | 3 (expanded pre-4) |
| `collapse` | **Built.** `CollapseHazards` (ten hazard types, now taking a `DungeonContext`) + `CollapseProfile`/`CollapseProfileRegistry` (theme-keyed weighted hazard tables, three examples + data-pack extensible), called from `events.FloorTimerEvents`. | 3 (profiles: pre-4) |
| `stairs` | **Built.** `StaircasePlacer` (placement + discovery marker upgrade), `TributeWeightRegistry` (combat/tool-stat-based weighting) + `DungeonTribute` (Early Descent Tribute). | 2-3 (tribute formula: pre-4) |
| `bosses` | **Built, with content.** `IBoss`/`BossPhase`/`BossAbility`/`BossLoot`/`BossArena`/`BossHealthBar` framework; `BossRegistry`/`BossFactory`/`BossSpawnController` registry split; `VanillaBoss` (a modified-vanilla-mob `IBoss` implementation) + `VanillaBossLoot`, with six instances registered by `VanillaBosses.bootstrap()` — one per boss floor. | pre-4 framework, Core Gameplay content |
| `loot` | **Built, with content.** `LootTier`/`LootTable`/`LootModifier`/`LootRegistry`/`LootGenerator`/`ChestGenerator`/`ArtifactPlaceholder` framework; `WeightedLootTable` (the one concrete `LootTable`) + `LootEntry` + `TreasureChestPlacer`, with five tiered tables registered by `LootTables.bootstrap()`. | pre-4 framework, Core Gameplay content |
| `mobs` | **Partially built.** Hostile-mob health/damage scaling lives in `events.MobScalingEvents` rather than here (it's a listener, not standalone placement logic) — see that class. Monster *count* scaling and any dedicated placement logic remain unbuilt. | Core Gameplay (partial) |
| `network` | Multiplayer sync payloads | 4 |
| `events` | **Built.** `FloorGenerationEvents` (placement + modifiers + boss-spawn + chest/town-furnishing trigger), `FloorTimerEvents` (timer/collapse tick), `StaircaseEvents` (discovery, descend, tribute, victory), `BossEvents` (death -> loot/flag/event), `MobScalingEvents` (hostile mob health/damage on spawn) — all now post `api.events` at the relevant moments. | 2-4, Core Gameplay |

## Configuration split

Two different shapes of "configurable" exist in the design document, so they
get two different mechanisms:

1. **Global formula inputs** — a handful of numbers that apply uniformly
   across all 18 floors (health/damage/count growth rates per floor,
   collapse duration and hazard intensity, whether tribute is enabled at
   all, whether victory returns to the Overworld, whether debug commands
   require op). These are server-owner-editable via NeoForge's
   `ModConfigSpec`, which generates a commented `.toml` file:
   `DescentCommonConfig`.
2. **Per-floor data** — eighteen distinct timer lengths, staircase counts,
   themes, and town/boss flags. This is naturally data, not a dozen parallel
   config lists, so it is data-pack-driven JSON
   (`data/descent/floors/*.json`) decoded through a `Codec<FloorDefinition>`
   and merged over hardcoded defaults on every resource reload:
   `FloorConfigManager`. A data pack (or a future in-game floor editor) can
   override any subset of floors without redefining all eighteen.

Both are reloadable without a restart-required flag; `FloorConfigManager`
swaps its backing map atomically so readers never see a half-updated
schedule.

## Extension-point pattern

```
gameplay code  ---calls--->  DungeonHooks.director()/.achievements()/.announcer()
                                   |
                                   v
                         installed IDungeonDirector / IAchievementSystem / IRewardAnnouncer
                                   |
                          defaults to Null*Instance (no-op)
```

Replacing a no-op is one call — `DungeonHooks.setDirector(new RealDirector())`
— made once during mod setup, by this mod or an add-on. No gameplay call site
needs to change. This is deliberately *not* a NeoForge capability or a
service-loader auto-discovery mechanism: the design document's future systems
(Director, achievements, RPG framework) are large enough that auto-wiring
would hide who's actually responsible for installing them.

## Event-driven architecture

Gameplay code posts a plain NeoForge `Event` (see `api.events`) at every
milestone a future system might care about, instead of calling that future
system directly:

```
events.StaircaseEvents / events.FloorTimerEvents
        |  NeoForge.EVENT_BUS.post(new SomeEvent(...))
        v
NeoForge.EVENT_BUS
        |  addListener(...)
        v
hooks.HookBridgeEvents  (today's only internal subscriber)
        |  DungeonHooks.achievements().grant(...) / .announcer().announceServerWide(...)
        v
installed IAchievementSystem / IRewardAnnouncer
```

Reusing `NeoForge.EVENT_BUS` — rather than inventing a second pub/sub
mechanism — was a deliberate choice: the mod already depends on it for
config reload, command registration, and every other listener, so a second
event bus would be new infrastructure with no corresponding benefit. Posting
is intentionally *not* the same as calling `DungeonHooks` directly: a future
Dungeon Director, an achievement/sponsor/analytics add-on, or another mod
entirely can all add a listener to e.g. `PlayerVictoryEvent` without
`events.StaircaseEvents` — or anything it depends on — ever being touched
again. `hooks.HookBridgeEvents` is today's one example of that pattern in
use (victory and boss-defeated events become achievement grants and
server-wide announcements); most posted events currently have no internal
listener at all, which is fine — that's exactly the "future systems
subscribe without modifying existing code" property this exists for.

## Deterministic generation contract

`util.DeterministicSeed.deriveFloorSeed(worldSeed, floorNumber)` is the one
and only place a floor seed is derived, using a SplitMix64-style mix so
adjacent floor numbers don't produce correlated seeds. `events.FloorGenerationEvents`
derives it once per floor (on first entry) and hands it to `stairs.StaircasePlacer`;
the value is then cached in `persistence.FloorSaveState.seed()` rather than
re-derived on every load. Anything else that wants a stable
per-floor-but-different sub-seed (loot rolls, mob placement) should derive
its own value from the cached floor seed rather than calling
`deriveFloorSeed` again with a different input, so there is exactly one
seed-derivation algorithm to reason about. `/debug seed <floor>` exposes this
directly for verification against a known world seed. See
[Generation.md](Generation.md) for why terrain *shape* doesn't need this
value — Minecraft's own noise generator already keys off the real world seed
for every dimension, custom ones included.

## Commands

`commands.DescentCommands` registers the full `/descent` and `/debug` tree in
Phase 1 so later phases only fill in a handler body — registration,
permission gating (`debug.commandsRequireOp`), and argument parsing never
need to change again. Every subcommand is live now except `/descent boss`
(Phase 4). `/descent floor` teleports via `ServerPlayer#teleportTo(ServerLevel, ...)`
— once Phase 2's research turned up that this simple overload still exists
in 1.21.x, the earlier concern about `Entity#changeDimension(DimensionTransition)`
turned out not to apply to normal mod code at all.

## Timer/collapse/descend flow

```
FloorGenerationEvents (player first enters floor N)
        |  generate floor -> apply floor modifiers (FloorModifierRegistry, currently a no-op loop)
        |  -> try spawn boss (BossSpawnController, currently a no-op) -> finalize floor
        v
persistence.FloorSaveState{startGameTime = floor's current game time}
        |
        v
FloorTimerEvents (every 20 ticks, per started floor) --> timer.FloorClock.compute(...)
        |
        | ACTIVE ---------> WARNING ---------> FINAL_WARNING ---------> COLLAPSING ---------> FINAL_COLLAPSE ---------> ENDED
        |  (nothing)     one-shot low-      one-shot urgent      one-shot "collapse       one-shot "peak"       one-shot: kill every
        |                urgency warning    warning              begun" announcement,     announcement, hazard  player still on the
        |                                                         + TimerExpiredEvent,     rate boosted 1.5x     floor, + CollapseEndedEvent
        |                                                         CollapseStartedEvent
        |
        +-- while COLLAPSING or FINAL_COLLAPSE: collapse.CollapseHazards.strike(...) per player, per check,
            at a rate from DifficultyCalculator.collapseIntensityMultiplier(floor) x collapseProgress(),
            picking a hazard from CollapseProfileRegistry.forTheme(floor's theme)

Meanwhile, independently:
StaircaseEvents (every 20 ticks) --> proximity check --> flip StaircaseRecord.discovered,
                                      upgrade entrance blocks, announce (world-shared), + FloorDiscoveredEvent
StaircaseEvents (right-click landing block) --> if isEarlyDescent(): stairs.DungeonTribute.apply(player) + TributeTakenEvent
                                              --> if finalStaircase: victory (+ PlayerVictoryEvent)
                                                  else: teleport to floor N+1 (+ PlayerDescendedFloorEvent)
```

Everything above keys off `FloorClock.compute(timerDays, startGameTime, currentGameTime)`
being a pure function — no per-tick state machine, so a floor with nobody on
it still "collapses on schedule" the moment someone checks it again, exactly
as the design document intends ("the timer begins immediately upon entering
the floor"). The six-phase list is itself data — see `timer.FloorClock`'s
`PhaseBoundary` list — so a seventh phase is one more list entry, not a
rewrite of the lookup logic.

## Pre-Phase-4 design review

Before starting Phase 4's content, a design review asked for ten specific
architectural changes so later phases wouldn't need to rewrite earlier ones.
What each turned into:

1. **Kept `FloorClock` as-is** — elapsed-game-time, no scheduled tasks.
2. **Expanded the state machine** to six phases via a sorted `PhaseBoundary`
   list (see above) instead of an if/else chain.
3. **`CollapseProfile`** — theme-keyed weighted hazard tables (10 hazard
   types now, up from 7), three worked examples, data-pack extensible.
4. **Fixed tribute weighting** — `stairs.TributeWeightRegistry` now reads
   real attack-damage/armor/armor-toughness attribute modifiers and mining
   speed off the item, per the design document's original "overall value"
   formula, with enchantment as a proportional bonus and durability as a
   small capped modifier only. (Phase 3 had shipped a durability-only
   approximation, flagged at the time as worth revisiting — this is that
   revisit.)
5. **Event-driven architecture** — `api.events` + `hooks.HookBridgeEvents`,
   described above.
6. **`IFloorModifier` framework** — `api.modifier` + `generation.FloorModifierRegistry`,
   wired into the generation pipeline, no modifier implemented yet.
7. **Boss framework first** — `bosses.*` (see package map), wired into
   `events.FloorGenerationEvents` for every boss floor, no boss registered
   yet.
8. **Loot framework first** — `loot.*` (see package map), including the
   reserved `ArtifactPlaceholder` seam, no table registered yet.
9. **Centralized `DifficultyCalculator`** — one formula per scaling axis,
   read from `DescentCommonConfig`; `collapse` already queries it, `mobs`/
   `loot` will when they're built.
10. **Kept avoiding custom mobs** — `bosses.IBoss#entityType()` names a
    vanilla `EntityType`; nothing here introduces a custom entity class.

## Second design review (post-review additions)

A follow-up review of the above, done before starting Core Gameplay content,
asked for four more additions:

1. **Registry layer.** `bosses.BossSpawnController` and `loot.LootGenerator`
   each used to hold their own registration map directly. Split into
   `bosses.BossRegistry`/`loot.LootRegistry` (registration/lookup only) plus
   `bosses.BossFactory` (builds the actual entity) and the now-thinner
   `BossSpawnController`/`LootGenerator` (decide when / roll + modify). A
   future datapack, expansion, or add-on registering content only ever calls
   `register(...)` on the registry half.
2. **`context.DungeonContext`.** An immutable per-floor snapshot (level,
   floor number, theme, timer phase, collapse profile, world seed, player
   count, active modifiers), built once per floor generation / per collapse
   tick and passed to `IFloorModifier.apply` and `CollapseHazards.strike`
   instead of each taking its own subset of parameters. Deliberately
   doesn't cache difficulty multipliers — those still come from
   `DifficultyCalculator` on demand, computed from `floorNumber()`, so
   there's never a stale value sitting in a context object. Kept distinct
   from `api.director.DungeonDirectorContext` (narrower, Director-specific,
   predates this) rather than merging the two and breaking Phase 1's
   interface for no functional gain.
3. **Save versioning.** `persistence.DescentSavedData` now writes a
   `version` tag (`SAVE_VERSION = 1`); `load` warns (but still loads) if a
   save's version is newer than the running build supports. No migration
   logic exists yet — there's been exactly one save format — but the tag is
   there from the start rather than retrofitted after the first format
   change forces the issue.
4. **`api.rules.DungeonRule`/`DungeonRules`.** A reserved placeholder for a
   future challenge-mode rule system (no block placement, no water buckets,
   keep inventory disabled, etc), explicitly distinct from
   `IFloorModifier`: a modifier is a per-floor behavior with code attached;
   a rule is a global, static constraint other systems would check against.
   Nothing enforces any rule yet — pure seam, per the request.

Also produced: [FloorIdentity.md](FloorIdentity.md), a design note (not
code) giving each of the 18 floors one signature mechanic, meant to guide
Core Gameplay content so floors read as distinct rather than as
interchangeable theme reskins.

## Core Gameplay

The frameworks above got their first real content, built and compiler-verified
in the order the second review specified:

- **Loot** — `loot.LootTables.bootstrap()` registers five `WeightedLootTable`s
  (COMMON through LEGENDARY, real vanilla items), and `loot.TreasureChestPlacer`
  places `config.chestsPerFloor` filled chests per floor using
  `DifficultyCalculator#lootTierForFloor`. No enchanted stacks — applying
  random enchantments at runtime is its own version-sensitive data-component
  API this pass didn't take on; plain items still deliver a real
  floor-to-floor progression.
- **Bosses** — `bosses.VanillaBosses.bootstrap()` registers six
  `VanillaBoss`es (one per boss floor), each a scaled, renamed vanilla mob:
  Zombie, Vindicator, Ravager, Evoker, Wither Skeleton, and a Warden for
  Floor 18. `events.BossEvents` recognizes a dying boss via a
  `descent_boss_<floor>` entity tag (set in `VanillaBoss#configure`), drops
  its `BossLoot`, flips `FloorSaveState#bossDefeated()`, and posts
  `BossDefeatedEvent` — which `hooks.HookBridgeEvents` already turns into an
  achievement grant and announcement, unchanged from the earlier review.
- **Mob scaling** — `events.MobScalingEvents` listens for
  `FinalizeSpawnEvent` (the natural-spawn pipeline; boss mobs bypass it since
  they're placed directly, so there's no double-scaling) and multiplies
  every hostile mob's max health and attack damage by that floor's
  `DifficultyCalculator` multipliers. Monster count scaling has a formula
  but no consumer — flagged in the package map rather than silently dropped.
- **Town furnishing** — `generation.TownFurnisher` builds a small utility
  room (crafting table, furnace, smithing table, anvil, a supply chest) on
  every town floor, called from `FloorGenerationEvents` right alongside
  staircase and chest placement. A deliberately modest answer to "merchants,
  repairs, preparation" — full villager trade customization was judged not
  worth its own API risk for this pass.
- **Difficulty tuning** — sanity-checking the formulas against real numbers
  (not empirical playtesting, which isn't possible in this environment)
  caught a genuine bug: `VanillaBoss` originally multiplied the *full*
  trash-mob health curve (`monsterHealthMultiplier`) by a flat boss bonus,
  which compounds to roughly 34x a Warden's base health by Floor 18 —
  arguably unkillable. Fixed by giving bosses their own curve
  (`DifficultyCalculator#bossHealthMultiplier`/`bossDamageMultiplier`, half
  the normal per-floor growth rate) instead of stacking two multipliers
  meant for different things. This is the kind of thing a compiler can't
  catch but arithmetic can — worth doing explicitly rather than trusting a
  formula because it typechecks.

Every file in this pass was compiled against the real NeoForge/vanilla
sources (see `DeveloperGuide.md`'s note on where those live), not just
written and hoped for. One real error turned up this round: `ItemLike` lives
in `net.minecraft.world.level`, not `net.minecraft.world.item`.
