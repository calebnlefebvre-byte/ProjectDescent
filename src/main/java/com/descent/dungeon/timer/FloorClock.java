package com.descent.dungeon.timer;

import com.descent.dungeon.config.DescentCommonConfig;

import java.util.Comparator;
import java.util.List;

/**
 * Pure computation of a floor's timer/collapse phase from elapsed game time.
 * Nothing here reads or writes persisted state — callers (
 * {@code events.FloorTimerEvents}) supply {@code startGameTime} (from
 * {@code persistence.FloorSaveState}) and {@code currentGameTime} (from the
 * floor's {@code ServerLevel#getGameTime()}) and get back where the floor
 * currently stands.
 * <p>
 * The phase list is a sorted set of "this phase starts at elapsed tick X"
 * boundaries rather than a hardcoded if/else chain, specifically so a future
 * phase can be inserted by adding one more {@link PhaseBoundary} entry here —
 * nothing about the lookup logic itself needs to change.
 */
public final class FloorClock {

    /** Ticks in one Minecraft day — the unit the design document measures floor timers in. */
    public static final long TICKS_PER_DAY = 24000L;

    public enum Phase {
        /** Timer counting down, no signs of trouble yet. Descending now costs an Early Descent Tribute. */
        ACTIVE,
        /** Timer nearing expiry; a first, low-urgency warning has gone out. Still costs tribute to descend. */
        WARNING,
        /** Timer about to expire; a final, high-urgency warning has gone out. Still costs tribute to descend. */
        FINAL_WARNING,
        /** Timer expired; collapse hazards are active. Descending now is free. */
        COLLAPSING,
        /** Collapse nearing its end; hazards intensify further. Descending now is free. */
        FINAL_COLLAPSE,
        /** Collapse finished; anyone still present should have already been killed. */
        ENDED
    }

    private record PhaseBoundary(long startsAtTick, Phase phase) {
    }

    private final Phase phase;
    private final long elapsedTicks;
    private final long timerTicks;
    private final long collapseDurationTicks;

    private FloorClock(Phase phase, long elapsedTicks, long timerTicks, long collapseDurationTicks) {
        this.phase = phase;
        this.elapsedTicks = elapsedTicks;
        this.timerTicks = timerTicks;
        this.collapseDurationTicks = collapseDurationTicks;
    }

    public static FloorClock compute(int timerDays, long startGameTime, long currentGameTime) {
        long elapsed = Math.max(0, currentGameTime - startGameTime);
        long timerTicks = timerDays * TICKS_PER_DAY;
        long collapseDurationTicks = Math.round(DescentCommonConfig.collapseDurationDays * TICKS_PER_DAY);
        long warningWindow = Math.round(DescentCommonConfig.warningWindowDays * TICKS_PER_DAY);
        long finalWarningWindow = Math.round(DescentCommonConfig.finalWarningWindowDays * TICKS_PER_DAY);
        long finalCollapseWindow = Math.round(DescentCommonConfig.finalCollapseWindowDays * TICKS_PER_DAY);

        List<PhaseBoundary> boundaries = List.of(
                new PhaseBoundary(0, Phase.ACTIVE),
                new PhaseBoundary(Math.max(0, timerTicks - warningWindow), Phase.WARNING),
                new PhaseBoundary(Math.max(0, timerTicks - finalWarningWindow), Phase.FINAL_WARNING),
                new PhaseBoundary(timerTicks, Phase.COLLAPSING),
                new PhaseBoundary(timerTicks + Math.max(0, collapseDurationTicks - finalCollapseWindow), Phase.FINAL_COLLAPSE),
                new PhaseBoundary(timerTicks + collapseDurationTicks, Phase.ENDED)
        );

        Phase phase = Phase.ACTIVE;
        for (PhaseBoundary boundary : boundaries.stream().sorted(Comparator.comparingLong(PhaseBoundary::startsAtTick)).toList()) {
            if (elapsed >= boundary.startsAtTick()) {
                phase = boundary.phase();
            } else {
                break;
            }
        }
        return new FloorClock(phase, elapsed, timerTicks, collapseDurationTicks);
    }

    public Phase phase() {
        return phase;
    }

    /** Whether descending right now would trigger the Early Descent Tribute (i.e. the timer has not expired). */
    public boolean isEarlyDescent() {
        return phase == Phase.ACTIVE || phase == Phase.WARNING || phase == Phase.FINAL_WARNING;
    }

    /** Whether collapse hazards should be actively striking (does not include {@link Phase#ENDED}). */
    public boolean isCollapsing() {
        return phase == Phase.COLLAPSING || phase == Phase.FINAL_COLLAPSE;
    }

    public long elapsedTicks() {
        return elapsedTicks;
    }

    public long remainingTicksUntilCollapse() {
        return Math.max(0, timerTicks - elapsedTicks);
    }

    /** Progress through the whole collapse window, from {@code 0.0} (just started) to {@code 1.0} (about to end). Only meaningful once {@link #isCollapsing()}. */
    public double collapseProgress() {
        if (collapseDurationTicks <= 0) {
            return 1.0;
        }
        return Math.min(1.0, Math.max(0.0, (double) (elapsedTicks - timerTicks) / collapseDurationTicks));
    }
}
