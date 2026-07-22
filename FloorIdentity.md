# Floor Identity

A design note, not a code file: before Core Gameplay content (loot, bosses,
mob placement, town interactions) gets built out, this defines what makes
each of the 18 floors *feel* different from the one before it — one
signature mechanic per floor, on top of the theme already locked in during
Phase 2 (`Generation.md`'s theme table) and the collapse flavor already
locked in during the pre-Phase-4 review (`Architecture.md`'s
`CollapseProfile` section).

The rule for picking a signature mechanic: it should create a decision, not
just a decoration. "Slippery ice" is set dressing; "slippery ice near an
unstable ledge, with the only staircase across it" is a mechanic. Content
work in Core Gameplay (loot tables, mob placement, town dressing) should
implement floors with this table in mind rather than treating all 12
non-town floors as interchangeable cave variations.

| Floor | Theme | Signature mechanic |
|---|---|---|
| 1 | Crystal Caverns | The tutorial floor: generous timer (5d), 8 staircases, low hazard. Amethyst geodes are the first "is it worth the detour" decision. |
| 2 | Frozen Tunnels | Ice patches near ledges punish careless movement; the collapse profile leans block-collapse/debris rather than fire, so caution matters more than speed here. |
| 3 | Town (Settlement 1) | First boss, first safe zone, first "evidence of previous survivors" storytelling beat. Should read as a genuine breather after two hazard floors. |
| 4 | Volcanic Depths | Lava fissures are baked into the `CollapseProfile` already — the floor should telegraph that collapse *reshapes the terrain* here, not just spawns mobs, so players learn to fear this theme's collapse specifically before Floor 8/16 reuse similar flavor. |
| 5 | Poison Marsh | Persistent hazard pockets (not just collapse-triggered) that make the "fastest path" and "safest path" genuinely different routes. |
| 6 | Town (Settlement 2) | Second boss. Vertical settlement layout (built into a cliff/mine) — the first town that isn't flat. |
| 7 | Ancient Fortress | Puzzle/trap-corridor heavy; the first floor where combat isn't the primary threat, navigation is. |
| 8 | Spider Nest | Webs restrict movement and vision at the same time — an ambush-ecology floor where staying still is also a risk. |
| 9 | Town (Settlement 3) | Third boss. Larger, more fortified settlement — should feel like the dungeon's "midpoint city," not a repeat of Floors 3/6. |
| 10 | Underground Jungle | Dense vegetation cuts visibility; verticality (climbing growth) becomes a real path option, not just an obstacle. |
| 11 | Haunted Catacombs | Darkness/lightning/phantom collapse flavor already locked in — the floor itself should be dim even before collapse starts, so the *transition* to collapse is less jarring and the dread is already present. |
| 12 | Town (Settlement 4) | Fourth boss. First town to show visible battle damage / a losing fight in its "evidence of survivors" dressing — the tone should start turning. |
| 13 | Sunken Aqueduct | Water-based navigation with current/flow as a hazard, not just scenery — the first floor where swimming skill actually matters. |
| 14 | Fungal Hollow | Spore clouds are an area hazard independent of collapse, similar in spirit to Floor 5's marsh but vision-based instead of poison-based. |
| 15 | Town (Settlement 5) | Fifth boss. Should feel like the last "safe" moment before the dungeon stops pretending to be forgiving. |
| 16 | Obsidian Rift | Nether-adjacent instability; ties back to Floor 4's volcanic flavor but harsher — the design intent is "you thought Floor 4 was the lava floor; this is worse." |
| 17 | Bone Quarry | Exposed high ground, ranged-heavy threats, minimal cover — the mechanical opposite of Floor 8's cramped webs, deliberately, right before the final town. |
| 18 | Town (Final) | Final boss, the single final staircase, victory. No early-descent option matters here — everyone commits to the same ending. |

## What this document does *not* do

It does not specify loot tables, mob lists, boss stats, or structure
layouts — those are Core Gameplay content decisions that should reference
this table for tone, not be blocked on it. It also does not revisit the
theme or collapse-profile assignments already locked in; this is additive
context for content work, not a request to change Phase 2/pre-Phase-4
decisions.

When implementing a floor, the concrete test is: **does this floor's
signature mechanic create a decision the last floor didn't?** If two
adjacent non-town floors would play identically with the mechanic
descriptions swapped, that's the sign to revisit one of them before writing
more content against it.
