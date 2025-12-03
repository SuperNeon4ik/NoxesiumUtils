package me.superneon4ik.noxesiumutils.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.noxcrew.noxesium.api.protocol.rule.ServerRuleIndices;
import com.noxcrew.noxesium.api.qib.QibDefinition;
import com.noxcrew.noxesium.api.util.DebugOption;
import com.noxcrew.noxesium.paper.api.rule.GraphicsType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.ArgumentResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import me.superneon4ik.noxesiumutils.NoxesiumUtils;
import me.superneon4ik.noxesiumutils.commands.arguments.DebugOptionsListArgument;
import me.superneon4ik.noxesiumutils.commands.arguments.QibDefinitionListArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unchecked")
public class ServerRuleCommands {
    private final NoxesiumUtils noxesiumUtils;

    public ServerRuleCommands(NoxesiumUtils noxesiumUtils) {
        this.noxesiumUtils = noxesiumUtils;
    }

    /**
     * Generate all needed server rule commands
     */
    public List<LiteralArgumentBuilder<CommandSourceStack>> generate() {
        List<LiteralArgumentBuilder<CommandSourceStack>> commands = new LinkedList<>();
        commands.add(booleanRule("disableSpinAttackCollisions", ServerRuleIndices.DISABLE_SPIN_ATTACK_COLLISIONS));
        commands.add(booleanRule("cameraLocked", ServerRuleIndices.CAMERA_LOCKED));
        commands.add(booleanRule("disableVanillaMusic", ServerRuleIndices.DISABLE_VANILLA_MUSIC));
        commands.add(booleanRule("disableBoatCollisions", ServerRuleIndices.DISABLE_BOAT_COLLISIONS));
        commands.add(booleanRule("showMapInUi", ServerRuleIndices.SHOW_MAP_IN_UI));
        commands.add(booleanRule("disableDeferredChunkUpdates", ServerRuleIndices.DISABLE_DEFERRED_CHUNK_UPDATES));
        commands.add(booleanRule("disableMapUi", ServerRuleIndices.DISABLE_MAP_UI));

        // TODO: Implement Noxcrew's recommendations
        // https://github.com/Noxcrew/noxesium/blob/4b3f93fe6886eac60dbfffa6cb125e1e5a31886a/api/src/main/java/com/noxcrew/noxesium/api/protocol/rule/ServerRuleIndices.java#L85
        commands.add(booleanRule("enableSmootherClientTrident", ServerRuleIndices.ENABLE_SMOOTHER_CLIENT_TRIDENT));

        commands.add(integerRule("heldItemNameOffset", ServerRuleIndices.HELD_ITEM_NAME_OFFSET));
        commands.add(integerRule("riptideCoyoteTime", ServerRuleIndices.RIPTIDE_COYOTE_TIME));
        commands.add(integerRule("riptidePreCharging", ServerRuleIndices.RIPTIDE_PRE_CHARGING));

        commands.add(itemStackRule("handItemOverride", ServerRuleIndices.HAND_ITEM_OVERRIDE));

        commands.add(valueToggleCommand("customCreativeItems", ServerRuleIndices.CUSTOM_CREATIVE_ITEMS,
                noxesiumUtils.getConfig().getCustomCreativeItems()));

        commands.add(graphicsTypeCommand("overrideGraphicsMode", ServerRuleIndices.OVERRIDE_GRAPHICS_MODE));
        commands.add(qibBehaviorListCommand("qibBehaviors", ServerRuleIndices.QIB_BEHAVIORS));
        commands.add(debugOptionsCommand("restrictDebugOptions", ServerRuleIndices.RESTRICT_DEBUG_OPTIONS));

        commands.add(generateResetCommand());

        return commands;
    }

