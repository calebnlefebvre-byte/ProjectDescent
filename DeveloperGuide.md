# Developer Guide

## Building and running

```
./gradlew build          # compile + package the mod jar
./gradlew runClient      # dev client, mod loaded
./gradlew runServer      # dev dedicated server
./gradlew runData        # run data generators (client-side data provider), output to src/generated/resources
```

First run downloads NeoForge and decompiles Minecraft — expect this to take
a while and require a real internet connection. Subsequent runs reuse that
cache and are fast (well under two minutes as of this writing).

Requires a JDK 21 on `PATH`. Verify with `java -version` before running the
wrapper.

### If you need to verify an API detail without guessing

Once `./gradlew build` has run at least once, the exact source for every
class you're calling into is sitting on disk, decompiled and mapped with
real parameter names and Javadoc — no need to trust a wiki or a search
result summary:

- **NeoForge's own classes** (events, etc.): the sources jar at
  `~/.gradle/caches/modules-2/files-2.1/net.neoforged/neoforge/<version>/*/neoforge-<version>-sources.jar`.
  Unzip it and grep/read directly.
- **Vanilla Minecraft classes**, decompiled and mapped: `build/neoForm/neoFormJoined<version>/steps/unzipSources/unpacked/net/minecraft/...`
  — this is the actual compiled-against source, not a cache of the raw
  decompile; it's had access transformers, official mappings, and Parchment
  parameter names/Javadoc applied, so it matches what `javac` sees exactly.

This is how the 17 compile errors from the first real build here (wrong
packages for `FallingBlockEntity`/`AddReloadListenerEvent`, a
`SimpleJsonResourceReloadListener` constructor that no longer takes a bare
folder string, `EntityType.create`/`ServerPlayer.teleportTo` overloads that
don't exist the way research summaries described them, a renamed
`getMinBuildHeight` → `getMinY`) got tracked down and fixed — by reading the
real source, not by searching for a second opinion. Prefer this over
web-search-based research for anything version-sensitive when it's
available.

## Configuration

- **`config/descent-common.toml`** (generated on first run from
  `DescentCommonConfig`): difficulty growth rates, timer phase windows
  (`timer.warningWindowDays`, `timer.finalWarningWindowDays`,
  `timer.finalCollapseWindowDays`), collapse duration/intensity, tribute
  on/off, victory behavior, whether debug commands require operator
  permission. Comments in the generated file explain each value; see also
  `Architecture.md`'s "Configuration split" section.
- **`data/descent/collapse_profiles/*.json`** (data pack driven): per-theme
  weighted hazard tables. Omitted themes fall back to
  `CollapseProfileRegistry.DEFAULT_PROFILE` (uniform across all ten
  `HazardType`s). Schema:

  ```json
  {
    "id": "descent:my_theme",
    "weights": {
      "LAVA_FISSURE": 30,
      "EXPLOSION": 25,
      "FIRE": 20
    }
  }
  ```

  Any `HazardType` omitted from `weights` simply never comes up for that
  profile — the map doesn't need every type listed.
- **`data/descent/floors/*.json`** (data pack driven, reloadable with
  vanilla `/reload` or a server restart): per-floor overrides. Any subset of
  floors, any subset of fields — omitted floors keep their hardcoded
  default from `FloorConfigManager.defaultFloorSchedule()`.

  Schema (all fields required except `town_floor`/`boss_floor`/`modifiers`,
  which default to `false`/`false`/`[]`):

  ```json
  {
    "floor_number": 4,
    "timer_days": 12,
    "stair_count": 5,
    "theme": "descent:volcanic_depths",
    "town_floor": false,
    "boss_floor": false,
    "modifiers": ["descent:double_monsters"]
  }
  ```

  `modifiers` lists `IFloorModifier` IDs to run on this floor after
  generation (see "Registering framework content" below) — an ID with
  nothing registered for it is logged as a warning and skipped, not a crash.

  Floor numbers outside `1..18` are logged as a warning and ignored rather
  than crashing floor load — a bad data pack should degrade, not brick the
  server.
