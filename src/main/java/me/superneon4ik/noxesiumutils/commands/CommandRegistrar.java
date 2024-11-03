package me.superneon4ik.noxesiumutils.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import me.superneon4ik.noxesiumutils.NoxesiumUtils;
import me.superneon4ik.noxesiumutils.modules.ModrinthUpdateChecker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.Collection;
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
@SuppressWarnings("unchecked")
public class CommandRegistrar {
    private final JavaPlugin plugin;
    private final NoxesiumUtils noxesiumUtils;
    @Nullable private final ModrinthUpdateChecker updateChecker;
    
    private final List<CommandAPICommand> additionalCommands = new LinkedList<>();
    
    public CommandRegistrar(JavaPlugin plugin, NoxesiumUtils noxesiumUtils, @Nullable ModrinthUpdateChecker updateChecker) {
        this.plugin = plugin;
        this.noxesiumUtils = noxesiumUtils;
        this.updateChecker = updateChecker;
    }
    
    /**
     * Add an additional command that should be registered
     * to {@code /noxesiumutils} (as subcommands)
     */
    public void addAdditionalCommand(CommandAPICommand command) {
        additionalCommands.add(command);
    }

    /**
     * Add a list of additional commands that should be registered
     * to {@code /noxesiumutils} (as subcommands)
     */
    public void addAdditionalCommands(List<CommandAPICommand> commands) {
        additionalCommands.addAll(commands);
    }
    
    /**
     * Registers all the {@code /noxesiumutils} commands.
     * Also tries to unregister existing commands, so it
     * can be used to reload commands, however Paper will
     * sometimes throw an exception in an async thread,
     * which doesn't break anything, but is annoying.
     */
    public void registerCommands() {
        // Unregister existing commands in case of reload
        CommandAPI.unregister("noxesiumutils", true);
        
        // Generate the commands
        List<CommandAPICommand> subcommands = generateGenericSubcommands();
        List<CommandAPICommand> serverRulesSubcommands = new ServerRuleCommands(noxesiumUtils).generate();
        List<CommandAPICommand> entityRulesSubcommands = new EntityRuleCommands(noxesiumUtils).generate();
        
        // Add server rule subcommands to the list
        subcommands.add(
                new CommandAPICommand("serverRules")
                        .withPermission("noxesiumutils.serverrules")
                        .withSubcommands(serverRulesSubcommands.toArray(new CommandAPICommand[0]))
        );

        // Add entity rule subcommands to the list
        subcommands.add(
                new CommandAPICommand("entityRules")
                        .withPermission("noxesiumutils.entityrules")
                        .withSubcommands(entityRulesSubcommands.toArray(new CommandAPICommand[0]))
        );
        
        // Add additional custom commands if defined
        if (!additionalCommands.isEmpty()) {
            subcommands.addAll(additionalCommands);
        }

        // Register the commands
        new CommandAPICommand("noxesiumutils")
                .withAliases("noxutils")
                .withPermission("noxesiumutils.about")
                .withSubcommands(subcommands.toArray(new CommandAPICommand[0]))
                .executes(((sender, args) -> {
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
                }))
                .register(plugin);
    }

    private List<CommandAPICommand> generateGenericSubcommands() {
        List<CommandAPICommand> subcommands = new LinkedList<>();
        subcommands.add(
                new CommandAPICommand("check")
                        .withPermission("noxesiumutils.check")
                        .withArguments(new EntitySelectorArgument.OnePlayer("player"))
                        .executes((sender, args) -> {
                            Player player = (Player) args.get("player");
                            assert player != null;
                            Integer protocolVersion = noxesiumUtils.getManager().getProtocolVersion(player);
                            if (protocolVersion == null) {
                                sender.sendMessage(Component.text(player.getName() + " doesn't have Noxesium installed.", NamedTextColor.RED));
                                return;
                            }

                            sender.sendMessage(Component.text(player.getName() + " has Noxesium installed.", NamedTextColor.GREEN)
                                    .append(Component.text(" (Protocol Version: " + protocolVersion + ")", NamedTextColor.YELLOW)));
                        })
        );
        subcommands.add(
                new CommandAPICommand("check")
                        .withPermission("noxesiumutils.check")
                        .withArguments(new EntitySelectorArgument.ManyPlayers("players"))
                        .executes((sender, args) -> {
                            Collection<Player> players = (Collection<Player>) args.get("players");
                            LinkedList<Component> strings = new LinkedList<>();
                            assert players != null;
                            for (Player player : players) {
                                Integer protocolVersion = noxesiumUtils.getManager().getProtocolVersion(player);
                                if (protocolVersion == null) {
                                    strings.add(Component.text(player.getName(), NamedTextColor.RED));
                                    continue;
                                }

                                strings.add(Component.text(player.getName(), NamedTextColor.GREEN)
                                        .append(Component.text(" (%s)".formatted(protocolVersion), NamedTextColor.YELLOW)));
                            }

                            sender.sendMessage(Component.join(JoinConfiguration.separator(Component.text(", ", NamedTextColor.DARK_GRAY)), strings));
                        })
        );
        subcommands.add(
                new CommandAPICommand("clientSettings")
                        .withPermission("noxesiumutils.check")
                        .withArguments(new EntitySelectorArgument.OnePlayer("player"))
                        .executes((sender, args) -> {
                            Player player = (Player) args.get("player");
                            assert player != null;
                            var clientSettings = noxesiumUtils.getManager().getClientSettings(player);
                            if (clientSettings == null) {
                                sender.sendMessage(Component.text(String.format("%s doesn't have Noxesium installed or didn't yet provide their settings.",
                                        player.getName()), NamedTextColor.RED));
                                return;
                            }

                            Gson gson = new GsonBuilder().setPrettyPrinting().create();
                            String json = gson.toJson(clientSettings);
                            sender.sendMessage(json);
                        })
        );
        return subcommands;
    }
}