    private LiteralArgumentBuilder<CommandSourceStack> generateResetCommand() {
        return Commands.literal("reset")
                .then(Commands.argument("players", ArgumentTypes.players())
                        .executes(ctx -> {
                            final var playerSelectorArgumentResolver = ctx.getArgument("players", PlayerSelectorArgumentResolver.class);
                            final var players = playerSelectorArgumentResolver.resolve(ctx.getSource());

                            AtomicInteger updates = new AtomicInteger();
                            players.forEach(player -> {
                                boolean hasChanged = false;
                                for (Integer idx : noxesiumUtils.getManager().getServerRules().getContents().keySet()) {
                                    if (noxesiumUtils.getManager().resetServerRule(player, idx))
                                        hasChanged = true;
                                }

                                if (hasChanged)
                                    updates.incrementAndGet();
                            });

                            ctx.getSource().getSender().sendMessage(Component.text(updates.get() + " player(s) affected.", NamedTextColor.GREEN));
                            return Command.SINGLE_SUCCESS;
                        }));
    }

    public LiteralArgumentBuilder<CommandSourceStack> resetCommand(int index) {
        return Commands.literal("reset")
                .executes(ctx -> {
                    final var playerSelectorArgumentResolver = ctx.getArgument("players", PlayerSelectorArgumentResolver.class);
                    final var players = playerSelectorArgumentResolver.resolve(ctx.getSource());
                    resetServerRule(ctx.getSource().getSender(), players, index);
                    return Command.SINGLE_SUCCESS;
                });
    }

    /**
     * Command that sets the rule's value to the value
     * returned by the provided CommandAPI Argument.
     * Also adds a {@link ServerRuleCommands#resetCommand(int)
     */
    public LiteralArgumentBuilder<CommandSourceStack> argumentRule(String name, int index, ArgumentType<?> argument) {
        return Commands.literal(name)
                .then(
                        Commands.argument("players", ArgumentTypes.players())
                                .then(Commands.argument("value", argument)
                                        .executes(ctx -> {
                                            final var playerSelectorArgumentResolver = ctx.getArgument("players", PlayerSelectorArgumentResolver.class);
                                            final var players = playerSelectorArgumentResolver.resolve(ctx.getSource());

                                            Object value;
                                            var valueTypeOrResolver = ctx.getArgument("value", Object.class);
                                            if (valueTypeOrResolver instanceof ArgumentResolver<?> resolver) {
                                                value = resolver.resolve(ctx.getSource());
                                            }
                                            else {
                                                value = valueTypeOrResolver;
                                            }

                                            updateServerRule(ctx.getSource().getSender(), players, index, value);
                                            return Command.SINGLE_SUCCESS;
                                        }))
                                .then(resetCommand(index))
                );
    }

    public LiteralArgumentBuilder<CommandSourceStack> booleanRule(String name, int index) {
        return argumentRule(name, index, BoolArgumentType.bool());
    }

    public LiteralArgumentBuilder<CommandSourceStack> integerRule(String name, int index) {
        return argumentRule(name, index, IntegerArgumentType.integer());
    }

    public LiteralArgumentBuilder<CommandSourceStack> itemStackRule(String name, int index) {
        return argumentRule(name, index, ArgumentTypes.itemStack());
    }

    public LiteralArgumentBuilder<CommandSourceStack> valueToggleCommand(String name, int index, Object value) {
        return Commands.literal(name)
                .then(
                        Commands.argument("players", ArgumentTypes.players())
                                .then(Commands.argument("toggle", BoolArgumentType.bool())
                                    .executes(ctx -> {
                                        final var playerSelectorArgumentResolver = ctx.getArgument("players", PlayerSelectorArgumentResolver.class);
                                        final var players = playerSelectorArgumentResolver.resolve(ctx.getSource());

                                        final var toggleState = ctx.getArgument("toggle", boolean.class);

                                        if (toggleState)
                                            updateServerRule(ctx.getSource().getSender(), players, index, value);
                                        else
                                            resetServerRule(ctx.getSource().getSender(), players, index);

                                        return Command.SINGLE_SUCCESS;
                                    }))
                                .then(resetCommand(index))
                );
    }

    public LiteralArgumentBuilder<CommandSourceStack> graphicsTypeCommand(String name, int index) {
        var playerArgumentCommandBuilder = Commands.argument("players", ArgumentTypes.players());

        for (GraphicsType type : GraphicsType.values()) {
            playerArgumentCommandBuilder
                    .then(Commands.literal(type.name().toLowerCase(Locale.ROOT))
                            .executes(ctx -> {
                                final var playerSelectorArgumentResolver = ctx.getArgument("players", PlayerSelectorArgumentResolver.class);
                                final var players = playerSelectorArgumentResolver.resolve(ctx.getSource());
                                updateServerRule(ctx.getSource().getSender(), players, index, Optional.of(type));
                                return Command.SINGLE_SUCCESS;
                            }));
        }

        playerArgumentCommandBuilder
                .then(resetCommand(index));

        return Commands.literal(name).then(playerArgumentCommandBuilder);
    }

