package com.descent.dungeon.stairs;

import com.descent.dungeon.DescentMod;
import com.descent.dungeon.config.FloorConfigManager;
import com.descent.dungeon.persistence.StaircaseRecord;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.List;

/**
 * Deterministically places a floor's staircases and builds their physical
 * structure. "Deterministic" means driven entirely by the floor's derived
 * seed (see {@code util.DeterministicSeed}) — same seed, same staircase
 * positions, every time.
 * <p>
 * The physical structure built here (a marked entrance ring at the surface,
 * a descending stair shaft, a sealed landing) is intentionally simple.
 * "Hidden until discovered" is a knowledge/map-state concept, not
 * invisibility: the blocks are real and walkable the moment they're placed.
 * Phase 3 adds the proximity check that flips {@link StaircaseRecord#discovered()}
 * and the interaction that actually descends a floor.
 */
public final class StaircasePlacer {

    private static final int SEARCH_RADIUS_BLOCKS = 400;
    private static final int MIN_SEPARATION_BLOCKS = 48;
    private static final int ATTEMPTS_PER_STAIRCASE = 200;
    private static final int SHAFT_STEPS = 12;
    private static final Direction[] HORIZONTAL_DIRECTIONS =
            {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    private StaircasePlacer() {
    }

    /**
     * Places {@code count} staircases on {@code level}, seeded from {@code floorSeed}. The
     * dungeon's single final exit (Floor {@link FloorConfigManager#FLOOR_COUNT}, which always
     * has exactly one staircase per the design document's schedule) is flagged
     * {@link StaircaseRecord#finalStaircase()}.
     */
    public static List<StaircaseRecord> placeStaircases(ServerLevel level, int floorNumber, long floorSeed, int count) {
        RandomSource random = RandomSource.create(floorSeed);
        List<BlockPos> placedPositions = new ArrayList<>(count);
        List<StaircaseRecord> records = new ArrayList<>(count);
        boolean isFinalFloor = floorNumber == FloorConfigManager.FLOOR_COUNT;

        for (int i = 0; i < count; i++) {
            BlockPos position = findCandidatePosition(level, random, placedPositions);
            if (position == null) {
                DescentMod.LOGGER.warn("Floor {}: could not find room for staircase {} of {} after exhausting search attempts",
                        floorNumber, i + 1, count);
                continue;
            }
            placedPositions.add(position);
            BlockPos landing = buildStaircaseStructure(level, position, random);
            records.add(new StaircaseRecord(position, landing, false, isFinalFloor));
        }
        return List.copyOf(records);
    }

    /** Upgrades an already-placed staircase's entrance ring to a bright, permanent "found" marker. */
    public static void markDiscovered(ServerLevel level, BlockPos entrance) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx != 0 || dz != 0) {
                    level.setBlock(entrance.offset(dx, 0, dz), Blocks.SEA_LANTERN.defaultBlockState(), 3);
                }
            }
        }
    }

    private static BlockPos findCandidatePosition(ServerLevel level, RandomSource random, List<BlockPos> alreadyPlaced) {
        int maxAttempts = ATTEMPTS_PER_STAIRCASE * (alreadyPlaced.size() + 1);
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int x = random.nextIntBetweenInclusive(-SEARCH_RADIUS_BLOCKS, SEARCH_RADIUS_BLOCKS);
            int z = random.nextIntBetweenInclusive(-SEARCH_RADIUS_BLOCKS, SEARCH_RADIUS_BLOCKS);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
            BlockPos candidate = new BlockPos(x, y, z);

            if (y <= level.getMinY() + SHAFT_STEPS + 4) {
                continue;
            }
            boolean farEnoughFromOthers = alreadyPlaced.stream()
                    .allMatch(other -> other.distSqr(candidate) >= (double) MIN_SEPARATION_BLOCKS * MIN_SEPARATION_BLOCKS);
            if (farEnoughFromOthers) {
                return candidate;
            }
        }
        return null;
    }

    /** Builds a marked entrance, a descending stair shaft, and a sealed landing at the bottom. Returns the landing marker position. */
    private static BlockPos buildStaircaseStructure(ServerLevel level, BlockPos entrance, RandomSource random) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx != 0 || dz != 0) {
                    level.setBlock(entrance.offset(dx, 0, dz), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 3);
                }
            }
        }
        level.setBlock(entrance, Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(entrance.above(), Blocks.AIR.defaultBlockState(), 3);

        Direction facing = HORIZONTAL_DIRECTIONS[random.nextInt(HORIZONTAL_DIRECTIONS.length)];
        BlockPos cursor = entrance.below();
        for (int step = 0; step < SHAFT_STEPS; step++) {
            cursor = cursor.relative(facing).below();
            level.setBlock(cursor, Blocks.COBBLED_DEEPSLATE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, facing), 3);
            level.setBlock(cursor.above(), Blocks.AIR.defaultBlockState(), 3);
            level.setBlock(cursor.above(2), Blocks.AIR.defaultBlockState(), 3);
            level.setBlock(cursor.relative(facing), Blocks.COBBLED_DEEPSLATE.defaultBlockState(), 3);
        }

        BlockPos landing = cursor.relative(facing);
        level.setBlock(landing, Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(), 3);
        level.setBlock(landing.above(), Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(landing.above(2), Blocks.AIR.defaultBlockState(), 3);
        return landing;
    }
}
