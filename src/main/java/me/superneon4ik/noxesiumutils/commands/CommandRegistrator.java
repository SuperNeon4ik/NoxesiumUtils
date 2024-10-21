package me.superneon4ik.noxesiumutils.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import me.superneon4ik.noxesiumutils.NoxesiumUtils;
import me.superneon4ik.noxesiumutils.OldNoxesiumUtilsImpl;
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

@SuppressWarnings("unchecked")
public class CommandRegistrator {
    private final JavaPlugin plugin;
    private final NoxesiumUtils noxesiumUtils;
    @Nullable private final ModrinthUpdateChecker updateChecker;
    
    public CommandRegistrator(JavaPlugin plugin, NoxesiumUtils noxesiumUtils, @Nullable ModrinthUpdateChecker updateChecker) {
        this.plugin = plugin;
        this.noxesiumUtils = noxesiumUtils;
        this.updateChecker = updateChecker;
    }
    
    public void registerCommands() {
        // Unregister existing commands in case of reload
        CommandAPI.unregister("noxesiumutils", true);
        
        
        List<CommandAPICommand> subcommands = generateGenericSubcommands();
        List<CommandAPICommand> serverRulesSubcommands = new ServerRuleCommands(noxesiumUtils).generate();
        // TODO: Entity rules
//        List<CommandAPICommand> entityRulesSubcommands = generateEntityRulesSubcommands();
        
        // Add server rule subcommands to the list
        subcommands.add(
                new CommandAPICommand("serverRules")
                        .withPermission("noxesiumutils.serverrules")
                        .withSubcommands(serverRulesSubcommands.toArray(new CommandAPICommand[0]))
        );

        // Add entity rule subcommands to the list
//        subcommands.add(
//                new CommandAPICommand("entityRules")
//                        .withPermission("noxesiumutils.entityrules")
//                        .withSubcommands(entityRulesSubcommands.toArray(new CommandAPICommand[0]))
//        );

        new CommandAPICommand("noxesiumutils")
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
                            var versionMessage = OldNoxesiumUtilsImpl.getUpdateChecker().generateVersionMessage(versionStatus);
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
                            Collection<Player> players = (Collection<Player>) args.get("player");
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
