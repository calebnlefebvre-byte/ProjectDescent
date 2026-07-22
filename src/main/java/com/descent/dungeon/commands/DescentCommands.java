package com.descent.dungeon.commands;

import com.descent.dungeon.bosses.BossFactory;
import com.descent.dungeon.bosses.BossRegistry;
import com.descent.dungeon.config.DescentCommonConfig;
import com.descent.dungeon.config.FloorConfigManager;
import com.descent.dungeon.config.FloorDefinition;
import com.descent.dungeon.generation.DescentDimensions;
import com.descent.dungeon.persistence.DescentSavedData;
import com.descent.dungeon.persistence.FloorSaveState;
import com.descent.dungeon.persistence.StaircaseRecord;
import com.descent.dungeon.stairs.StaircasePlacer;
import com.descent.dungeon.timer.FloorClock;
import com.descent.dungeon.util.DeterministicSeed;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

/**
 * Developer/debug command tree: {@code /descent <...>} and {@code /debug <...>}.
 * <p>
 * The command tree itself was wired up in Phase 1 so later phases only need
 * to fill in a handler body, never touch registration or permission gating.
 * Every subcommand is live: {@code floor} teleports the executing player
 * onto a floor (creating its save state if needed), {@code timer} reports a
 * floor's live {@code timer.FloorClock} phase, {@code collapse}
 * force-expires a floor's timer, {@code boss} force-spawns a floor's
 * registered boss regardless of town/defeated state (a debug shortcut, not
 * the real gate — see {@code bosses.BossSpawnController} for that),
 * {@code generate}/{@code structures} drive and inspect staircase
 * placement, {@code reload} prints the loaded schedule, and {@code seed}
 * derives a floor's deterministic seed.
 */
public final class DescentCommands {

