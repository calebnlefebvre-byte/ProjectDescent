# Project Descent

A standalone Minecraft Java Edition dungeon-survival mod, built on NeoForge.
Eighteen dimensions, each its own dungeon floor. Enter, loot, find the
staircase — and decide, under a running countdown, whether to take it now or
push your luck for a deeper haul. Everything on a floor is racing toward one
of two outcomes: you find the stairs, or the floor finds you.

This is an original work. It does not reuse characters, locations, dialogue,
names, or lore from any existing game or franchise.

## Design pillars

- **Eighteen floors, no more.** No procedural infinite descent, no Floor 19.
  Reaching Floor 18 is already an accomplishment; escaping it is the game.
- **Every floor is a separate dimension**, generated deterministically from
  `(world seed, floor number)`. This buys independent timers, independent
  save data, independent themes, and no world-height stacking problems.
- **Time pressure with a real trade-off.** Each floor collapses when its
  timer runs out — but leaving early costs you a random piece of equipment
  (weighted so your best gear is what's actually at risk). Rushing is
  punished by being caught in the open; stalling is punished by the timer;
  greed is punished by the tribute.
- **Nothing about the numbers is hardcoded where it doesn't have to be.**
  Floor timers, staircase counts, themes, difficulty growth, collapse
  intensity, and tribute behavior are all configuration, not constants.

See [Architecture.md](Architecture.md) for how the codebase is organized,
[Generation.md](Generation.md) for how floors are built,
[FloorIdentity.md](FloorIdentity.md) for what makes each of the 18 floors
feel different from the last, and [FutureSystems.md](FutureSystems.md) for
what is deliberately *not* built yet but already has an extension point.

## Current status

Being built in five phases (see the project's development process); phase
completion is tracked here as it lands.

- [x] **Phase 1 — Project setup, architecture, configuration, extension interfaces.**
      Gradle/NeoForge scaffold, package layout, the common config spec, the
      data-driven per-floor config system, and every future-system interface
      (Dungeon Director, achievements, announcer, RPG framework, tribute
      weighting) with the neutral defaults gameplay code will call into from
      Phase 2 onward.
- [x] **Phase 2 — Floor generation, themes, staircase generation, save/load.**
      All 18 floors are real, statically-registered NeoForge dimensions (12
      cave-style + 6 town/wasteland-style), each paired with a vanilla or
      custom biome matching its `FloorDefinition` theme. Staircases are
      placed deterministically from a floor's derived seed the first time any
      player enters it, and physically built (marked entrance, descending
      shaft, sealed landing). World-scoped save data
      (`persistence.DescentSavedData`) persists per-floor seeds, staircases,
      and which floor each player is on. See [Generation.md](Generation.md)
      for what's built versus deferred.
- [x] **Phase 3 — Timers, collapse, staircase discovery, Early Descent Tribute.**
      Every floor's timer (`timer.FloorClock`) is a pure function of elapsed
      game time since first entry — no scheduled tasks, just a periodic tick
      check. Expiry triggers a collapse sequence (`collapse.CollapseHazards`:
      explosions, fire, poison gas, darkness, falling debris, block
      destruction, an earthquake approximation, warning messages) that kills
      anyone still on the floor once it ends. Standing near an undiscovered
      staircase permanently reveals it, world-wide. Right-clicking a
      staircase's landing descends a floor — for free once the timer's
      expired, or at the cost of a weighted-random equipment item (Early
      Descent Tribute, `stairs.DungeonTribute` + `stairs.TributeWeightRegistry`)
      if taken early. Floor 18's staircase triggers victory instead.
- [x] **Pre-Phase-4 design review — frameworks before content.** Applied ten
      architectural changes requested ahead of Phase 4: the floor timer's
      three phases became six (`ACTIVE`/`WARNING`/`FINAL_WARNING`/`COLLAPSING`/
      `FINAL_COLLAPSE`/`ENDED`), collapse hazards became theme-specific
      (`collapse.CollapseProfile`), the Early Descent Tribute formula was
      corrected to weigh real attack-damage/armor/mining stats instead of
      durability, gameplay milestones now post domain events
      (`api.events` + `hooks.HookBridgeEvents`) instead of coupling systems
      directly together, and reusable-but-currently-empty frameworks were
      built for floor modifiers (`api.modifier`), bosses (`bosses.*`), loot
      (`loot.*`), and a single shared `difficulty.DifficultyCalculator`. See
      [Architecture.md](Architecture.md)'s "Pre-Phase-4 design review"
      section for the full mapping from request to implementation.
- [x] **Second design review — registries, shared context, save versioning.**
      Split the boss and loot frameworks into a registration layer
      (`bosses.BossRegistry`, `loot.LootRegistry`) separate from the logic
      that consumes it (`bosses.BossFactory`/`BossSpawnController`,
      `loot.LootGenerator`), so an add-on only ever needs to call
      `register(...)`. Added `context.DungeonContext`, an immutable
      per-floor snapshot (level, floor number, theme, timer phase, collapse
      profile, world seed, player count, active modifiers) that
      `api.modifier.IFloorModifier` and `collapse.CollapseHazards` now
      receive instead of several separate parameters. Added a `version` tag
      to `persistence.DescentSavedData` (`SAVE_VERSION = 1`) ahead of any
      save-format change actually needing it. Added a reserved
      `api.rules.DungeonRule`/`DungeonRules` placeholder for a future
      challenge-mode rule system (no enforcement yet — a pure seam). Wrote
      [FloorIdentity.md](FloorIdentity.md), a design note giving each of the
      18 floors one signature mechanic to build Core Gameplay content
      against.
- [x] **Core Gameplay** (the design document's "Phase 4," renamed to reflect
      what it actually is — content, not architecture), built and verified
      in this order:
      1. **Loot generation** — five tiered `loot.WeightedLootTable`s
         (COMMON through LEGENDARY, real vanilla items scaling from bread
         and iron to netherite and elytra), registered at startup
         (`loot.LootTables.bootstrap()`) and rolled by
         `difficulty.DifficultyCalculator#lootTierForFloor`.
         `loot.TreasureChestPlacer` places a configurable number of filled
         chests per floor (`loot.chestsPerFloor`, default 3) alongside
         staircase placement.
      2. **Boss encounters** — six `bosses.VanillaBoss`es, one per boss
         floor, registered via `bosses.VanillaBosses.bootstrap()`: a Zombie,
         Vindicator, Ravager, Evoker, Wither Skeleton, and — for Floor 18 —
         a Warden. `events.BossEvents` recognizes a dying boss by its
         `descent_boss_<floor>` tag, drops its loot, marks
         `FloorSaveState#bossDefeated()`, and posts `BossDefeatedEvent`.
      3. **Mob scaling** — `events.MobScalingEvents` scales every hostile
         mob's health and attack damage on spawn using
         `DifficultyCalculator`'s per-floor multipliers. Monster *count*
         scaling has a formula (`DifficultyCalculator#monsterCountMultiplier`)
         but no consumer yet — flagged, not silently dropped; vanilla's
         spawn-cap internals didn't have a low-risk seam for this pass.
      4. **Town floor interactions** — `generation.TownFurnisher` builds a
         small utility outpost (crafting table, furnace, smithing table,
         anvil, a supply chest) on every town floor. A lighter answer to
         "merchants, repairs, preparation" than custom villager trades,
         chosen to avoid that API surface for now.
      5. **Difficulty tuning pass** — sanity-checking the scaling formulas
         against real numbers caught a genuine balance bug: boss mobs using
         the *full* trash-mob health/damage curve on top of a flat boss
         bonus compounded to a ~34x-base-health Floor 18 Warden. Fixed by
         giving bosses their own, gentler per-floor curve
         (`DifficultyCalculator#bossHealthMultiplier`/`bossDamageMultiplier`)
         instead of stacking the two.
- [ ] Phase 5 — Optimization, bug fixing, documentation, polish.

`mobs` and `network` still contain only a `package-info.java` beyond what
`events.MobScalingEvents` already covers for hostile-mob scaling. `floors`
also remains package-info-only — nothing yet needs a floor-lifecycle class
distinct from what `persistence.FloorSaveState` and the `events` listeners
already cover; it'll earn its contents if that stops being true. The
`/descent` and `/debug` commands are wired up and almost entirely live now —
only `/descent boss` still reports which phase it arrives in (see
[DeveloperGuide.md](DeveloperGuide.md)).

## Requirements

- **Java 21** (Mojang ships Java 21 to players; NeoForge for this Minecraft
  line targets it).
- **Minecraft 1.21.4** / **NeoForge 21.4.138.** This is the last stable,
  non-beta, Java-21 NeoForge line as of this project's scaffolding; NeoForge's
  newer calendar-versioned line (26.x) requires Java 25 and was still
  `-beta` at the time this choice was made. Bump `gradle.properties` and the
  toolchain version in `build.gradle` together if you move off this line.
- Gradle is invoked via the included wrapper (`gradlew` / `gradlew.bat`) —
  you do not need Gradle installed separately, only a Java 21 JDK on `PATH`.

> **Note on verification:** this project was scaffolded (Phases 1-3, the
> pre-Phase-4 reviews) in an environment without a local JDK, so none of
> that work was compiler-checked as it was written — only researched
> against real API sources where the risk seemed high. A JDK 21 install
> (Eclipse Temurin) later became available in that same environment, and
> `./gradlew build` was run against the code as it stood at that point: it
> failed with 17 real compiler errors (wrong packages, superseded method
> overloads, a reload-listener constructor that no longer takes a bare
> string), all fixed by reading the actual decompiled/mapped sources instead
> of guessing again, and now **builds clean** (`BUILD SUCCESSFUL`, one
> unrelated deprecation notice). Every Core Gameplay addition afterward
> (loot, bosses, mob scaling, town furnishing) was compiled the same way
> before being considered done — one more real error turned up along the way
> (`ItemLike` living in `net.minecraft.world.level`, not
> `net.minecraft.world.item`), fixed the same way. Anything added after this
> point should be compiler-checked the same way before being trusted — run
> `./gradlew build` (or `gradlew.bat build` on Windows) yourself to confirm
> before relying on new changes; the very first run on a fresh machine will
> download NeoForge, decompile Minecraft, and can take up to an hour, but
> subsequent runs are fast (well under two minutes here).

## Building & running

```
git clone <this repo>
cd ProjectDescent
./gradlew build          # compile and package
./gradlew runClient      # launch a dev client with the mod loaded
./gradlew runServer      # launch a dev dedicated server
```

## Project layout

See [Architecture.md](Architecture.md) for the full package-by-package
breakdown. At a glance:

```
src/main/java/com/descent/dungeon/
  api/           extension-point interfaces (director, achievement, reward, rpg, equipment, modifier, rules)
                 + api.events, the domain events gameplay code posts
  config/        ModConfigSpec + data-driven per-floor JSON config
  context/       DungeonContext, the shared per-floor snapshot several systems consume
  difficulty/    the one shared DifficultyCalculator every scaling system queries
  hooks/         runtime registry for the swappable extension points + the event-to-hook bridge
  commands/      /descent and /debug developer commands
  util/          small dependency-free utilities (deterministic seeding)
  floors/ generation/ collapse/ timer/ stairs/ bosses/ loot/ mobs/ network/ persistence/ events/
                 gameplay systems — see Architecture.md's package map for what's built vs. framework-only vs. not started
```

## License

`All Rights Reserved` by default (see `gradle.properties` / `mods.toml`) —
change this if you intend to distribute.
