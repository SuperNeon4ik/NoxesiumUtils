package me.superneon4ik.noxesiumutils;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandAPIConfig;
import dev.jorel.commandapi.arguments.*;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import me.superneon4ik.noxesiumutils.listeners.NoxesiumMessageListener;
import me.superneon4ik.noxesiumutils.modules.FriendlyByteBuf;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public final class NoxesiumUtils extends JavaPlugin {
    public static final String NOXESIUM_CLIENT_INFORMATION_CHANNEL = "noxesium:client_information";
    public static final String NOXESIUM_CLIENT_SETTINGS_CHANNEL = "noxesium:client_settings";
    public static final String NOXESIUM_SERVER_RULE_CHANNEL = "noxesium:server_rules";

    @Getter private static NoxesiumUtils plugin;
    @Getter private static Map<UUID, Integer> noxesiumPlayers = new Hashtable<>();

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        registerCommands();
        getServer().getMessenger().registerOutgoingPluginChannel(this, NOXESIUM_SERVER_RULE_CHANNEL);
        getServer().getMessenger().registerIncomingPluginChannel(this, NOXESIUM_CLIENT_INFORMATION_CHANNEL, new NoxesiumMessageListener());
    }

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIConfig());
    }

    private void registerCommands() {
        new CommandAPICommand("noxesiumutils")
                .withPermission("noxesiumutils.commands")
                .withSubcommands(
                    new CommandAPICommand("disableAutoSpinAttack")
                            .withArguments(new EntitySelectorArgument.ManyPlayers("players"), new BooleanArgument("value"))
                            .executes((executor, args) -> {
                                int amount = forNoxesiumPlayers((Collection<Player>) args[0], 1, player -> {
                                    sendBoolean(player, 0, (boolean) args[1]);
                                });
                                executor.sendMessage(ChatColor.GREEN + "Send rules to " + amount + " players.");
                            }),
                    new CommandAPICommand("cameraLocked")
                            .withArguments(new EntitySelectorArgument.ManyPlayers("players"), new BooleanArgument("value"))
                            .executes((executor, args) -> {
                                int amount = forNoxesiumPlayers((Collection<Player>) args[0], 2, player -> {
                                    sendBoolean(player, 4, (boolean) args[1]);
                                });
                                executor.sendMessage(ChatColor.GREEN + "Send rules to " + amount + " players.");
                            }),
                    new CommandAPICommand("heldItemNameOffset")
                            .withArguments(new EntitySelectorArgument.ManyPlayers("players"), new IntegerArgument("value"))
                            .executes((executor, args) -> {
                                int amount = forNoxesiumPlayers((Collection<Player>) args[0], 2, player -> {
                                    sendInteger(player, 3, (int) args[1]);
                                });
                                executor.sendMessage(ChatColor.GREEN + "Send rules to " + amount + " players.");
                            }),
                    new CommandAPICommand("globalCanDestroy")
                            .withArguments(new EntitySelectorArgument.ManyPlayers("players"), new ListArgumentBuilder<Material>("values")
                                    .withList(List.of(Material.values()))
                                    .withMapper(material -> "minecraft:" + material.name().toLowerCase())
                                    .buildGreedy()
                            )
                            .executes((executor, args) -> {
                                int amount = forNoxesiumPlayers((Collection<Player>) args[0], 1, player -> {
                                    sendStrings(player, 2, ((String) args[1]).split(" "));
                                });
                                executor.sendMessage(ChatColor.GREEN + "Send rules to " + amount + " players.");
                            }),
                    new CommandAPICommand("globalCanPlaceOn")
                            .withArguments(new EntitySelectorArgument.ManyPlayers("players"), new ListArgumentBuilder<Material>("values")
                                    .withList(List.of(Material.values()))
                                    .withMapper(material -> "minecraft:" + material.name().toLowerCase())
                                    .buildGreedy()
                            )
                            .executes((executor, args) -> {
                                int amount = forNoxesiumPlayers((Collection<Player>) args[0], 1, player -> {
                                    sendStrings(player, 1, ((String) args[1]).split(" "));
                                });
                                executor.sendMessage(ChatColor.GREEN + "Send rules to " + amount + " players.");
                            })
                )
                .register();
    }

    public int forNoxesiumPlayers(Collection<Player> players, int minProtocol, Consumer<Player> playerConsumer) {
        int amount = 0;
        for (Player player : players) {
            if (noxesiumPlayers.containsKey(player.getUniqueId())) {
                if (noxesiumPlayers.get(player.getUniqueId()) >= minProtocol) {
                    playerConsumer.accept(player);
                    amount++;
                }
            }
        }
        return amount;
    }

    public void sendBoolean(@NotNull Player player, int index, boolean value) {
        FriendlyByteBuf byteBuf = new FriendlyByteBuf(Unpooled.buffer());
        byteBuf.writeVarIntArray(new int[] { index });
        byteBuf.writeInt(1);
        byteBuf.writeInt(index);
        byteBuf.writeBoolean(value);
        player.sendPluginMessage(this, NOXESIUM_SERVER_RULE_CHANNEL, byteBuf.array());
    }

    public void sendInteger(@NotNull Player player, int index, int value) {
        FriendlyByteBuf byteBuf = new FriendlyByteBuf(Unpooled.buffer());
        byteBuf.writeVarIntArray(new int[] { index });
        byteBuf.writeInt(1);
        byteBuf.writeInt(index);
        byteBuf.writeVarInt(value);
        player.sendPluginMessage(this, NOXESIUM_SERVER_RULE_CHANNEL, byteBuf.array());
    }

    public void sendStrings(@NotNull Player player, int index, String... strings) {
        FriendlyByteBuf byteBuf = new FriendlyByteBuf(Unpooled.buffer());
        byteBuf.writeVarIntArray(new int[] { index });
        byteBuf.writeInt(1);
        byteBuf.writeInt(index);
        byteBuf.writeVarInt(strings.length);
        for (String string : strings) {
            byteBuf.writeUtf(string);
        }
        player.sendPluginMessage(this, NOXESIUM_SERVER_RULE_CHANNEL, byteBuf.array());
    }

    public static String toHexadecimal(byte[] digest){
        StringBuilder hash = new StringBuilder();
        for(byte aux : digest) {
            int b = aux & 0xff;
            if (Integer.toHexString(b).length() == 1) hash.append("0");
            hash.append(Integer.toHexString(b));
        }
        return hash.toString();
    }
}