    private DescentCommands() {
    }

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("descent")
                .requires(DescentCommands::hasDebugPermission)
                .then(Commands.literal("floor")
                        .then(Commands.argument("floor", IntegerArgumentType.integer(1, FloorConfigManager.FLOOR_COUNT))
                                .executes(DescentCommands::teleportToFloor)))
                .then(Commands.literal("timer")
                        .then(Commands.argument("floor", IntegerArgumentType.integer(1, FloorConfigManager.FLOOR_COUNT))
                                .executes(DescentCommands::showConfiguredTimer)))
                .then(Commands.literal("collapse")
                        .then(Commands.argument("floor", IntegerArgumentType.integer(1, FloorConfigManager.FLOOR_COUNT))
                                .executes(DescentCommands::forceCollapse)))
                .then(Commands.literal("boss")
                        .then(Commands.argument("floor", IntegerArgumentType.integer(1, FloorConfigManager.FLOOR_COUNT))
                                .executes(DescentCommands::forceSpawnBoss)))
                .then(Commands.literal("generate")
                        .then(Commands.argument("floor", IntegerArgumentType.integer(1, FloorConfigManager.FLOOR_COUNT))
                                .executes(DescentCommands::generateFloor)))
                .then(Commands.literal("reload")
                        .executes(DescentCommands::showLoadedSchedule)));

        event.getDispatcher().register(Commands.literal("debug")
                .requires(DescentCommands::hasDebugPermission)
                .then(Commands.literal("structures")
                        .executes(DescentCommands::listStaircases))
                .then(Commands.literal("seed")
                        .then(Commands.argument("floor", IntegerArgumentType.integer(1, FloorConfigManager.FLOOR_COUNT))
                                .executes(DescentCommands::showDerivedSeed))));
    }

    private static boolean hasDebugPermission(CommandSourceStack source) {
        return !DescentCommonConfig.debugCommandsRequireOp || source.hasPermission(2);
    }

    private static int showConfiguredTimer(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        int floor = IntegerArgumentType.getInteger(ctx, "floor");
        CommandSourceStack source = ctx.getSource();
        FloorDefinition definition = FloorConfigManager.get(floor);

        Optional<FloorSaveState> state = DescentSavedData.get(source.getServer()).getFloor(floor);
        if (state.isEmpty()) {
            source.sendSuccess(() -> Component.literal("Floor " + floor + " is configured for a "
                    + definition.timerDays() + "-day timer but has not been entered yet."), false);
            return 1;
        }

        ServerLevel level = DescentDimensions.getFloorLevel(source.getServer(), floor);
        FloorClock clock = FloorClock.compute(definition.timerDays(), state.get().startGameTime(), level.getGameTime());
        String detail;
        if (clock.isEarlyDescent()) {
            detail = ", " + (clock.remainingTicksUntilCollapse() / FloorClock.TICKS_PER_DAY) + "d remaining until collapse";
        } else if (clock.isCollapsing()) {
            detail = String.format(", %.0f%% through collapse", clock.collapseProgress() * 100);
        } else {
            detail = "";
        }
        source.sendSuccess(() -> Component.literal("Floor " + floor + ": " + clock.phase() + detail), false);
        return 1;
    }

    private static int forceCollapse(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        int floor = IntegerArgumentType.getInteger(ctx, "floor");
        CommandSourceStack source = ctx.getSource();
        MinecraftServer server = source.getServer();

        ServerLevel level;
        try {
            level = DescentDimensions.getFloorLevel(server, floor);
        } catch (IllegalStateException notLoaded) {
            source.sendFailure(Component.literal(notLoaded.getMessage()));
            return 0;
        }

        DescentSavedData saveData = DescentSavedData.get(server);
        long seed = DeterministicSeed.deriveFloorSeed(server.overworld().getSeed(), floor);
        FloorSaveState state = saveData.getFloor(floor).orElseGet(() -> FloorSaveState.empty(floor, seed, level.getGameTime()));

        long timerTicks = FloorConfigManager.get(floor).timerDays() * FloorClock.TICKS_PER_DAY;
        FloorSaveState forced = state.withStartGameTime(level.getGameTime() - timerTicks);
        saveData.putFloor(forced);

        source.sendSuccess(() -> Component.literal("Floor " + floor + "'s timer has been forced to expire. "
                + "Collapse will begin on the next tick check."), true);
        return 1;
    }

    private static int forceSpawnBoss(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        int floor = IntegerArgumentType.getInteger(ctx, "floor");
        CommandSourceStack source = ctx.getSource();

        ServerLevel level;
        try {
            level = DescentDimensions.getFloorLevel(source.getServer(), floor);
        } catch (IllegalStateException notLoaded) {
            source.sendFailure(Component.literal(notLoaded.getMessage()));
            return 0;
        }

        return BossRegistry.forFloor(floor).map(boss -> {
            BlockPos spawnPos = DescentDimensions.findSafeEntryPosition(level);
            boolean spawned = BossFactory.spawn(level, boss, spawnPos).isPresent();
            if (spawned) {
                source.sendSuccess(() -> Component.literal("Spawned floor " + floor + "'s boss."), true);
                return 1;
            }
            source.sendFailure(Component.literal("Floor " + floor + "'s boss entity type failed to instantiate."));
            return 0;
        }).orElseGet(() -> {
            source.sendFailure(Component.literal("No boss is registered for floor " + floor + "."));
            return 0;
        });
    }

    private static int teleportToFloor(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        int floor = IntegerArgumentType.getInteger(ctx, "floor");
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayerOrException();

        ServerLevel level;
        try {
            level = DescentDimensions.getFloorLevel(source.getServer(), floor);
        } catch (IllegalStateException notLoaded) {
            source.sendFailure(Component.literal(notLoaded.getMessage()));
            return 0;
        }

        BlockPos entry = DescentDimensions.findSafeEntryPosition(level);
        player.teleportTo(level, entry.getX() + 0.5, entry.getY(), entry.getZ() + 0.5, Set.of(), player.getYRot(), player.getXRot(), false);
        source.sendSuccess(() -> Component.literal("Teleported to Floor " + floor + "."), true);
        return 1;
    }

    private static int showLoadedSchedule(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        source.sendSuccess(() -> Component.literal("Loaded floor schedule (" + FloorConfigManager.FLOOR_COUNT + " floors):"), false);
        FloorConfigManager.all().values().stream()
                .sorted(java.util.Comparator.comparingInt(FloorDefinition::floorNumber))
                .forEach(def -> source.sendSuccess(() -> Component.literal(String.format(
                        "  Floor %2d: %-20s timer=%3dd stairs=%d%s%s",
                        def.floorNumber(), def.theme().getPath(), def.timerDays(), def.stairCount(),
                        def.townFloor() ? " [TOWN]" : "", def.bossFloor() ? " [BOSS]" : "")), false));
        return 1;
    }

    private static int showDerivedSeed(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        int floor = IntegerArgumentType.getInteger(ctx, "floor");
        CommandSourceStack source = ctx.getSource();
        long worldSeed = source.getServer().overworld().getSeed();
        long floorSeed = DeterministicSeed.deriveFloorSeed(worldSeed, floor);
        source.sendSuccess(() -> Component.literal("Floor " + floor + " deterministic seed: " + floorSeed
                + " (derived from world seed " + worldSeed + ")"), false);
        return 1;
    }

    private static int generateFloor(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        int floor = IntegerArgumentType.getInteger(ctx, "floor");
        CommandSourceStack source = ctx.getSource();
        MinecraftServer server = source.getServer();

        ServerLevel level;
        try {
            level = DescentDimensions.getFloorLevel(server, floor);
        } catch (IllegalStateException notLoaded) {
            source.sendFailure(Component.literal(notLoaded.getMessage()));
            return 0;
        }

        DescentSavedData saveData = DescentSavedData.get(server);
        long seed = DeterministicSeed.deriveFloorSeed(server.overworld().getSeed(), floor);
        FloorSaveState state = saveData.getFloor(floor).orElseGet(() -> FloorSaveState.empty(floor, seed, level.getGameTime()));

        if (state.staircasesPlaced()) {
            source.sendSuccess(() -> Component.literal("Floor " + floor + " already has its staircases placed ("
                    + state.staircases().size() + "). Delete the world's Descent save data to force regeneration."), false);
            return 1;
        }

        FloorDefinition definition = FloorConfigManager.get(floor);
        List<StaircaseRecord> staircases = StaircasePlacer.placeStaircases(level, floor, state.seed(), definition.stairCount());
        saveData.putFloor(state.withStaircases(staircases));

        source.sendSuccess(() -> Component.literal("Floor " + floor + ": placed " + staircases.size()
                + " staircase(s) around the world origin."), true);
        return 1;
    }

    private static int listStaircases(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        OptionalInt floorNumber = DescentDimensions.floorNumberFromKey(source.getLevel().dimension());
        if (floorNumber.isEmpty()) {
            source.sendFailure(Component.literal("You are not currently on a Descent dungeon floor."));
            return 0;
        }

        int floor = floorNumber.getAsInt();
        DescentSavedData saveData = DescentSavedData.get(source.getServer());
        List<StaircaseRecord> staircases = saveData.getFloor(floor)
                .map(FloorSaveState::staircases)
                .orElse(List.of());

        if (staircases.isEmpty()) {
            source.sendSuccess(() -> Component.literal("Floor " + floor + " has no staircases placed yet "
                    + "(use /descent generate " + floor + " or wait for a player to enter)."), false);
            return 1;
        }

        source.sendSuccess(() -> Component.literal("Floor " + floor + ": " + staircases.size() + " staircase(s):"), false);
        for (StaircaseRecord staircase : staircases) {
            BlockPos pos = staircase.position();
            source.sendSuccess(() -> Component.literal(String.format("  (%d, %d, %d)%s%s",
                    pos.getX(), pos.getY(), pos.getZ(),
                    staircase.discovered() ? " [discovered]" : " [hidden]",
                    staircase.finalStaircase() ? " [FINAL EXIT]" : "")), false);
        }
        return 1;
    }
}