    public LiteralArgumentBuilder<CommandSourceStack> qibBehaviorListCommand(String name, int index) {
        return Commands.literal(name)
                .then(Commands.argument("players", ArgumentTypes.players())
                        .then(Commands.literal("*")
                                .executes(ctx -> {
                                    final var playerSelectorArgumentResolver = ctx.getArgument("players", PlayerSelectorArgumentResolver.class);
                                    final var players = playerSelectorArgumentResolver.resolve(ctx.getSource());

                                    noxesiumUtils.getLogger().info("Definitions: " + noxesiumUtils.getConfig().getQibDefinitions());
                                    updateServerRule(ctx.getSource().getSender(), players, index, noxesiumUtils.getConfig().getQibDefinitions());

                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(Commands.argument("definitions", new QibDefinitionListArgument(noxesiumUtils))
                                .executes(ctx -> {
                                    final var playerSelectorArgumentResolver = ctx.getArgument("players", PlayerSelectorArgumentResolver.class);
                                    final var players = playerSelectorArgumentResolver.resolve(ctx.getSource());
                                    final Set<Map.Entry<String, QibDefinition>> definitions = ctx.getArgument("definitions", Set.class);

                                    Map<String, QibDefinition> mappedEffects = new HashMap<>();
                                    definitions.forEach(effect -> mappedEffects.put(effect.getKey(), effect.getValue()));
                                    noxesiumUtils.getLogger().info("Definitions: " + definitions);
                                    updateServerRule(ctx.getSource().getSender(), players, index, mappedEffects);

                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(resetCommand(index))
                );
    }

    public LiteralArgumentBuilder<CommandSourceStack> debugOptionsCommand(String name, int index) {
        return Commands.literal(name)
                .then(Commands.argument("players", ArgumentTypes.players())
                        .then(Commands.literal("*")
                                .executes(ctx -> {
                                    final var playerSelectorArgumentResolver = ctx.getArgument("players", PlayerSelectorArgumentResolver.class);
                                    final var players = playerSelectorArgumentResolver.resolve(ctx.getSource());

                                    var optionIndexes = Arrays.stream(DebugOption.values())
                                            .map(DebugOption::getKeyCode)
                                            .toList();

                                    updateServerRule(ctx.getSource().getSender(), players, index, optionIndexes);

                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(Commands.argument("options", new DebugOptionsListArgument())
                                .executes(ctx -> {
                                    final var playerSelectorArgumentResolver = ctx.getArgument("players", PlayerSelectorArgumentResolver.class);
                                    final var players = playerSelectorArgumentResolver.resolve(ctx.getSource());
                                    final Set<DebugOption> debugOptions = ctx.getArgument("options", Set.class);

                                    var optionIndexes = debugOptions.stream()
                                            .map(DebugOption::getKeyCode)
                                            .toList();

                                    updateServerRule(ctx.getSource().getSender(), players, index, optionIndexes);

                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(resetCommand(index)));
    }

    // ---

    private void updateServerRule(@Nullable CommandSender sender, Collection<Player> players, Integer index, Object value) {
        if (players == null) return;
        AtomicInteger updates = new AtomicInteger();
        players.forEach(player -> {
            if (noxesiumUtils.getManager().setServerRule(player, index, value))
                updates.getAndIncrement();
        });

        if (sender != null)
            sender.sendMessage(Component.text(updates.get() + " player(s) affected.", NamedTextColor.GREEN));
    }

    private void resetServerRule(@Nullable CommandSender sender, Collection<Player> players, Integer index) {
        if (players == null) return;
        AtomicInteger updates = new AtomicInteger();
        players.forEach(player -> {
            if (noxesiumUtils.getManager().resetServerRule(player, index))
                updates.getAndIncrement();
        });

        if (sender != null)
            sender.sendMessage(Component.text(updates.get() + " player(s) affected.", NamedTextColor.GREEN));
    }

}
