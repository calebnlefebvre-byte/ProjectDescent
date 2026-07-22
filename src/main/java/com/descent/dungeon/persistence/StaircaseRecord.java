package com.descent.dungeon.persistence;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

/**
 * One generated staircase on a floor. Placement happens in Phase 2
 * ({@code stairs.StaircasePlacer}); {@code discovered} starts {@code false}
 * for every staircase and is flipped by Phase 3's proximity check
 * ({@code events.StaircaseEvents}) — per the design document, discovery is
 * permanent and shared world-wide once any player finds it, which falls out
 * naturally here from this record living in world-scoped
 * {@link DescentSavedData} rather than per-player data.
 *
 * @param position        the surface entrance position (used for discovery proximity)
 * @param landingPosition the marker block at the bottom of the shaft (used to match
 *                        a right-click interaction to this staircase for descending)
 * @param finalStaircase  true only for Floor 18's single exit; descending it triggers
 *                        the victory sequence instead of moving to another floor
 */
public record StaircaseRecord(BlockPos position, BlockPos landingPosition, boolean discovered, boolean finalStaircase) {

    public static final Codec<StaircaseRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("position").forGetter(StaircaseRecord::position),
            BlockPos.CODEC.fieldOf("landing_position").forGetter(StaircaseRecord::landingPosition),
            Codec.BOOL.optionalFieldOf("discovered", false).forGetter(StaircaseRecord::discovered),
            Codec.BOOL.optionalFieldOf("final_staircase", false).forGetter(StaircaseRecord::finalStaircase)
    ).apply(instance, StaircaseRecord::new));

    public StaircaseRecord withDiscovered() {
        return new StaircaseRecord(position, landingPosition, true, finalStaircase);
    }
}
