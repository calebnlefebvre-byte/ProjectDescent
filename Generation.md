# Generation

Phase 2 status: floor dimensions, themes, and staircase placement are built.
This document describes what exists, the decisions behind it, and what's
still deferred.

## Dimension architecture

**Decision: all 18 floor dimensions are statically registered, not created
dynamically at runtime.** The original plan (written during Phase 1, before
this phase's research) was to lazily create each floor's dimension the first
time it's needed. In practice, dynamic runtime dimension creation is one of
the least standardized, highest-risk corners of NeoForge modding — it means
hand-building a `LevelStem`/`ServerLevel` and inserting it into the server's
level map outside the normal datapack-driven path. Static registration
(`data/descent/dimension/floor_<n>.json`, one per floor) is the
overwhelmingly standard, well-documented approach every comparable
multi-dimension mod uses, and it costs nothing at idle: Minecraft doesn't
generate a dimension's terrain until a chunk in it actually loads, and an
unvisited floor's level file stays essentially empty. `generation.DescentDimensions`
resolves the `ResourceKey<Level>` for a floor number
(`ResourceKey.create(Registries.DIMENSION, "descent:floor_<n>")`, the exact
same pattern vanilla uses for `Level.OVERWORLD`/`Level.NETHER`/`Level.END`)
and fetches the live `ServerLevel` from the running server.

Two shared `dimension_type`s back all 18 floors:

- **`descent:cave_floor`** (12 non-town floors) — no skylight, no ceiling,
  min_y -64 / height 192 (matched to the `minecraft:caves` noise preset
  bounds, not the Overworld's, since that's the noise settings every
  non-town floor uses), beds disabled. No sleeping through the timer.
- **`descent:town_floor`** (6 town floors) — full skylight, min_y -64 /
  height 384 (matched to `minecraft:overworld` noise preset bounds), beds
  work, otherwise close to vanilla Overworld's dimension type.

## Theme → generation mapping

Every non-town theme in `FloorConfigManager.defaultFloorSchedule()` is
backed by a `minecraft:noise` generator using the `minecraft:caves` noise
preset and a `minecraft:fixed` biome source pointing at one biome. Reusing
real vanilla biome IDs (rather than hand-authoring 12 new biome files) was a
deliberate risk/value call: biome JSON is exacting (feature-step lists,
spawn tables, climate effects) and a malformed one can block a world from
loading, whereas referencing a vanilla biome ID is guaranteed valid and still
gives each floor a genuinely distinct fog color, ambient sound, and mob
list. Floor 1 (Crystal Caverns) gets one fully custom biome
(`data/descent/worldgen/biome/crystal_caverns.json`, modeled on vanilla's
`dripstone_caves` with amethyst/dripstone features and a cyan-tinted fog) as
the dungeon's flagship first impression.

| Floor | Theme | Biome used |
|---|---|---|
| 1 | Crystal Caverns | `descent:crystal_caverns` (custom) |
| 2 | Frozen Tunnels | `minecraft:frozen_peaks` |
| 4 | Volcanic Depths | `minecraft:basalt_deltas` |
| 5 | Poison Marsh | `minecraft:mangrove_swamp` |
| 7 | Ancient Fortress | `minecraft:deep_dark` |
| 8 | Spider Nest | `minecraft:dripstone_caves` |
| 10 | Underground Jungle | `minecraft:lush_caves` |
| 11 | Haunted Catacombs | `minecraft:soul_sand_valley` |
| 13 | Sunken Aqueduct | `minecraft:lukewarm_ocean` |
| 14 | Fungal Hollow | `minecraft:mushroom_fields` |
| 16 | Obsidian Rift | `minecraft:crimson_forest` |
| 17 | Bone Quarry | `minecraft:eroded_badlands` |

Town floors (3, 6, 9, 12, 15, 18) all currently share `minecraft:badlands`
over the `minecraft:overworld` noise preset. **This is a known simplification,
not the final intent** — `Architecture.md`'s aspiration is six *distinct*
settlements, not six copies of the same wasteland. Differentiating them
(different biome, different ruin dressing) is deferred to a later pass; the
important thing built now is that town floors generate as real, walkable,
sky-lit surface terrain that building/structure placement can be layered
onto later.

No custom `ChunkGenerator` was written. A hand-built noise chunk generator
(overriding surface rules, height sampling, structure placement, etc.) is
one of the highest-risk pieces of Minecraft modding to write correctly
without compiling against it, and vanilla's own noise generator, driven by
data, already gets every floor a real, distinct-feeling terrain shape at a
fraction of the risk.

## Determinism, precisely

`util.DeterministicSeed.deriveFloorSeed(worldSeed, floorNumber)` is not
plumbed into the vanilla terrain generator — Minecraft's noise generation
for a custom dimension already uses the world's actual seed for every
dimension automatically (this is exactly how the Nether and End already
work, and how our floors do too), so terrain shape is deterministic through
ordinary Minecraft behavior. Where determinism is a **gameplay** requirement
we actually control directly — staircase placement — the derived floor seed
is used explicitly: `stairs.StaircasePlacer` seeds a `RandomSource` from it,
so the same world seed always produces the same staircase layout on a given
floor. This is checked into `persistence.FloorSaveState.seed()` so it
survives even if the derivation algorithm ever changes later.

## Staircase placement

`stairs.StaircasePlacer.placeStaircases(level, floorNumber, floorSeed, count)`:

1. Seeds a `RandomSource` from the floor seed.
2. For each of the floor's configured staircases (`FloorDefinition.stairCount()`),
   samples random `(x, z)` within 400 blocks of the origin, finds the
   surface `y` via `Heightmap.Types.MOTION_BLOCKING_NO_LEAVES`, and rejects
   candidates too close to an already-placed staircase (48-block minimum
   separation) or too shallow to fit the shaft.
3. Builds a small physical structure at each accepted position: a
   polished-blackstone ring marking the entrance, a 12-step descending
   `cobbled_deepslate_stairs` shaft in a random cardinal direction, and a
   sealed landing room at the bottom.

"Hidden until discovered" (per the design document) is a knowledge/map-state
concept, not invisibility — the blocks placed here are real and walkable
immediately. `persistence.StaircaseRecord.discovered()` starts `false` for
every staircase; Phase 3's `events.StaircaseEvents` flips it on proximity
(and upgrades the entrance ring to sea lanterns as a permanent "found"
marker) and handles the right-click-to-descend interaction, including the
tribute check for descending early. Floor 18's staircase was flagged
`finalStaircase` back in this phase specifically so Phase 3 could branch
victory logic on it without re-deriving which staircase is "the" exit.

## Save/load

`persistence.DescentSavedData` (a `SavedData` attached to the Overworld,
since the dungeon spans many dimensions but there's only one Overworld)
holds, per floor: derived seed, whether staircases have been placed, and the
list of `StaircaseRecord`s. It also tracks which floor each player is
currently on. Placement is triggered the first time any player's dimension
changes onto a floor (`events.FloorGenerationEvents`, listening for
`PlayerEvent.PlayerChangedDimensionEvent`) and is idempotent — re-entering an
already-generated floor does nothing.

## Explicitly out of scope (this phase)

- No procedural infinite generation past Floor 18 — `FloorConfigManager.FLOOR_COUNT`
  (18) is a hard, non-configurable bound, and `DescentDimensions` only
  resolves dimensions for floors in that range.
- No hand-authored structures (ruined buildings, dungeon rooms) — town
  floors generate as raw wasteland terrain with no settlement dressing yet.
  Real content here is level-design work, not architecture, and belongs to a
  later pass once the underlying system (this phase) is proven out.
- Per-town-floor visual distinction — all six currently share one biome/noise
  pairing (see table above).
