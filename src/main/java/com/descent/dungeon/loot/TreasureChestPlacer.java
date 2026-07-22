package com.descent.dungeon.loot;

import com.descent.dungeon.difficulty.DifficultyCalculator;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.List;

/**
 * Places a floor's treasure chests: deterministic positions (from the same
 * floor seed {@code stairs.StaircasePlacer} uses, offset so chest and
 * staircase positions don't correlate 1:1), filled via
 * {@link LootGenerator#generate} using the tier
 * {@link DifficultyCalculator#lootTierForFloor} assigns that floor. Called
 * once per floor alongside staircase placement — see
 * {@code events.FloorGenerationEvents}.
 */
public final class TreasureChestPlacer {

    private static final int SEARCH_RADIUS_BLOCKS = 400;
    private static final int MIN_SEPARATION_BLOCKS = 32;
    private static final int ATTEMPTS_PER_CHEST = 150;
    private static final int MIN_ROLLS = 2;
    private static final int MAX_ROLLS = 4;

    /** Decorrelates chest positions from staircase positions despite sharing the same floor seed. */
    private static final long SEED_OFFSET = 0x5DEECE66DL;

    private TreasureChestPlacer() {
    }

    public static void placeChests(ServerLevel level, int floorNumber, long floorSeed, int count) {
        RandomSource random = RandomSource.create(floorSeed ^ SEED_OFFSET);
        ResourceLocation tableId = LootTables.forTier(DifficultyCalculator.lootTierForFloor(floorNumber));
        List<BlockPos> placed = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            BlockPos position = findCandidatePosition(level, random, placed);
            if (position == null) {
                continue;
            }
            placed.add(position);

            int rolls = MIN_ROLLS + random.nextInt(MAX_ROLLS - MIN_ROLLS + 1);
            List<ItemStack> loot = LootGenerator.generate(tableId, rolls, floorNumber, random);
            ChestGenerator.placeChest(level, position, loot, random);
        }
    }

    private static BlockPos findCandidatePosition(ServerLevel level, RandomSource random, List<BlockPos> alreadyPlaced) {
        int maxAttempts = ATTEMPTS_PER_CHEST * (alreadyPlaced.size() + 1);
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int x = random.nextIntBetweenInclusive(-SEARCH_RADIUS_BLOCKS, SEARCH_RADIUS_BLOCKS);
            int z = random.nextIntBetweenInclusive(-SEARCH_RADIUS_BLOCKS, SEARCH_RADIUS_BLOCKS);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
            BlockPos candidate = new BlockPos(x, y, z);

            if (y <= level.getMinY() + 4) {
                continue;
            }
            boolean farEnough = alreadyPlaced.stream()
                    .allMatch(other -> other.distSqr(candidate) >= (double) MIN_SEPARATION_BLOCKS * MIN_SEPARATION_BLOCKS);
            if (farEnough) {
                return candidate;
            }
        }
        return null;
    }
}
