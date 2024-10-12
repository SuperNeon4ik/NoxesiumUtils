package me.superneon4ik.noxesiumutils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import lombok.Getter;
import me.superneon4ik.noxesiumutils.config.NoxesiumUtilsConfigBuilder;
import me.superneon4ik.noxesiumutils.config.RuleIndex;
import me.superneon4ik.noxesiumutils.config.ServerRuleDefaults;
import me.superneon4ik.noxesiumutils.listeners.PlayerJoinEventListener;
import me.superneon4ik.noxesiumutils.modules.ModrinthUpdateChecker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class NoxesiumUtilsPlugin extends JavaPlugin {
    @Getter private static NoxesiumUtilsPlugin instance;
    @Getter private static ModrinthUpdateChecker updateChecker;
    @Getter private static NoxesiumUtils noxesiumUtils;
    
    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        
        var config = new NoxesiumUtilsConfigBuilder()
                .withConfig(getConfig())
                .withQibFolder(Path.of(getDataFolder().getPath(), "qibs").toFile())
                .withLogger(getLogger())
                .build();
        noxesiumUtils = new NoxesiumUtils(this, config, getLogger());
        noxesiumUtils.register();

        // Register update checker
        updateChecker = new ModrinthUpdateChecker(this, "noxesiumutils");
        if (config.isCheckForUpdates())
            updateChecker.beginChecking(5 * 60 * 20); // every 5 minutes
        
        getServer().getPluginManager().registerEvents(new PlayerJoinEventListener(updateChecker), this);
    }

    @Override
    public void onDisable() {
        noxesiumUtils.unregister();
        instance = null;
    }
    
    @SuppressWarnings("unchecked")
    private void registerCommands() {
        List<CommandAPICommand> subcommands = new LinkedList<>();
        List<CommandAPICommand> serverRulesSubcommands = generateServerRulesSubcommands();
        List<CommandAPICommand> entityRulesSubcommands = generateEntityRulesSubcommands();
        
        // Add generic subcommands to the list
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
        
        new CommandAPICommand("noxesiumutils")
                .withPermission("noxesiumutils.about")
                .withSubcommands(subcommands.toArray(new CommandAPICommand[0]))
                .executes(((sender, args) -> {
                    var url = Component.text("https://github.com/SuperNeon4ik/NoxesiumUtils#readme", NamedTextColor.YELLOW)
                            .clickEvent(ClickEvent.openUrl("https://github.com/SuperNeon4ik/NoxesiumUtils#readme"))
                            .hoverEvent(Component.text("Click to open in browser!", NamedTextColor.GREEN, TextDecoration.ITALIC).asHoverEvent());

                    sender.sendMessage(Component.text("For help refer to ", NamedTextColor.GREEN).append(url));
                    sender.sendMessage(Component.text("Checking for updates...", NamedTextColor.DARK_GRAY));
                    var future = updateChecker.checkForUpdates();
                    future.thenAccept(versionStatus -> {
                        var versionMessage = OldNoxesiumUtilsImpl.getUpdateChecker().generateVersionMessage(versionStatus);
                        sender.sendMessage(versionMessage);
                    });
                }))
                .register(this);
    }
    
    @SuppressWarnings("unchecked")
    private List<CommandAPICommand> generateServerRulesSubcommands() {
        List<CommandAPICommand> commands = new LinkedList<>();

        var clazz = ServerRuleDefaults.class;
        var fields = clazz.getDeclaredFields();
        for (var field : fields) {
            var fieldName = field.getName();

            try {
                field.setAccessible(true);

                if (field.isAnnotationPresent(RuleIndex.class)) {
                    int index = field.getAnnotation(RuleIndex.class).index();

                    // TODO: Unfinished section
                    
                    var command = new CommandAPICommand(fieldName);
                    command.setPermission(CommandPermission.fromString("noxesiumutils.command." + fieldName));
                    Argument<?> valueArgument = null;
                    ProcessObjectFunction processor = (input) -> input; // by default - do not convert anything
                    if (field.getType() == Boolean.class) {
                        valueArgument = new BooleanArgument("value");
                        
                    }
                    
                    if (valueArgument == null) throw new RuntimeException("valueArgument is null");
                    
                    commands.add(
                            new CommandAPICommand(fieldName)
                                    .withPermission("noxesiumutils.command." + fieldName)
                                    .withArguments(
                                            new EntitySelectorArgument.ManyPlayers("players"),
                                            valueArgument
                                    )
                                    .executes((sender, args) -> {
                                        var players = (Collection<Player>) args.get("players");
                                        var value = args.get("value");
                                        value = processor.process(value);
                                        updateServerRule(sender, players, index, value);
                                    })
                    );

                    commands.add(
                            new CommandAPICommand(fieldName)
                                    .withPermission("noxesiumutils.command." + fieldName)
                                    .withArguments(
                                            new EntitySelectorArgument.ManyPlayers("players"),
                                            new LiteralArgument("reset")
                                    )
                                    .executes((sender, args) -> {
                                        var players = (Collection<Player>) args.get("players");
                                        resetServerRule(sender, players, index);
                                    })
                    );
                }
            } catch (Exception e) {
                getLogger().warning("Couldn't read field data for '" + fieldName + "' while registering commands: " + e.getMessage());
            }
        }
        
        return commands;
    }

    private List<CommandAPICommand> generateEntityRulesSubcommands() {
        List<CommandAPICommand> commands = new LinkedList<>();
        // TODO: Unfinished section
        return commands;
    }

    private static void updateServerRule(@Nullable CommandSender sender, Collection<Player> players, Integer index, Object value) {
        if (players == null) return;
        AtomicInteger updates = new AtomicInteger();
        players.forEach(player -> {
            if (noxesiumUtils.getManager().setServerRule(player, index, value))
                updates.getAndIncrement();
        });

        if (sender != null)
            sender.sendMessage(Component.text(updates.get() + " player(s) affected.", NamedTextColor.GREEN));
    }

    private static void resetServerRule(@Nullable CommandSender sender, Collection<Player> players, Integer index) {
        if (players == null) return;
        AtomicInteger updates = new AtomicInteger();
        players.forEach(player -> {
            if (noxesiumUtils.getManager().resetServerRule(player, index))
                updates.getAndIncrement();
        });

        if (sender != null)
            sender.sendMessage(Component.text(updates.get() + " player(s) affected.", NamedTextColor.GREEN));
    }
    
    @FunctionalInterface
    interface ProcessObjectFunction {
        Object process(Object input);
    }
}