- **`config/descent-common.toml`**, Core Gameplay additions:
  - `loot.chestsPerFloor` — how many filled treasure chests
    `loot.TreasureChestPlacer` places per floor alongside staircase placement
    (default 3, range 0-20).
  - `bosses.healthBonus` / `bosses.damageBonus` — flat multipliers applied on
    top of `DifficultyCalculator`'s boss-specific (not the regular monster)
    per-floor curve (defaults 2.0 / 1.3, range 1-20 each). See
    `DifficultyCalculator#bossHealthMultiplier`/`bossDamageMultiplier`'s
    Javadoc for why bosses get their own gentler curve instead of reusing the
    regular monster growth rate — stacking the two compounded to a ~34x-base
    -health Floor 18 Warden before this split existed.

## Debug commands

All under `/descent` and `/debug`, gated by `debug.commandsRequireOp` (op
level 2 by default — set the config to `false` only on private test worlds).

| Command | Status | Notes |
|---|---|---|
| `/descent floor <n>` | **live** | teleports you onto floor `n`, creating its save state if needed |
| `/descent timer <n>` | **live** | reports the floor's live phase (ACTIVE/COLLAPSING/ENDED) and remaining time or collapse progress |
| `/descent collapse <n>` | **live** | force-expires the floor's timer (rewrites `startGameTime` so it's already past due) |
| `/descent boss <n>` | **live** | force-spawns floor `n`'s registered boss at a safe entry position, scaled the same way a natural boss-floor spawn would be |
| `/descent generate <n>` | **live** | places floor `n`'s staircases now if not already placed |
| `/descent reload` | **live** | prints the full effective floor schedule (defaults + data pack overrides) |
| `/debug structures` | **live** | lists staircase positions (and discovered/final-exit flags) on your current floor |
| `/debug seed <n>` | **live** | prints floor `n`'s deterministic seed derived from the current world seed |

Every `/descent`/`/debug` subcommand is now fully wired up — none are stubs.
`/descent boss <n>` fails cleanly (a chat message, not an exception) if no
boss is registered for that floor or the level isn't loaded; on success it
reports the boss's name. `/descent floor 5` is the normal way to jump
straight to a floor for testing (it uses the same
`ServerPlayer#teleportTo(ServerLevel, ...)` call the real staircase-descend
path uses), no `/execute in` workaround needed anymore.

## Testing the timer/collapse/descend loop end-to-end

```
/descent floor 1                  # jump to floor 1; staircases get placed on arrival
/debug structures                 # note a staircase's entrance position
# walk within ~5 blocks of it -> "Staircase discovered!" message, entrance blocks turn to sea lanterns
/descent collapse 1               # force floor 1's timer to have already expired (jumps straight to COLLAPSING)
# within ~1 second: "The floor is collapsing! Find a staircase — now."
# right-click the staircase's landing block (bottom of the shaft) to descend — free, since the timer already expired
```

`/descent collapse` jumps straight from ACTIVE to COLLAPSING, skipping
WARNING/FINAL_WARNING (those are only announced if the floor is actually
observed passing through them — see `timer.FloorClock`). To see the full
phase progression instead of skipping to it, just wait it out on a
short-timer floor, or lower `timer.warningWindowDays` /
`collapse.durationDays` in the config for faster testing.

To see the Early Descent Tribute instead, skip the `/descent collapse` step
and descend while the timer is still active (ACTIVE, WARNING, or
FINAL_WARNING all count) — the right-click will destroy one weighted-random
equipment item first. Weight now comes from the item's real attack damage /
armor value / armor toughness / mining speed (whichever apply), boosted for
enchanted gear, with durability only a small modifier on top — see
`stairs.TributeWeightRegistry`. A diamond sword should be dramatically more
likely to be picked than a stack of wooden hoes.

## Registering framework content (bosses, loot, floor modifiers)

