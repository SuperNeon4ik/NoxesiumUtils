package me.superneon4ik.noxesiumutils;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandAPIConfig;
import dev.jorel.commandapi.arguments.*;
import lombok.Getter;
import me.superneon4ik.noxesiumutils.listeners.NoxesiumBukkitListener;
import me.superneon4ik.noxesiumutils.listeners.NoxesiumMessageListener;
import me.superneon4ik.noxesiumutils.modules.NoxesiumServerRuleBuilder;
import me.superneon4ik.noxesiumutils.modules.UpdateChecker;
import me.superneon4ik.noxesiumutils.objects.PlayerClientSettings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;

public final class NoxesiumUtils extends JavaPlugin {
    public static final String NOXESIUM_LEGACY_CLIENT_INFORMATION_CHANNEL = "noxesium:client_information";
    public static final String NOXESIUM_LEGACY_CLIENT_SETTINGS_CHANNEL = "noxesium:client_settings";
    public static final String NOXESIUM_LEGACY_SERVER_RULE_CHANNEL = "noxesium:server_rules";
    public static final String NOXESIUM_V1_CLIENT_INFORMATION_CHANNEL = "noxesium-v1:client_information";
    public static final String NOXESIUM_V1_CLIENT_SETTINGS_CHANNEL = "noxesium-v1:client_settings";
    public static final String NOXESIUM_V1_SERVER_RULE_CHANNEL = "noxesium-v1:server_rules";
    public static final String NOXESIUM_V1_RESET_CHANNEL = "noxesium-v1:reset";

    @Getter private static NoxesiumUtils plugin;
    @Getter private static final Map<UUID, Integer> noxesiumPlayers = new Hashtable<>();
    @Getter private static final Map<UUID, PlayerClientSettings> noxesiumClientSettings = new Hashtable<>();
    @Getter private static final UpdateChecker updateChecker = new UpdateChecker("SuperNeon4ik", "NoxesiumUtils");

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        registerCommands();

        // Register outgoing plugin messaging channels
        getServer().getMessenger().registerOutgoingPluginChannel(this, NOXESIUM_LEGACY_SERVER_RULE_CHANNEL);
        getServer().getMessenger().registerOutgoingPluginChannel(this, NOXESIUM_V1_SERVER_RULE_CHANNEL);
        getServer().getMessenger().registerOutgoingPluginChannel(this, NOXESIUM_V1_RESET_CHANNEL);

        // Register incoming plugin messaging channels
        getServer().getMessenger().registerIncomingPluginChannel(this, NOXESIUM_LEGACY_CLIENT_INFORMATION_CHANNEL, new NoxesiumMessageListener());
        getServer().getMessenger().registerIncomingPluginChannel(this, NOXESIUM_LEGACY_CLIENT_SETTINGS_CHANNEL, new NoxesiumMessageListener());
        getServer().getMessenger().registerIncomingPluginChannel(this, NOXESIUM_V1_CLIENT_INFORMATION_CHANNEL, new NoxesiumMessageListener());
        getServer().getMessenger().registerIncomingPluginChannel(this, NOXESIUM_V1_CLIENT_SETTINGS_CHANNEL, new NoxesiumMessageListener());

        // Register Bukkit listener
        getServer().getPluginManager().registerEvents(new NoxesiumBukkitListener(), this);

