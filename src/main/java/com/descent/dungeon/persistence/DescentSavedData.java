package com.descent.dungeon.persistence;

import com.descent.dungeon.DescentMod;
import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * World-scoped dungeon state: per-floor save data (seed, staircases) and
 * which floor each player currently occupies. Attached to the Overworld's
 * data storage since the dungeon spans many dimensions but the game only
 * ever has one Overworld.
 * <p>
 * Living here rather than in per-player data is what makes staircase
 * discovery (Phase 3) and floor state inherently shared in multiplayer — see
 * the design document's "Shared staircase discoveries" requirement.
 * <p>
 * Every save carries a {@code version} tag ({@link #SAVE_VERSION}). Nothing
 * reads it to migrate data yet — there's been exactly one save format so
 * far — but it exists from the first release rather than being bolted on
 * after the first format change forces the issue. A save with no version
 * tag (i.e. saved before this existed) is treated as version {@code 0}.
 */
public final class DescentSavedData extends SavedData {

    private static final String DATA_NAME = "descent_dungeon_state";

    /** Bump this and add migration logic in {@link #load} when the save format changes. */
    public static final int SAVE_VERSION = 1;

    private final Map<Integer, FloorSaveState> floors = new HashMap<>();
    private final Map<UUID, Integer> playerFloors = new HashMap<>();
    private boolean dungeonCompleted;

    public static DescentSavedData create() {
        return new DescentSavedData();
    }

    public static DescentSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        DescentSavedData data = new DescentSavedData();

        int version = tag.contains("version") ? tag.getInt("version") : 0;
        if (version > SAVE_VERSION) {
            DescentMod.LOGGER.warn("Descent save data is version {}, newer than this build's version {} — loading anyway, "
                    + "but you may be running an older mod version against a newer save", version, SAVE_VERSION);
        }

        Codec<List<FloorSaveState>> floorListCodec = FloorSaveState.CODEC.listOf();
        if (tag.contains("floors")) {
            floorListCodec.parse(NbtOps.INSTANCE, tag.get("floors"))
                    .resultOrPartial(error -> DescentMod.LOGGER.error("Failed to parse Descent floor save data: {}", error))
                    .ifPresent(list -> list.forEach(floor -> data.floors.put(floor.floorNumber(), floor)));
        }

        CompoundTag playersTag = tag.getCompound("player_floors");
        for (String key : playersTag.getAllKeys()) {
            try {
                data.playerFloors.put(UUID.fromString(key), playersTag.getInt(key));
            } catch (IllegalArgumentException invalidUuid) {
                DescentMod.LOGGER.warn("Ignoring malformed player UUID '{}' in Descent save data", key);
            }
        }

        data.dungeonCompleted = tag.getBoolean("dungeon_completed");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("version", SAVE_VERSION);

        Codec<List<FloorSaveState>> floorListCodec = FloorSaveState.CODEC.listOf();
        floorListCodec.encodeStart(NbtOps.INSTANCE, List.copyOf(floors.values()))
                .resultOrPartial(error -> DescentMod.LOGGER.error("Failed to write Descent floor save data: {}", error))
                .ifPresent(encoded -> tag.put("floors", encoded));

        CompoundTag playersTag = new CompoundTag();
        playerFloors.forEach((uuid, floor) -> playersTag.putInt(uuid.toString(), floor));
        tag.put("player_floors", playersTag);

        tag.putBoolean("dungeon_completed", dungeonCompleted);
        return tag;
    }

    public static DescentSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage()
                .computeIfAbsent(new SavedData.Factory<>(DescentSavedData::create, DescentSavedData::load), DATA_NAME);
    }

    public Optional<FloorSaveState> getFloor(int floorNumber) {
        return Optional.ofNullable(floors.get(floorNumber));
    }

    public void putFloor(FloorSaveState state) {
        floors.put(state.floorNumber(), state);
        setDirty();
    }

    public int getPlayerFloor(UUID playerId, int defaultFloor) {
        return playerFloors.getOrDefault(playerId, defaultFloor);
    }

    public void setPlayerFloor(UUID playerId, int floorNumber) {
        playerFloors.put(playerId, floorNumber);
        setDirty();
    }

    public boolean isDungeonCompleted() {
        return dungeonCompleted;
    }

    public void setDungeonCompleted(boolean dungeonCompleted) {
        this.dungeonCompleted = dungeonCompleted;
        setDirty();
    }
}
