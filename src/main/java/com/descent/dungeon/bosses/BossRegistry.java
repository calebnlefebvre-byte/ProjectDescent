package com.descent.dungeon.bosses;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registration/lookup for installed {@link IBoss}es, keyed by floor number.
 * Deliberately separate from {@link BossSpawnController} (which decides
 * *when* to spawn) and {@link BossFactory} (which decides *how* to build the
 * entity) — a future datapack-driven or add-on boss only ever needs to call
 * {@link #register}, never touch the spawn or factory logic.
 */
public final class BossRegistry {

    private static final Map<Integer, IBoss> BOSSES = new HashMap<>();

    private BossRegistry() {
    }

    public static void register(IBoss boss) {
        BOSSES.put(boss.floorNumber(), boss);
    }

    public static Optional<IBoss> forFloor(int floorNumber) {
        return Optional.ofNullable(BOSSES.get(floorNumber));
    }
}
