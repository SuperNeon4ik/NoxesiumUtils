package me.superneon4ik.noxesiumutils;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandAPIConfig;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import me.superneon4ik.noxesiumutils.listeners.NoxesiumMessageListener;
import me.superneon4ik.noxesiumutils.modules.FriendlyByteBuf;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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
                .withSubcommand(
                        new CommandAPICommand("broadcast")
                                .withSubcommands(
                                        new CommandAPICommand("disableAutoSpinAttack")
                                                .withArguments(new BooleanArgument("value"))
                                                .executes((executor, args) -> {
                                                    int amount = 0;
                                                    for (Player player : Bukkit.getOnlinePlayers()) {
                                                        if (noxesiumPlayers.containsKey(player.getUniqueId())) {
                                                            if (noxesiumPlayers.get(player.getUniqueId()) >= 1) {
                                                                sendDisableAutoSpinAttackRule(player, (boolean) args[0]);
                                                                amount++;
                                                            }
                                                        }
                                                    }
                                                    executor.sendMessage(ChatColor.GREEN + "Send rules to " + amount + " players.");
                                                })
                                )
                )
                .register();
    }

    public void sendDisableAutoSpinAttackRule(@NotNull Player player, boolean value) {
        FriendlyByteBuf byteBuf = new FriendlyByteBuf(Unpooled.buffer());
        byteBuf.writeVarIntArray(new int[] { 0 });
        byteBuf.writeInt(1);
        byteBuf.writeInt(0);
        byteBuf.writeBoolean(value);
//        getLogger().info(toHexadecimal(byteBuf.array()));
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
