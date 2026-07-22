# Changelog

All notable changes to Project Descent are documented here. Format loosely
follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [0.1.0] - 2026-07-22

Initial public release. Every core gameplay loop described in the design
document is implemented and compiles clean against NeoForge 21.4.138 /
Minecraft 1.21.4: enter a floor, loot it, find the stairs, and decide
whether to take them now or push your luck against the clock.

### Added

- **Eighteen floor dimensions.** Each of the 18 floors is its own
  statically-registered NeoForge dimension (12 cave-style, 6 town/wasteland
  -style), generated deterministically from `(world seed, floor number)` —
  same seed always produces the same floor.
- **Staircases.** Placed deterministically the first time any player enters
  a floor; physically built (entrance, descending shaft, sealed landing).
  Standing near an undiscovered staircase reveals it, world-wide, forever.
- **Floor timers and collapse.** Every floor runs a six-phase timer
  (ACTIVE → WARNING → FINAL_WARNING → COLLAPSING → FINAL_COLLAPSE → ENDED)
  driven purely by elapsed in-game time. Expiry triggers a theme-specific
  hazard sequence (explosions, fire, gas, darkness, falling debris,
  earthquake approximation) that kills anyone still on the floor once it
  ends.
- **Early Descent Tribute.** Taking the stairs before a floor's timer
  expires costs a weighted-random piece of equipment — weighting is based
  on the item's real attack damage, armor value, armor toughness, and
  mining speed (boosted for enchanted gear), so your best gear is genuinely
  what's at risk, not just whatever has the most durability left.
- **Floor themes and identity.** Each floor has a distinct theme and one
  signature mechanic (see [FloorIdentity.md](FloorIdentity.md)); collapse
  hazard odds vary by theme via `collapse.CollapseProfile`.
- **Bosses.** Six boss encounters on floors 3/6/9/12/15/18 (Zombie,
  Vindicator, Ravager, Evoker, Wither Skeleton, and a Warden for Floor 18),
  scaled with their own gentler difficulty curve so a Floor 18 boss is
  tough but killable rather than a ~34x-health wall. Force-spawnable via
  `/descent boss <floor>` for testing.
- **Loot.** Five tiered loot tables (COMMON through LEGENDARY, scaling from
  bread and iron up through netherite, elytra, and totems), rolled for
  boss drops and for a configurable number of treasure chests placed on
  every floor.
- **Mob scaling.** Naturally-spawned hostile mobs get progressively
  tougher and harder-hitting on deeper floors, via a single shared
  `DifficultyCalculator` every scaling system reads from.
- **Town floors.** Every town floor gets a small utility outpost (crafting
  table, furnace, smithing table, anvil, supply chest) so players have
  somewhere to prepare between descents.
- **Persistence.** World-scoped save data tracks per-floor seeds,
  staircase state, boss-defeated flags, and which floor each player is on
  — survives server restarts and covers multiplayer out of the box.
- **Extensive configuration.** Difficulty growth rates, timer phase
  windows, collapse duration/intensity, tribute on/off, chests per floor,
  boss health/damage bonuses, and whether debug commands require operator
  permission are all config, not constants. Per-floor overrides (timer
  length, stair count, theme, town/boss flags, floor modifiers) are
  data-pack driven and `/reload`-able.
- **Debug commands.** `/descent floor|timer|collapse|boss|generate|reload`
  and `/debug structures|seed`, gated behind `debug.commandsRequireOp`.
- **Reserved extension points**, deliberately unimplemented but with real
  seams to build against later: a Dungeon Director, an achievement system,
  a reward announcer, a full RPG framework (classes, skills, spells,
  attributes), and a `DungeonRules` challenge-mode placeholder. See
  [FutureSystems.md](FutureSystems.md).

### Known gaps (tracked, not bugs)

- Monster **count** scaling has a formula
  (`DifficultyCalculator#monsterCountMultiplier`) but no consumer yet —
  vanilla's spawn-cap internals didn't offer a low-risk seam this pass.
- No enchanted loot items yet — avoided the enchantment data-component API
  surface for this release.
- Town floors offer a generic utility room, not custom villager trades.
- This build has been compiler-verified (`./gradlew build` succeeds clean)
  but not yet playtested end-to-end in a running client/server.
