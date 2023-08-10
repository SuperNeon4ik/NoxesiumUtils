package me.superneon4ik.noxesiumutils;

import com.noxcrew.noxesium.api.protocol.NoxesiumFeature;
import com.noxcrew.noxesium.api.protocol.rule.ServerRuleIndices;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.ListArgumentBuilder;
import lombok.Getter;
import me.superneon4ik.noxesiumutils.feature.rule.ClientboundServerRule;
import me.superneon4ik.noxesiumutils.listeners.LegacyNoxesiumMessageListener;
import me.superneon4ik.noxesiumutils.listeners.NoxesiumBukkitListener;
import me.superneon4ik.noxesiumutils.listeners.NoxesiumMessageListener;
import me.superneon4ik.noxesiumutils.modules.ModrinthUpdateChecker;
import me.superneon4ik.noxesiumutils.network.clientbound.ClientboundChangeServerRulesPacket;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class NoxesiumUtils extends JavaPlugin {
    public static final int SERVER_PROTOCOL_VERSION = 3;

    // legacy
    public static final String NOXESIUM_LEGACY_CLIENT_INFORMATION_CHANNEL = "noxesium:client_information";
    public static final String NOXESIUM_LEGACY_CLIENT_SETTINGS_CHANNEL = "noxesium:client_settings";
    public static final String NOXESIUM_LEGACY_SERVER_RULE_CHANNEL = "noxesium:server_rules";

    // v1
    public static final String NOXESIUM_V1_CLIENT_INFORMATION_CHANNEL = "noxesium-v1:client_info";
    public static final String NOXESIUM_V1_CLIENT_SETTINGS_CHANNEL = "noxesium-v1:client_settings";
    public static final String NOXESIUM_V1_SERVER_INFORMATION_CHANNEL = "noxesium-v1:server_info";
    public static final String NOXESIUM_V1_CHANGE_SERVER_RULES_CHANNEL = "noxesium-v1:change_server_rules";
    public static final String NOXESIUM_V1_RESET_SERVER_RULES_CHANNEL = "noxesium-v1:reset_server_rules";
    public static final String NOXESIUM_V1_RESET_CHANNEL = "noxesium-v1:reset";

    @Getter private static NoxesiumUtils plugin;
    @Getter private static final ModrinthUpdateChecker updateChecker = new ModrinthUpdateChecker("noxesiumutils");
    @Getter private static final NoxesiumManager manager = new NoxesiumManager();

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        registerCommands();

        // Register outgoing plugin messaging channels
        getServer().getMessenger().registerOutgoingPluginChannel(this, NOXESIUM_LEGACY_SERVER_RULE_CHANNEL);
        getServer().getMessenger().registerOutgoingPluginChannel(this, NOXESIUM_V1_CHANGE_SERVER_RULES_CHANNEL);
        getServer().getMessenger().registerOutgoingPluginChannel(this, NOXESIUM_V1_RESET_SERVER_RULES_CHANNEL);
        getServer().getMessenger().registerOutgoingPluginChannel(this, NOXESIUM_V1_SERVER_INFORMATION_CHANNEL);
        getServer().getMessenger().registerOutgoingPluginChannel(this, NOXESIUM_V1_RESET_CHANNEL);

        // Register incoming plugin messaging channels
        getServer().getMessenger().registerIncomingPluginChannel(this, NOXESIUM_LEGACY_CLIENT_INFORMATION_CHANNEL, new LegacyNoxesiumMessageListener());
        getServer().getMessenger().registerIncomingPluginChannel(this, NOXESIUM_LEGACY_CLIENT_SETTINGS_CHANNEL, new LegacyNoxesiumMessageListener());
        getServer().getMessenger().registerIncomingPluginChannel(this, NOXESIUM_V1_CLIENT_INFORMATION_CHANNEL, new NoxesiumMessageListener());
        getServer().getMessenger().registerIncomingPluginChannel(this, NOXESIUM_V1_CLIENT_SETTINGS_CHANNEL, new NoxesiumMessageListener());

        // Register Bukkit listener
        getServer().getPluginManager().registerEvents(new NoxesiumBukkitListener(), this);

        // Check for updates
        if (getConfig().getBoolean("checkForUpdates")) updateChecker.beginChecking(5 * 60 * 20);
    }

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this));
    }

    @SuppressWarnings({"unsafe", "unchecked"})
    private void registerCommands() {
        CommandAPI.registerCommand(NoxesiumUtilsCommand.class);
        new CommandAPICommand("noxesiumutils")
                .withPermission("noxesiumutils.commands")
                .withSubcommands(
                        new CommandAPICommand("globalCanPlaceOn")
                                .withArguments(new EntitySelectorArgument.ManyPlayers("players"), new ListArgumentBuilder<Material>("values")
                                        .withList(List.of(Material.values()))
                                        .withMapper(material -> "minecraft:" + material.name().toLowerCase())
                                        .buildGreedy()
                                )
                                .executes((sender, args) -> {
                                    var players = (Collection<Player>) args.get(0);
                                    var materialValues = (List<Material>) args.get(1);
                                    assert materialValues != null && players != null;
                                    var stringValues = materialValues.stream().map(v -> "minecraft:" + v.name().toLowerCase()).toList();

                                    AtomicInteger updates = new AtomicInteger();
                                    players.stream().filter(x -> NoxesiumUtils.getManager().isUsingNoxesium(x, 2)).forEach(player -> {
                                        var rule = NoxesiumUtils.getManager().<List<String>>getServerRule(player, ServerRuleIndices.GLOBAL_CAN_PLACE_ON);
                                        if (rule == null) return;
                                        rule.setValue(stringValues);
                                        if (new ClientboundChangeServerRulesPacket(List.of(rule)).send(player)) {
                                            updates.getAndIncrement();
                                        }
                                    });
                                    sender.sendMessage(ChatColor.GREEN + String.valueOf(updates.get()) + " player(s) affected.");
                                }),
                        new CommandAPICommand("globalCanDestroy")
                                .withArguments(new EntitySelectorArgument.ManyPlayers("players"), new ListArgumentBuilder<Material>("values")
                                        .withList(List.of(Material.values()))
                                        .withMapper(material -> "minecraft:" + material.name().toLowerCase())
                                        .buildGreedy()
                                )
                                .executes((sender, args) -> {
                                    var players = (Collection<Player>) args.get(0);
                                    var materialValues = (List<Material>) args.get(1);
                                    assert materialValues != null && players != null;
                                    var stringValues = materialValues.stream().map(v -> "minecraft:" + v.name().toLowerCase()).toList();

                                    AtomicInteger updates = new AtomicInteger();
                                    players.stream().filter(x -> NoxesiumUtils.getManager().isUsingNoxesium(x, 2)).forEach(player -> {
                                        var rule = NoxesiumUtils.getManager().<List<String>>getServerRule(player, ServerRuleIndices.GLOBAL_CAN_DESTROY);
                                        if (rule == null) return;
                                        rule.setValue(stringValues);
                                        if (new ClientboundChangeServerRulesPacket(List.of(rule)).send(player)) {
                                            updates.getAndIncrement();
                                        }
                                    });
                                    sender.sendMessage(ChatColor.GREEN + String.valueOf(updates.get()) + " player(s) affected.");
                                })
                )
                .register();
    }

    public void sendLoginServerRules(Player player) {
        // Send defaults
        if (NoxesiumUtils.getPlugin().getConfig().getBoolean("sendDefaultsOnJoin", false)) {
            // Send defaults after a little time, so the client actually registers the packet.
            new BukkitRunnable() {
                @Override
                public void run() {
                    // Create builder
                    var protocolVersion = getManager().getProtocolVersion(player);
                    LinkedList<ClientboundServerRule<?>> rules = new LinkedList<>();
                    if (protocolVersion >= NoxesiumFeature.ANY.getMinProtocolVersion()) {
                        // Tridents
                        if (NoxesiumUtils.getPlugin().getConfig().contains("defaults.disableAutoSpinAttack")) {
                            var value = NoxesiumUtils.getPlugin().getConfig().getBoolean("defaults.disableAutoSpinAttack", false);
                            var rule = getManager().getServerRule(player, ServerRuleIndices.DISABLE_SPIN_ATTACK_COLLISIONS);
                            if (rule != null) {
                                rule.setValue(value);
                                rules.add(rule);
                            }
                        }
                        // Global Can Place On
                        if (NoxesiumUtils.getPlugin().getConfig().contains("defaults.globalCanPlaceOn")) {
                            var blocks = NoxesiumUtils.getPlugin().getConfig().getStringList("defaults.globalCanPlaceOn");
                            var rule = getManager().getServerRule(player, ServerRuleIndices.GLOBAL_CAN_PLACE_ON);
                            if (rule != null) {
                                rule.setValue(blocks);
                                rules.add(rule);
                            }
                        }
                        // Global Can Destroy
                        if (NoxesiumUtils.getPlugin().getConfig().contains("defaults.globalCanDestroy")) {
                            var blocks = NoxesiumUtils.getPlugin().getConfig().getStringList("defaults.globalCanDestroy");
                            var rule = getManager().getServerRule(player, ServerRuleIndices.GLOBAL_CAN_DESTROY);
                            if (rule != null) {
                                rule.setValue(blocks);
                                rules.add(rule);
                            }
                        }
                    }
                    if (protocolVersion >= 2) {
                        // Held Item Name Offset
                        if (NoxesiumUtils.getPlugin().getConfig().contains("defaults.heldItemNameOffset")) {
                            var value = NoxesiumUtils.getPlugin().getConfig().getInt("defaults.heldItemNameOffset", 0);
                            var rule = getManager().getServerRule(player, ServerRuleIndices.HELD_ITEM_NAME_OFFSET);
                            if (rule != null) {
                                rule.setValue(value);
                                rules.add(rule);
                            }
                        }
                        // Camera Locked
                        if (NoxesiumUtils.getPlugin().getConfig().contains("defaults.cameraLocked")) {
                            var value = NoxesiumUtils.getPlugin().getConfig().getBoolean("defaults.cameraLocked", false);
                            var rule = getManager().getServerRule(player, ServerRuleIndices.CAMERA_LOCKED);
                            if (rule != null) {
                                rule.setValue(value);
                                rules.add(rule);
                            }
                        }
                    }
                    // Send packet
                    new ClientboundChangeServerRulesPacket(rules).send(player);
                }
            }.runTaskLater(NoxesiumUtils.getPlugin(), 5);
        }
    }
}
