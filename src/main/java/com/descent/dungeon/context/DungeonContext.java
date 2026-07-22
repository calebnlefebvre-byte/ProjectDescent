package com.descent.dungeon.context;

import com.descent.dungeon.collapse.CollapseProfile;
import com.descent.dungeon.collapse.CollapseProfileRegistry;
import com.descent.dungeon.config.FloorDefinition;
import com.descent.dungeon.timer.FloorClock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

/**
 * A single immutable snapshot of "everything about where we are right now" —
 * the floor, its theme, its timer phase, its collapse profile, the world
 * seed, how many players are present, and which floor modifiers are active.
 * Added so systems receive one object instead of assembling their own subset
 * of these from four or five different places, which matters most for
 * anything that will eventually need most of them at once — a future
 * Dungeon Director being the obvious example.
 * <p>
 * Deliberately does <em>not</em> carry precomputed difficulty multipliers:
 * {@code difficulty.DifficultyCalculator} stays the single source of truth
 * for those, computed from {@link #floorNumber()} on demand, so there is
 * never a stale cached multiplier sitting in a context object.
 * <p>
 * Distinct from {@code api.director.DungeonDirectorContext}, which is
 * narrower and specific to the Director's own hook methods (it predates
 * this class and nothing forced the two together). This is the
 * general-purpose one other systems should reach for.
 */
public record DungeonContext(
        ServerLevel level,
        int floorNumber,
        ResourceLocation theme,
        FloorClock.Phase timerPhase,
        CollapseProfile collapseProfile,
        long worldSeed,
        int playerCount,
        List<ResourceLocation> activeModifiers
) {

    public static DungeonContext of(ServerLevel level, FloorDefinition definition, FloorClock clock, long worldSeed) {
        return new DungeonContext(
                level,
                definition.floorNumber(),
                definition.theme(),
                clock.phase(),
                CollapseProfileRegistry.forTheme(definition.theme()),
                worldSeed,
                level.players().size(),
                definition.modifiers()
        );
    }
}
