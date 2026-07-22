package com.descent.dungeon.generation;

import com.descent.dungeon.DescentMod;
import com.descent.dungeon.config.FloorConfigManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.OptionalInt;

/**
 * Resolves the {@link ResourceKey} and live {@link ServerLevel} for a given
 * floor number. Every floor is a separately-registered dimension —
 * {@code data/descent/dimension/floor_<n>.json}, paired with either the
 * {@code descent:cave_floor} or {@code descent:town_floor} dimension type —
 * rather than a dimension created dynamically at runtime. Static
 * registration is far better trodden ground in NeoForge than dynamic level
 * creation, and it costs nothing at idle: Minecraft does not generate a
 * dimension's terrain until a chunk in it is actually loaded, so an
 * unvisited floor has no meaningful footprint beyond an empty level file.
 */
public final class DescentDimensions {

    private static final String FLOOR_PATH_PREFIX = "floor_";

    private DescentDimensions() {
    }

    public static ResourceKey<Level> floorLevelKey(int floorNumber) {
        return ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.fromNamespaceAndPath(DescentMod.MODID, FLOOR_PATH_PREFIX + floorNumber));
    }

    /**
     * @throws IllegalStateException if the floor's dimension is not currently loaded on this
     *                                server — this should only happen for an out-of-range floor
     *                                number, since all 18 floor dimensions are registered at world load
     */
    public static ServerLevel getFloorLevel(MinecraftServer server, int floorNumber) {
        ServerLevel level = server.getLevel(floorLevelKey(floorNumber));
        if (level == null) {
            throw new IllegalStateException("Floor " + floorNumber + " dimension is not loaded — is "
                    + "data/descent/dimension/floor_" + floorNumber + ".json present?");
        }
        return level;
    }

    /** Returns the floor number for a Descent floor dimension key, or empty if {@code levelKey} is not one. */
    public static OptionalInt floorNumberFromKey(ResourceKey<Level> levelKey) {
        ResourceLocation location = levelKey.location();
        if (!location.getNamespace().equals(DescentMod.MODID) || !location.getPath().startsWith(FLOOR_PATH_PREFIX)) {
            return OptionalInt.empty();
        }
        try {
            int floorNumber = Integer.parseInt(location.getPath().substring(FLOOR_PATH_PREFIX.length()));
            return floorNumber >= 1 && floorNumber <= FloorConfigManager.FLOOR_COUNT
                    ? OptionalInt.of(floorNumber)
                    : OptionalInt.empty();
        } catch (NumberFormatException notAFloor) {
            return OptionalInt.empty();
        }
    }

    /** A deterministic, always-safe-to-stand-on position near the world origin, used when a player first enters a floor. */
    public static BlockPos findSafeEntryPosition(ServerLevel level) {
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 0, 0);
        return new BlockPos(0, y, 0);
    }
}
