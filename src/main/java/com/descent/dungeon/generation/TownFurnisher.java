package com.descent.dungeon.generation;

import com.descent.dungeon.loot.ChestGenerator;
import com.descent.dungeon.loot.LootGenerator;
import com.descent.dungeon.loot.LootTables;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;

/**
 * Builds a small, plain "outpost" on a town floor (crafting table, furnace,
 * smithing table, anvil, a chest of modest supplies) — a real, if modest,
 * answer to the design document's town-floor "merchants, repairs,
 * preparation" intent that doesn't require the villager-trade API surface.
 * Called once per town floor, right after staircase/chest placement, from
 * {@code events.FloorGenerationEvents}.
 */
public final class TownFurnisher {

    private static final int SEARCH_RADIUS_BLOCKS = 60;
    private static final long SEED_OFFSET = 0x2545F4914F6CDD1DL;
    private static final Direction[] HORIZONTAL_DIRECTIONS =
            {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    private TownFurnisher() {
    }

    public static void furnish(ServerLevel level, int floorNumber, long floorSeed) {
        RandomSource random = RandomSource.create(floorSeed ^ SEED_OFFSET);
        int x = random.nextIntBetweenInclusive(-SEARCH_RADIUS_BLOCKS, SEARCH_RADIUS_BLOCKS);
        int z = random.nextIntBetweenInclusive(-SEARCH_RADIUS_BLOCKS, SEARCH_RADIUS_BLOCKS);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos corner = new BlockPos(x, y, z);

        buildOutpost(level, corner, random);
        List<ItemStack> loot = LootGenerator.generate(LootTables.COMMON, 3, floorNumber, random);
        ChestGenerator.placeChest(level, corner.offset(3, 1, 1), loot, random);
    }

    /** A plain 5x5 stone-brick room, 4 tall, with a doorway and four utility blocks along the back wall. */
    private static void buildOutpost(ServerLevel level, BlockPos corner, RandomSource random) {
        for (int dx = 0; dx < 5; dx++) {
            for (int dz = 0; dz < 5; dz++) {
                level.setBlock(corner.offset(dx, 0, dz), Blocks.STONE_BRICKS.defaultBlockState(), 3);
                for (int dy = 1; dy <= 4; dy++) {
                    boolean isWall = dx == 0 || dx == 4 || dz == 0 || dz == 4;
                    if (isWall) {
                        level.setBlock(corner.offset(dx, dy, dz), Blocks.STONE_BRICKS.defaultBlockState(), 3);
                    } else if (dy == 4) {
                        level.setBlock(corner.offset(dx, dy, dz), Blocks.STONE_BRICKS.defaultBlockState(), 3);
                    } else {
                        level.setBlock(corner.offset(dx, dy, dz), Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }

        // Doorway through the front wall (dz == 0), centered.
        level.setBlock(corner.offset(2, 1, 0), Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(corner.offset(2, 2, 0), Blocks.AIR.defaultBlockState(), 3);

        // Utility blocks along the back interior wall (dz == 3), floor level.
        level.setBlock(corner.offset(1, 1, 3), Blocks.CRAFTING_TABLE.defaultBlockState(), 3);
        level.setBlock(corner.offset(2, 1, 3), Blocks.FURNACE.defaultBlockState(), 3);
        level.setBlock(corner.offset(3, 1, 3), Blocks.SMITHING_TABLE.defaultBlockState(), 3);

        Direction anvilFacing = HORIZONTAL_DIRECTIONS[random.nextInt(HORIZONTAL_DIRECTIONS.length)];
        level.setBlock(corner.offset(1, 1, 1),
                Blocks.ANVIL.defaultBlockState().setValue(net.minecraft.world.level.block.AnvilBlock.FACING, anvilFacing), 3);
    }
}