        // Check for updates
        new BukkitRunnable() {
            @Override
            public void run() {
                updateChecker.sendVersionInfoMessage(getServer().getConsoleSender());
            }
        }.runTaskLaterAsynchronously(this, 1);
    }

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this));
    }

    @SuppressWarnings({"unsafe", "unchecked"})
    private void registerCommands() {
        new CommandAPICommand("noxesiumutils")
                .withPermission("noxesiumutils.commands")
                .withSubcommands(
                        new CommandAPICommand("disableAutoSpinAttack")
                                .withArguments(new EntitySelectorArgument.ManyPlayers("players"), new BooleanArgument("value"))
                                .executes((executor, args) -> {
                                    var players = (Collection<Player>) args.get(0);
                                    var value = (boolean) args.get(1);
                                    int amount = forNoxesiumPlayers(players, 1, (player, pv) -> {
                                        sendServerRulesPacket(player, new NoxesiumServerRuleBuilder(pv).add(0, value).build());
                                    });
                                    executor.sendMessage(ChatColor.GREEN + String.valueOf(amount) + " player(s) affected.");
                                }),
                        new CommandAPICommand("globalCanPlaceOn")
                                .withArguments(new EntitySelectorArgument.ManyPlayers("players"), new ListArgumentBuilder<Material>("values")
                                        .withList(List.of(Material.values()))
                                        .withMapper(material -> "minecraft:" + material.name().toLowerCase())
                                        .buildGreedy()
                                )
                                .executes((executor, args) -> {
                                    var players = (Collection<Player>) args.get(0);
                                    var materialValues = (List<Material>) args.get(1);
                                    var stringValues = materialValues.stream().map(v -> "minecraft:" + v.name().toLowerCase()).toList();
                                    int amount = forNoxesiumPlayers(players, 1, (player, pv) -> {
                                        sendServerRulesPacket(player, new NoxesiumServerRuleBuilder(pv).add(1, stringValues).build());
                                    });
                                    executor.sendMessage(ChatColor.GREEN + String.valueOf(amount) + " player(s) affected.");
                                }),
                        new CommandAPICommand("globalCanDestroy")
                                .withArguments(new EntitySelectorArgument.ManyPlayers("players"), new ListArgumentBuilder<Material>("values")
                                        .withList(List.of(Material.values()))
                                        .withMapper(material -> "minecraft:" + material.name().toLowerCase())
                                        .buildGreedy()
                                )
                                .executes((executor, args) -> {
                                    var players = (Collection<Player>) args.get(0);
                                    var materialValues = (List<Material>) args.get(1);
                                    var stringValues = materialValues.stream().map(v -> "minecraft:" + v.name().toLowerCase()).toList();
                                    int amount = forNoxesiumPlayers(players, 1, (player, pv) -> {
                                        sendServerRulesPacket(player, new NoxesiumServerRuleBuilder(pv).add(2, stringValues).build());
                                    });
                                    executor.sendMessage(ChatColor.GREEN + String.valueOf(amount) + " player(s) affected.");
                                }),
                        new CommandAPICommand("heldItemNameOffset")
                                .withArguments(new EntitySelectorArgument.ManyPlayers("players"), new IntegerArgument("value"))
                                .executes((executor, args) -> {
                                    var players = (Collection<Player>) args.get(0);
                                    int value = (int) args.get(1);
                                    int amount = forNoxesiumPlayers(players, 2, (player, pv) -> {
                                        sendServerRulesPacket(player, new NoxesiumServerRuleBuilder(pv).add(3, value).build());
                                    });
                                    executor.sendMessage(ChatColor.GREEN + String.valueOf(amount) + " player(s) affected.");
                                }),
                        new CommandAPICommand("cameraLocked")
                                .withArguments(new EntitySelectorArgument.ManyPlayers("players"), new BooleanArgument("value"))
                                .executes((executor, args) -> {
                                    var players = (Collection<Player>) args.get(0);
                                    var value = (boolean) args.get(1);
                                    int amount = forNoxesiumPlayers(players, 2, (player, pv) -> {
                                        sendServerRulesPacket(player, new NoxesiumServerRuleBuilder(pv).add(4, value).build());
                                    });
                                    executor.sendMessage(ChatColor.GREEN + String.valueOf(amount) + " player(s) affected.");
                                }),
                        new CommandAPICommand("reset")
                                .withArguments(new EntitySelectorArgument.ManyPlayers("players"), new MultiLiteralArgument("all", "cachedPlayerSkulls"))
                                .executes((executor, args) -> {
                                    var players = (Collection<Player>) args.get(0);
                                    var command = (String) args.get(1);

                                    byte bitmask;
                                    if (command.equals("all")) bitmask = 0x01;
                                    else if (command.equals("cachedPlayerSkulls")) bitmask = 0x02;
                                    else bitmask = 0x00;

                                    int amount = forNoxesiumPlayers(players, 3, (player, pv) -> {
                                        player.sendPluginMessage(this, NOXESIUM_V1_RESET_CHANNEL, new byte[] { bitmask });
                                    });
                                    executor.sendMessage(ChatColor.GREEN + String.valueOf(amount) + " player(s) affected.");
                                }),
                        new CommandAPICommand("check")
                                .withArguments(new EntitySelectorArgument.OnePlayer("player"))
                                .executes((executor, args) -> {
                                    Player player = (Player) args.get(0);
                                    if (noxesiumPlayers.containsKey(player.getUniqueId())) {
                                        executor.sendMessage(ChatColor.GREEN + player.getName() + " has Noxesium installed. " + ChatColor.YELLOW + "(Protocol Version: " + noxesiumPlayers.get(player.getUniqueId()) + ")");
                                    }
                                    else {
                                        executor.sendMessage(ChatColor.RED + player.getName() + " doesn't have Noxesium installed.");
                                    }
                                })
                )
                .executes((executor, args) -> {
                    executor.sendMessage(ChatColor.GREEN + "For help refer to " + ChatColor.YELLOW + "https://github.com/SuperNeon4ik/NoxesiumUtils#readme");
                    executor.sendMessage(ChatColor.DARK_GRAY + "Checking for updates...");
                    updateChecker.sendVersionInfoMessage(executor);
                })
                .register();
    }

    /**
     * Execute a Consumer for all Noxesium players online.
     * @param minProtocol Minimum noxesium protocol version.
     * @param playerConsumer Consumer (Player, Protocol Version). Runs for each Noxesium player.
     * @return Number of Noxesium players affected.
     */
    public static int forNoxesiumPlayers(int minProtocol, BiConsumer<Player, Integer> playerConsumer) {
        int amount = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (noxesiumPlayers.containsKey(player.getUniqueId())) {
                Integer protocolVersion = noxesiumPlayers.get(player.getUniqueId());
                if (protocolVersion >= minProtocol) {
                    playerConsumer.accept(player, protocolVersion);
                    amount++;
                }
            }
        }
        return amount;
    }

    /**
     * Execute a Consumer for Noxesium players from the Collection.
     * @param players Collection of players.
     * @param minProtocol Minimum noxesium protocol version.
     * @param playerConsumer Consumer (Player, Protocol Version). Runs for each Noxesium player.
     * @return Number of Noxesium players affected.
     */
    public static int forNoxesiumPlayers(Collection<Player> players, int minProtocol, BiConsumer<Player, Integer> playerConsumer) {
        int amount = 0;
        for (Player player : players) {
            if (noxesiumPlayers.containsKey(player.getUniqueId())) {
                Integer protocolVersion = noxesiumPlayers.get(player.getUniqueId());
                if (protocolVersion >= minProtocol) {
                    playerConsumer.accept(player, protocolVersion);
                    amount++;
                }
            }
        }
        return amount;
    }

    /**
     * Send a server rules packet to a player.
     * @param player Receiver.
     * @param packet Bytes.
     */
    public static void sendServerRulesPacket(@NotNull Player player, byte[] packet) {
        var protocolVersion = getPlayerProtocolVersion(player.getUniqueId());
        if (protocolVersion >= 3) {
            player.sendPluginMessage(getPlugin(), NOXESIUM_V1_SERVER_RULE_CHANNEL, packet);
        }
        else if (protocolVersion >= 1){
            player.sendPluginMessage(getPlugin(), NOXESIUM_LEGACY_SERVER_RULE_CHANNEL, packet);
        }
    }

    /**
     * Returns player's Noxesium protocol version.
     * @param uuid UUID of the player.
     * @return Protocol Version or 0 if not installed.
     */
    public static int getPlayerProtocolVersion(UUID uuid) {
        return noxesiumPlayers.getOrDefault(uuid, 0);
    }

    /**
     * Returns player's client settings.
     * @param uuid UUID of the player.
     * @return Client settings or NULL if not installed.
     */
    public static @Nullable PlayerClientSettings getPlayerClientSettings(UUID uuid) {
         return noxesiumClientSettings.getOrDefault(uuid, null);
    }
}
