package com.descent.dungeon.persistence;

import com.descent.dungeon.timer.FloorClock;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Persisted state for a single floor: its derived seed (see
 * {@code util.DeterministicSeed} — cached here mainly so a saved world keeps
 * behaving the same way even if the derivation algorithm ever changes), its
 * generated staircases, its timer/collapse progress, and whether its boss
 * has been defeated.
 * <p>
 * {@code startGameTime} anchors the floor's countdown: it's the floor
 * dimension's own {@code ServerLevel#getGameTime()} at the moment the floor
 * was first entered. Elapsed time is always computed as
 * {@code currentGameTime - startGameTime} (see {@code timer.FloorClock}) —
 * this keeps ticking even with no players on the floor, since custom
 * dimensions tick regardless of occupancy, which matches the design
 * document's "the timer begins immediately upon entering the floor" and
 * gives it no further special-casing once started.
 * <p>
 * {@code announcedPhases} tracks which {@link FloorClock.Phase} transitions
 * {@code events.FloorTimerEvents} has already reacted to (sent a message
 * for, started/ended a hazard sequence for), so each is reacted to exactly
 * once. It's a set of phases rather than one boolean field per phase
 * specifically so inserting a new {@code FloorClock.Phase} later (per the
 * design review that expanded ACTIVE/COLLAPSING/ENDED into six phases) never
 * requires touching this class again.
 * <p>
 * {@code bossDefeated} exists ahead of any boss actually being implemented
 * (see {@code bosses.BossSpawnController}) so the framework has somewhere to
 * record "don't spawn this floor's boss again" from day one.
 */
public record FloorSaveState(
        int floorNumber,
        long seed,
        long startGameTime,
        boolean staircasesPlaced,
        List<StaircaseRecord> staircases,
        Set<FloorClock.Phase> announcedPhases,
        boolean bossDefeated
) {

    private static final Codec<FloorClock.Phase> PHASE_CODEC = Codec.STRING.xmap(FloorClock.Phase::valueOf, Enum::name);

    public static final Codec<FloorSaveState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("floor_number").forGetter(FloorSaveState::floorNumber),
            Codec.LONG.fieldOf("seed").forGetter(FloorSaveState::seed),
            Codec.LONG.fieldOf("start_game_time").forGetter(FloorSaveState::startGameTime),
            Codec.BOOL.optionalFieldOf("staircases_placed", false).forGetter(FloorSaveState::staircasesPlaced),
            StaircaseRecord.CODEC.listOf().optionalFieldOf("staircases", List.of()).forGetter(FloorSaveState::staircases),
            PHASE_CODEC.listOf()
                    .xmap(list -> (Set<FloorClock.Phase>) new LinkedHashSet<>(list), List::copyOf)
                    .optionalFieldOf("announced_phases", Set.of())
                    .forGetter(FloorSaveState::announcedPhases),
            Codec.BOOL.optionalFieldOf("boss_defeated", false).forGetter(FloorSaveState::bossDefeated)
    ).apply(instance, FloorSaveState::new));

    public static FloorSaveState empty(int floorNumber, long seed, long startGameTime) {
        return new FloorSaveState(floorNumber, seed, startGameTime, false, List.of(), Set.of(), false);
    }

    public FloorSaveState withStaircases(List<StaircaseRecord> staircases) {
        return new FloorSaveState(floorNumber, seed, startGameTime, true, List.copyOf(staircases), announcedPhases, bossDefeated);
    }

    /** Replaces the staircase at {@code index} (e.g. after flipping its {@code discovered} flag). */
    public FloorSaveState withStaircaseAt(int index, StaircaseRecord updated) {
        List<StaircaseRecord> copy = new ArrayList<>(staircases);
        copy.set(index, updated);
        return new FloorSaveState(floorNumber, seed, startGameTime, staircasesPlaced, List.copyOf(copy), announcedPhases, bossDefeated);
    }

    public boolean hasAnnounced(FloorClock.Phase phase) {
        return announcedPhases.contains(phase);
    }

    /** Marks {@code phase} as reacted to; safe to call repeatedly. */
    public FloorSaveState withAnnounced(FloorClock.Phase phase) {
        if (announcedPhases.contains(phase)) {
            return this;
        }
        Set<FloorClock.Phase> next = new LinkedHashSet<>(announcedPhases);
        next.add(phase);
        return new FloorSaveState(floorNumber, seed, startGameTime, staircasesPlaced, staircases, next, bossDefeated);
    }

    /** Rewrites the start time — used by {@code /descent collapse} to force a floor's timer to have already expired. */
    public FloorSaveState withStartGameTime(long startGameTime) {
        return new FloorSaveState(floorNumber, seed, startGameTime, staircasesPlaced, staircases, announcedPhases, bossDefeated);
    }

    public FloorSaveState withBossDefeated() {
        return new FloorSaveState(floorNumber, seed, startGameTime, staircasesPlaced, staircases, announcedPhases, true);
    }
}
