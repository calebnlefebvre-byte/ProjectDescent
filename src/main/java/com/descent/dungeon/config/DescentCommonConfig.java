package com.descent.dungeon.config;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Global tunables that are not naturally per-floor. Per-floor values (timer
 * length, staircase count, theme, town/boss flag) live in data-driven JSON —
 * see {@link FloorDefinition} and {@link FloorConfigManager} — because they
 * describe eighteen distinct, hand-tunable floors rather than a single knob.
 * <p>
 * Everything here is deliberately a formula input rather than a hardcoded
 * value, per the design requirement that difficulty scaling, collapse
 * behavior, and tribute weighting all be driven by configuration rather than
 * code constants.
 */
@EventBusSubscriber(modid = com.descent.dungeon.DescentMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class DescentCommonConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // --- Difficulty scaling -------------------------------------------------

    private static final ModConfigSpec.DoubleValue MONSTER_HEALTH_GROWTH_PER_FLOOR = BUILDER
            .comment("Multiplicative monster health growth applied per floor number.",
                    "Effective health multiplier = (1 + this) ^ (floorNumber - 1).")
            .defineInRange("difficulty.monsterHealthGrowthPerFloor", 0.12, 0.0, 5.0);

    private static final ModConfigSpec.DoubleValue MONSTER_DAMAGE_GROWTH_PER_FLOOR = BUILDER
            .comment("Multiplicative monster damage growth applied per floor number.",
                    "Effective damage multiplier = (1 + this) ^ (floorNumber - 1).")
            .defineInRange("difficulty.monsterDamageGrowthPerFloor", 0.08, 0.0, 5.0);

    private static final ModConfigSpec.DoubleValue MONSTER_COUNT_GROWTH_PER_FLOOR = BUILDER
            .comment("Multiplicative spawn-count growth applied per floor number.")
            .defineInRange("difficulty.monsterCountGrowthPerFloor", 0.10, 0.0, 5.0);

    private static final ModConfigSpec.DoubleValue TRAP_DENSITY_GROWTH_PER_FLOOR = BUILDER
            .comment("Multiplicative trap/hazard density growth applied per floor number.")
            .defineInRange("difficulty.trapDensityGrowthPerFloor", 0.10, 0.0, 5.0);

    private static final ModConfigSpec.DoubleValue RESOURCE_SCARCITY_GROWTH_PER_FLOOR = BUILDER
            .comment("How much scarcer generated loot becomes per floor (higher = scarcer).")
            .defineInRange("difficulty.resourceScarcityGrowthPerFloor", 0.05, 0.0, 5.0);

    // --- Timer phases -----------------------------------------------------------
    // See timer.FloorClock: these carve the ACTIVE window into ACTIVE -> WARNING ->
    // FINAL_WARNING, and the collapse window into COLLAPSING -> FINAL_COLLAPSE.
    // Expressed as "how long before the next boundary," not absolute points, so they
    // scale automatically with each floor's own configured timer length.

    private static final ModConfigSpec.DoubleValue WARNING_WINDOW_DAYS = BUILDER
            .comment("How long before a floor's timer expires the WARNING phase begins.")
            .defineInRange("timer.warningWindowDays", 2.0, 0.0, 100.0);

    private static final ModConfigSpec.DoubleValue FINAL_WARNING_WINDOW_DAYS = BUILDER
            .comment("How long before a floor's timer expires the more urgent FINAL_WARNING phase begins.",
                    "Must be smaller than warningWindowDays to actually sit inside it.")
            .defineInRange("timer.finalWarningWindowDays", 0.5, 0.0, 100.0);

    private static final ModConfigSpec.DoubleValue FINAL_COLLAPSE_WINDOW_DAYS = BUILDER
            .comment("How long before a floor's collapse fully ends the more intense FINAL_COLLAPSE phase begins.")
            .defineInRange("timer.finalCollapseWindowDays", 0.25, 0.0, 100.0);

    // --- Collapse -------------------------------------------------------------

    private static final ModConfigSpec.DoubleValue COLLAPSE_DURATION_DAYS = BUILDER
            .comment("Length of the collapse sequence in Minecraft days once a floor's timer expires.")
            .defineInRange("collapse.durationDays", 1.0, 0.05, 30.0);

    private static final ModConfigSpec.DoubleValue COLLAPSE_HAZARD_INTENSITY = BUILDER
            .comment("Global multiplier on collapse hazard frequency/severity (explosions, falling",
                    "debris, fire, lava, gas, quakes). 1.0 = designed default.")
            .defineInRange("collapse.hazardIntensity", 1.0, 0.0, 10.0);

    // --- Early Descent Tribute --------------------------------------------------

    private static final ModConfigSpec.BooleanValue TRIBUTE_ENABLED = BUILDER
            .comment("Whether descending before a floor's timer expires destroys a random equipment item",
                    "(Dungeon Tribute). Disabling this removes the early-descent penalty entirely.")
            .define("tribute.enabled", true);

    // --- Victory ----------------------------------------------------------------

    private static final ModConfigSpec.BooleanValue RETURN_TO_OVERWORLD_ON_VICTORY = BUILDER
            .comment("Whether completing Floor 18 returns the player to the Overworld.",
                    "If false, a future extension point may define an alternate destination.")
            .define("victory.returnToOverworld", true);

    // --- Loot ---------------------------------------------------------------------

    private static final ModConfigSpec.IntValue CHESTS_PER_FLOOR = BUILDER
            .comment("How many treasure chests are placed per floor, filled from the tier",
                    "difficulty.DifficultyCalculator#lootTierForFloor assigns that floor.")
            .defineInRange("loot.chestsPerFloor", 3, 0, 20);

    // --- Bosses ---------------------------------------------------------------------
    // Bosses use their own, gentler per-floor growth curve (difficulty.DifficultyCalculator#bossHealthMultiplier),
    // not the full trash-mob curve — a hand-picked tanky mob doesn't need the same
    // exponential growth a common spawn does, and stacking both compounds into
    // absurd numbers by Floor 18. These two values are just the flat "this is a boss" bonus.

    private static final ModConfigSpec.DoubleValue BOSS_HEALTH_BONUS = BUILDER
            .comment("Flat health bonus for boss mobs, applied on top of a gentler boss-specific floor curve",
                    "(not the full monster health curve — see DifficultyCalculator#bossHealthMultiplier).")
            .defineInRange("bosses.healthBonus", 2.0, 1.0, 20.0);

    private static final ModConfigSpec.DoubleValue BOSS_DAMAGE_BONUS = BUILDER
            .comment("Flat damage bonus for boss mobs, applied on top of a gentler boss-specific floor curve.")
            .defineInRange("bosses.damageBonus", 1.3, 1.0, 20.0);

    // --- Debug ------------------------------------------------------------------

    private static final ModConfigSpec.BooleanValue DEBUG_COMMANDS_REQUIRE_OP = BUILDER
            .comment("Whether /descent and /debug developer commands require operator permission (level 2).",
                    "Only disable this on private test worlds.")
            .define("debug.commandsRequireOp", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static double monsterHealthGrowthPerFloor;
    public static double monsterDamageGrowthPerFloor;
    public static double monsterCountGrowthPerFloor;
    public static double trapDensityGrowthPerFloor;
    public static double resourceScarcityGrowthPerFloor;
    public static double warningWindowDays;
    public static double finalWarningWindowDays;
    public static double finalCollapseWindowDays;
    public static double collapseDurationDays;
    public static double collapseHazardIntensity;
    public static boolean tributeEnabled;
    public static boolean returnToOverworldOnVictory;
    public static int chestsPerFloor;
    public static double bossHealthBonus;
    public static double bossDamageBonus;
    public static boolean debugCommandsRequireOp;

    private DescentCommonConfig() {
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        monsterHealthGrowthPerFloor = MONSTER_HEALTH_GROWTH_PER_FLOOR.get();
        monsterDamageGrowthPerFloor = MONSTER_DAMAGE_GROWTH_PER_FLOOR.get();
        monsterCountGrowthPerFloor = MONSTER_COUNT_GROWTH_PER_FLOOR.get();
        trapDensityGrowthPerFloor = TRAP_DENSITY_GROWTH_PER_FLOOR.get();
        resourceScarcityGrowthPerFloor = RESOURCE_SCARCITY_GROWTH_PER_FLOOR.get();
        warningWindowDays = WARNING_WINDOW_DAYS.get();
        finalWarningWindowDays = FINAL_WARNING_WINDOW_DAYS.get();
        finalCollapseWindowDays = FINAL_COLLAPSE_WINDOW_DAYS.get();
        collapseDurationDays = COLLAPSE_DURATION_DAYS.get();
        collapseHazardIntensity = COLLAPSE_HAZARD_INTENSITY.get();
        tributeEnabled = TRIBUTE_ENABLED.get();
        returnToOverworldOnVictory = RETURN_TO_OVERWORLD_ON_VICTORY.get();
        chestsPerFloor = CHESTS_PER_FLOOR.get();
        bossHealthBonus = BOSS_HEALTH_BONUS.get();
        bossDamageBonus = BOSS_DAMAGE_BONUS.get();
        debugCommandsRequireOp = DEBUG_COMMANDS_REQUIRE_OP.get();
    }
}