Bosses and loot are now populated (`bosses.VanillaBosses.bootstrap()`, six
bosses on floors 3/6/9/12/15/18; `loot.LootTables.bootstrap()`, five tiers
COMMON-LEGENDARY) — both called from `DescentMod`'s constructor. Adding more
content follows the same registration pattern; note registration is always
on the `*Registry` class, never on the class that consumes the registry:

- **Bosses:** implement `bosses.IBoss` (or add another `VanillaBoss` entry to
  `VanillaBosses.bootstrap()` if a modified-vanilla-mob boss is all you
  need), then `BossRegistry.register(myBoss)` during mod setup.
  `events.FloorGenerationEvents` already calls
  `BossSpawnController.trySpawnBoss(...)` for every boss floor, and
  `/descent boss <n>` can force-spawn one for testing — registering is the
  only step left for a new boss floor.
- **Loot:** implement `loot.LootTable` (or add a tier/entries to
  `loot.LootTables`), then `LootRegistry.register(myTable)` (or
  `LootRegistry.register(myModifier)` for a `LootModifier`).
  `loot.TreasureChestPlacer` rolls `LootGenerator.generate(...)` for chest
  contents and `events.BossEvents` rolls it for boss drops (via `BossLoot`)
  — both already wired, so a new table just needs registering and a tier
  mapping in `DifficultyCalculator#lootTierForFloor` if it's floor-driven.
- **Floor modifiers:** implement `api.modifier.IFloorModifier` (its `apply`
  method takes a `context.DungeonContext`, not separate level/definition
  parameters), then `FloorModifierRegistry.register(myModifier)`. List its
  ID in a floor's `modifiers` array (see the Configuration section above) to
  have it run. Still unpopulated — no modifiers are registered yet.

## Coding conventions

- **Package by responsibility**, not by layer — see `Architecture.md`'s
  package map. A new gameplay concept gets its own package or a clearly
  -owned subpackage of an existing one, not a grab-bag `common`/`util` dump.
- **Interfaces in `api`, implementations elsewhere.** If you're adding
  something another system should be able to replace later, it belongs in
  `api` with a concrete default (if one is needed) living next to its real
  caller, not in `api` itself.
- **Data over constants.** Before hardcoding a number that varies by floor,
  check whether it belongs in `FloorDefinition` instead. Before hardcoding a
  global tunable, check whether it belongs in `DescentCommonConfig`.
- **Document the why, not the what** in comments; let Javadoc on public
  classes/interfaces carry the "what this is for."
- **One reload-safe source of truth.** Anything reloadable (`FloorConfigManager`,
  `CollapseProfileRegistry`) swaps its backing collection atomically
  (`volatile` reference to an immutable map) rather than mutating in place,
  so concurrent readers never see a half-applied reload.
- **Post an event, don't reach across systems.** If code in one gameplay
  package needs to react to something happening in another (an achievement
  system reacting to victory, a future Director reacting to a boss kill),
  post a domain event from `api.events` instead of calling the other system
  directly. See `Architecture.md`'s "Event-driven architecture" section.
  Direct calls are still fine *within* a system (e.g. `events.StaircaseEvents`
  calling `stairs.DungeonTribute` — tribute is stair mechanics, not a
  separate system) and for the `hooks.DungeonHooks` extension points
  themselves, which exist precisely to be called directly.
- **Difficulty numbers go through `DifficultyCalculator`.** A new system that
  scales with floor number should add a method there (or reuse an existing
  one) rather than reading `DescentCommonConfig` growth rates and computing
  `Math.pow(...)` itself.

## Phase discipline

Per the design document's development process, each phase should leave the
project compiling and the systems it introduces actually working before the
next phase starts. Do not skip ahead: Phase 3's collapse system depends on
Phase 2's floor lifecycle existing, Core Gameplay's bosses depend on Phase
2's generation existing, and so on. If you're picking this project back up,
check `README.md`'s status checklist for the last completed phase before
adding anything.
