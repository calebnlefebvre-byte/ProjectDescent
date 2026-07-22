package com.descent.dungeon.collapse;

import com.descent.dungeon.context.DungeonContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Applies one collapse hazard near a player, picked by a floor's
 * {@link CollapseProfile} (see {@code CollapseProfileRegistry#forTheme}).
 * Called periodically by {@code events.FloorTimerEvents} for every player on
 * a floor that's collapsing. Each call picks one hazard so a player doesn't
 * get hit by all of them simultaneously every check — the caller controls
 * how often this runs and scales frequency with
 * {@code timer.FloorClock#collapseProgress()} and
 * {@code config.DescentCommonConfig#collapseHazardIntensity}.
 * <p>
 * Not every hazard the design document lists gets its own distinct
 * implementation — "earthquake" in particular is approximated (rumbling
 * sound, nausea, block-break particles) rather than built as a real
 * screen-shake effect, since Minecraft has no built-in camera-shake hook to
 * key off of.
 */
public final class CollapseHazards {

    private static final int HAZARD_RADIUS = 6;
    private static final BlockState[] DEBRIS_BLOCKS = {
            Blocks.COBBLESTONE.defaultBlockState(),
            Blocks.COBBLED_DEEPSLATE.defaultBlockState(),
            Blocks.GRAVEL.defaultBlockState()
    };

    private CollapseHazards() {
    }

    public static void strike(DungeonContext context, ServerPlayer player, RandomSource random) {
        ServerLevel level = context.level();
        HazardType hazard = context.collapseProfile().pickHazard(random);
        BlockPos origin = player.blockPosition();

        switch (hazard) {
            case EXPLOSION -> {
                BlockPos target = jitter(origin, random);
                level.explode(null, target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5,
                        2.0F + random.nextFloat(), Level.ExplosionInteraction.BLOCK);
            }
            case FIRE -> {
                player.setRemainingFireTicks(60 + random.nextInt(60));
                level.playSound(null, origin, SoundEvents.FIRE_AMBIENT, SoundSource.HOSTILE, 1.0F, 0.8F);
            }
            case POISON_GAS -> {
                player.addEffect(new MobEffectInstance(MobEffects.POISON, 100 + random.nextInt(100), 0));
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.MYCELIUM,
                        origin.getX() + 0.5, origin.getY() + 1.0, origin.getZ() + 0.5, 15, 1.0, 0.5, 1.0, 0.0);
            }
            case DARKNESS -> {
                player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 160, 0));
                level.playSound(null, origin, SoundEvents.AMBIENT_CAVE.value(), SoundSource.HOSTILE, 1.0F, 0.6F);
            }
            case FALLING_DEBRIS -> {
                BlockPos above = jitter(origin, random).above(6 + random.nextInt(6));
                BlockState debris = DEBRIS_BLOCKS[random.nextInt(DEBRIS_BLOCKS.length)];
                FallingBlockEntity.fall(level, above, debris);
                level.playSound(null, origin, SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 1.0F, 0.7F);
            }
            case EARTHQUAKE -> {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
                level.playSound(null, origin, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 1.0F, 0.3F);
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT,
                        origin.getX() + 0.5, origin.getY(), origin.getZ() + 0.5, 20, 2.0, 0.2, 2.0, 0.0);
            }
            case BLOCK_COLLAPSE -> collapseBlocks(level, jitter(origin, random), random);
            case LAVA_FISSURE -> {
                BlockPos target = jitter(origin, random);
                level.setBlock(target, Blocks.LAVA.defaultBlockState(), 3);
                level.explode(null, target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5,
                        1.5F, Level.ExplosionInteraction.BLOCK);
            }
            case LIGHTNING -> {
                BlockPos target = jitter(origin, random);
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.EVENT);
                if (bolt != null) {
                    bolt.moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
                    level.addFreshEntity(bolt);
                }
            }
            case PHANTOM_ATTACK -> {
                BlockPos above = origin.above(8 + random.nextInt(4));
                Phantom phantom = EntityType.PHANTOM.create(level, EntitySpawnReason.EVENT);
                if (phantom != null) {
                    phantom.moveTo(above.getX() + 0.5, above.getY(), above.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
                    level.addFreshEntity(phantom);
                }
            }
        }

        player.displayClientMessage(Component.literal("The floor shudders around you."), true);
    }

    private static BlockPos jitter(BlockPos origin, RandomSource random) {
        return origin.offset(random.nextInt(HAZARD_RADIUS * 2 + 1) - HAZARD_RADIUS, 0,
                random.nextInt(HAZARD_RADIUS * 2 + 1) - HAZARD_RADIUS);
    }

    private static void collapseBlocks(ServerLevel level, BlockPos center, RandomSource random) {
        for (int i = 0; i < 6; i++) {
            BlockPos pos = center.offset(random.nextInt(5) - 2, random.nextInt(3) - 1, random.nextInt(5) - 2);
            BlockState state = level.getBlockState(pos);
            if (!state.isAir() && state.getDestroySpeed(level, pos) >= 0) {
                level.destroyBlock(pos, false);
            }
        }
    }
}
