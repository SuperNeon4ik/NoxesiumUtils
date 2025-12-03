package me.superneon4ik.noxesiumutils.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import me.superneon4ik.noxesiumutils.NoxesiumUtils;
import me.superneon4ik.noxesiumutils.modules.ModrinthUpdateChecker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Registers {@code /noxesiumutils} commands for the plugin.
 * <p>
 * If your plugin depends on NoxesiumUtils you shouldn't use this.
 * However, if you shade NoxesiumUtils into your plugin, and you want
 * the commands you can simply register them:
 * <pre>{@code
 * var registrar = new CommandRegistrar(this, noxesiumUtils, updateChecker);
 * registrar.registerCommands();
 * }</pre>
 *
 * The reload command is not built into the registrar.
 * You can refer to {@code NoxesiumUtilsPlugin#registerCommands()} for
 * an example on how to implement the reload command.
 */
public class CommandBuilder {
    private final NoxesiumUtils noxesiumUtils;
    @Nullable private final ModrinthUpdateChecker updateChecker;
    
    private final List<LiteralArgumentBuilder<CommandSourceStack>> additionalCommands = new LinkedList<>();
    
    public CommandBuilder(NoxesiumUtils noxesiumUtils, @Nullable ModrinthUpdateChecker updateChecker) {
        this.noxesiumUtils = noxesiumUtils;
        this.updateChecker = updateChecker;
    }
    
    /**
     * Add an additional command that should be registered
     * to {@code /noxesiumutils} (as subcommands)
     */
    public void addAdditionalCommand(LiteralArgumentBuilder<CommandSourceStack> command) {
        additionalCommands.add(command);
    }

    /**
     * Add a list of additional commands that should be registered
     * to {@code /noxesiumutils} (as subcommands)
     */
    public void addAdditionalCommands(List<LiteralArgumentBuilder<CommandSourceStack>> commands) {
        additionalCommands.addAll(commands);
    }
    
    /**
     * Registers all the {@code /noxesiumutils} commands.
     * Also tries to unregister existing commands, so it
     * can be used to reload commands, however Paper will
     * sometimes throw an exception in an async thread,
     * which doesn't break anything, but is annoying.
     */
//    public void registerCommands() {
//        // Unregister existing commands in case of reload
//        CommandAPI.unregister("noxesiumutils", true);
//
//        // Generate the commands
//        List<LiteralArgumentBuilder<CommandSourceStack>> subcommands = generateGenericSubcommands();
//        List<LiteralArgumentBuilder<CommandSourceStack>> serverRulesSubcommands = new ServerRuleCommands(noxesiumUtils).generate();
//        List<LiteralArgumentBuilder<CommandSourceStack>> entityRulesSubcommands = new EntityRuleCommands(noxesiumUtils).generate();
//
//        // Add server rule subcommands to the list
//        subcommands.add(
//                new CommandAPICommand("serverRules")
//                        .withPermission("noxesiumutils.serverrules")
//                        .withSubcommands(serverRulesSubcommands.toArray(new CommandAPICommand[0]))
//        );
//
//        // Add entity rule subcommands to the list
//        subcommands.add(
//                new CommandAPICommand("entityRules")
//                        .withPermission("noxesiumutils.entityrules")
//                        .withSubcommands(entityRulesSubcommands.toArray(new CommandAPICommand[0]))
//        );
//
//        // Add additional custom commands if defined
//        if (!additionalCommands.isEmpty()) {
//            subcommands.addAll(additionalCommands);
//        }
//
//        // Register the commands
//        new CommandAPICommand("noxesiumutils")
//                .withAliases("noxutils")
//                .withPermission("noxesiumutils.about")
//                .withSubcommands(subcommands.toArray(new CommandAPICommand[0]))
//                .executes(((sender, args) -> {
//                    var url = Component.text("https://github.com/SuperNeon4ik/NoxesiumUtils#readme", NamedTextColor.YELLOW)
//                            .clickEvent(ClickEvent.openUrl("https://github.com/SuperNeon4ik/NoxesiumUtils#readme"))
//                            .hoverEvent(Component.text("Click to open in browser!", NamedTextColor.GREEN, TextDecoration.ITALIC).asHoverEvent());
//
//                    sender.sendMessage(Component.text("For help refer to ", NamedTextColor.GREEN).append(url));
//                    if (updateChecker != null) {
//                        sender.sendMessage(Component.text("Checking for updates...", NamedTextColor.DARK_GRAY));
//                        var future = updateChecker.checkForUpdates();
//                        future.thenAccept(versionStatus -> {
//                            var versionMessage = updateChecker.generateVersionMessage(versionStatus);
//                            sender.sendMessage(versionMessage);
//                        });
//                    }
//                }))
//                .register(plugin);
//    }

    public LiteralCommandNode<CommandSourceStack> build() {
        var command = Commands.literal("noxesiumutils");

        // Generate the commands
        List<LiteralArgumentBuilder<CommandSourceStack>> subcommands = generateGenericSubcommands();
        List<LiteralArgumentBuilder<CommandSourceStack>> serverRulesSubcommands = new ServerRuleCommands(noxesiumUtils).generate();
        List<LiteralArgumentBuilder<CommandSourceStack>> entityRulesSubcommands = new EntityRuleCommands(noxesiumUtils).generate();

        // Populate serverRules subcommands
        var serverRulesSubcommand = Commands.literal("serverRules")
                .requires(ctx -> ctx.getSender().hasPermission("noxesiumutils.serverrules"));

        for (var subcommand : serverRulesSubcommands) {
            serverRulesSubcommand.then(subcommand);
        }

        // Populate entityRules subcommands
        var entityRulesSubcommand = Commands.literal("entityRules")
                .requires(ctx -> ctx.getSender().hasPermission("noxesiumutils.entityrules"));

        for (var subcommand : entityRulesSubcommands) {
            entityRulesSubcommand.then(subcommand);
        }

        subcommands.add(serverRulesSubcommand);
        subcommands.add(entityRulesSubcommand);

        // Add subcommands to the command
        for (var subcommand : subcommands) {
            command.then(subcommand);
        }

        // Add additional subcommands
        for (var additionalCommand : additionalCommands) {
            command.then(additionalCommand);
        }

        // Root executor
        command.requires(ctx -> ctx.getSender().hasPermission("noxesiumutils.about"));
        command.executes(ctx -> {
            final var sender = ctx.getSource().getSender();

            var url = Component.text("https://github.com/SuperNeon4ik/NoxesiumUtils#readme", NamedTextColor.YELLOW)
                            .clickEvent(ClickEvent.openUrl("https://github.com/SuperNeon4ik/NoxesiumUtils#readme"))
                            .hoverEvent(Component.text("Click to open in browser!", NamedTextColor.GREEN, TextDecoration.ITALIC).asHoverEvent());

            sender.sendMessage(Component.text("For help refer to ", NamedTextColor.GREEN).append(url));
            if (updateChecker != null) {
                sender.sendMessage(Component.text("Checking for updates...", NamedTextColor.DARK_GRAY));
                var future = updateChecker.checkForUpdates();
                future.thenAccept(versionStatus -> {
                    var versionMessage = updateChecker.generateVersionMessage(versionStatus);
                    sender.sendMessage(versionMessage);
                });
            }

            return Command.SINGLE_SUCCESS;
        });

        return command.build();
    }

    private List<LiteralArgumentBuilder<CommandSourceStack>> generateGenericSubcommands() {
        List<LiteralArgumentBuilder<CommandSourceStack>> subcommands = new ArrayList<>(2);
        subcommands.add(
                Commands.literal("check")
                        .requires(ctx -> ctx.getSender().hasPermission("minecraft.command.selector") &&
                                ctx.getSender().hasPermission("noxesiumutils.check"))
                        .then(Commands.argument("player", ArgumentTypes.player())
                                .executes(ctx -> {
                                    final var playerSelectorArgumentResolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                                    final var player = playerSelectorArgumentResolver.resolve(ctx.getSource()).getFirst();

                                    Integer protocolVersion = noxesiumUtils.getManager().getProtocolVersion(player);
                                    if (protocolVersion == null) {
                                        ctx.getSource().getSender().sendMessage(Component.text(player.getName() + " doesn't have Noxesium installed.", NamedTextColor.RED));
                                        return 0;
                                    }

                                    ctx.getSource().getSender().sendMessage(Component.text(player.getName() + " has Noxesium installed.", NamedTextColor.GREEN)
                                            .append(Component.text(" (Protocol Version: " + protocolVersion + ")", NamedTextColor.YELLOW)));

                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(Commands.argument("players", ArgumentTypes.players())
                                .executes(ctx -> {
                                    final var playerSelectorArgumentResolver = ctx.getArgument("players", PlayerSelectorArgumentResolver.class);
                                    final var players = playerSelectorArgumentResolver.resolve(ctx.getSource());

                                    List<Component> strings = new ArrayList<>(players.size());
                                    for (var player : players) {
                                        Integer protocolVersion = noxesiumUtils.getManager().getProtocolVersion(player);
                                        if (protocolVersion == null) {
                                            strings.add(Component.text(player.getName(), NamedTextColor.RED));
                                            continue;
                                        }

                                        strings.add(Component.text(player.getName(), NamedTextColor.GREEN)
                                                .append(Component.text(" (%s)".formatted(protocolVersion), NamedTextColor.YELLOW)));
                                    }

                                    ctx.getSource().getSender().sendMessage(Component.join(JoinConfiguration.separator(Component.text(", ", NamedTextColor.DARK_GRAY)), strings));
                                    return Command.SINGLE_SUCCESS;
                                }))
        );

        subcommands.add(
            Commands.literal("clientSettings")
                    .requires(ctx -> ctx.getSender().hasPermission("minecraft.command.selector") &&
                            ctx.getSender().hasPermission("noxesiumutils.check"))
                    .then(Commands.argument("player", ArgumentTypes.player())
                            .executes(ctx -> {
                                final var playerSelectorArgumentResolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                                final var player = playerSelectorArgumentResolver.resolve(ctx.getSource()).getFirst();

                                var clientSettings = noxesiumUtils.getManager().getClientSettings(player);
                                if (clientSettings == null) {
                                    ctx.getSource().getSender().sendMessage(Component.text(String.format("%s doesn't have Noxesium installed or didn't yet provide their settings.",
                                            player.getName()), NamedTextColor.RED));
                                    return 0;
                                }

                                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                                String json = gson.toJson(clientSettings);
                                ctx.getSource().getSender().sendMessage(json);

                                return Command.SINGLE_SUCCESS;
                            }))

        );

        return subcommands;
    }
}
