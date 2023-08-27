package me.superneon4ik.noxesiumutils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.noxcrew.noxesium.api.protocol.NoxesiumFeature;
import com.noxcrew.noxesium.api.protocol.rule.ServerRuleIndices;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import dev.jorel.commandapi.annotations.Subcommand;
import dev.jorel.commandapi.annotations.arguments.ABooleanArgument;
import dev.jorel.commandapi.annotations.arguments.AEntitySelectorArgument;
import dev.jorel.commandapi.annotations.arguments.AIntegerArgument;
import me.superneon4ik.noxesiumutils.network.clientbound.ClientboundChangeServerRulesPacket;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Command("noxesiumutils")
@Permission("noxesiumutils.commands")
public class NoxesiumUtilsCommand {
    @Default
    public static void def(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "For help refer to " + ChatColor.YELLOW + "https://github.com/SuperNeon4ik/NoxesiumUtils#readme");
        sender.sendMessage(ChatColor.DARK_GRAY + "Checking for updates...");
        var future = NoxesiumUtils.getUpdateChecker().checkForUpdates();
        future.thenAccept(versionStatus -> {
            NoxesiumUtils.getUpdateChecker().sendVersionMessage(sender, versionStatus);
        });
    }

    @Subcommand("check")
    public static void check(CommandSender sender, @AEntitySelectorArgument.OnePlayer Player player) {
        Integer protocolVersion = NoxesiumUtils.getManager().getProtocolVersion(player);
        if (protocolVersion == null) {
            sender.sendMessage(ChatColor.RED + player.getName() + " doesn't have Noxesium installed.");
            return;
        }

        sender.sendMessage(ChatColor.GREEN + player.getName() + " has Noxesium installed. " + ChatColor.YELLOW + "(Protocol Version: " + protocolVersion + ")");
    }

    @Subcommand("check")
    public static void check(CommandSender sender, @AEntitySelectorArgument.ManyPlayers Collection<Player> players) {
        LinkedList<String> strings = new LinkedList<>();
        for (Player player : players) {
            Integer protocolVersion = NoxesiumUtils.getManager().getProtocolVersion(player);
            if (protocolVersion == null) {
                strings.add(ChatColor.RED + player.getName());
                continue;
            }

            strings.add(ChatColor.GREEN + player.getName() + ChatColor.YELLOW + " (%s)".formatted(protocolVersion));
        }
        sender.sendMessage(String.join(ChatColor.GRAY + ", ", strings));
    }

    @Subcommand("disableAutoSpinAttack")
    public static void disableAutoSpinAttack(CommandSender sender,
                                             @AEntitySelectorArgument.ManyPlayers Collection<Player> players,
                                             @ABooleanArgument boolean value) {
        AtomicInteger updates = new AtomicInteger();
        players.stream().filter(x -> NoxesiumUtils.getManager().isUsingNoxesium(x, NoxesiumFeature.ANY)).forEach(player -> {
            var rule = NoxesiumUtils.getManager().<Boolean>getServerRule(player, ServerRuleIndices.DISABLE_SPIN_ATTACK_COLLISIONS);
            if (rule == null) return;
            rule.setValue(value);
            if (new ClientboundChangeServerRulesPacket(List.of(rule)).send(player)) {
                updates.getAndIncrement();
            }
        });
        sender.sendMessage(ChatColor.GREEN + String.valueOf(updates.get()) + " player(s) affected.");
    }

    @Subcommand("heldItemNameOffset")
    public static void heldItemNameOffset(CommandSender sender,
                                          @AEntitySelectorArgument.ManyPlayers Collection<Player> players,
                                          @AIntegerArgument int value) {
        AtomicInteger updates = new AtomicInteger();
        players.stream().filter(x -> NoxesiumUtils.getManager().isUsingNoxesium(x, NoxesiumFeature.PLAYER_HEADS)).forEach(player -> {
            var rule = NoxesiumUtils.getManager().<Integer>getServerRule(player, ServerRuleIndices.HELD_ITEM_NAME_OFFSET);
            if (rule == null) return;
            rule.setValue(value);
            if (new ClientboundChangeServerRulesPacket(List.of(rule)).send(player)) {
                updates.getAndIncrement();
            }
        });
        sender.sendMessage(ChatColor.GREEN + String.valueOf(updates.get()) + " player(s) affected.");
    }

    @Subcommand("cameraLocked")
    public static void cameraLocked(CommandSender sender,
                                    @AEntitySelectorArgument.ManyPlayers Collection<Player> players,
                                    @ABooleanArgument boolean value) {
        AtomicInteger updates = new AtomicInteger();
        players.stream().filter(x -> NoxesiumUtils.getManager().isUsingNoxesium(x, NoxesiumFeature.PLAYER_HEADS)).forEach(player -> {
            var rule = NoxesiumUtils.getManager().<Boolean>getServerRule(player, ServerRuleIndices.CAMERA_LOCKED);
            if (rule == null) return;
            rule.setValue(value);
            if (new ClientboundChangeServerRulesPacket(List.of(rule)).send(player)) {
                updates.getAndIncrement();
            }
        });
        sender.sendMessage(ChatColor.GREEN + String.valueOf(updates.get()) + " player(s) affected.");
    }

    @Subcommand("enableCustomMusic")
    public static void enableCustomMusic(CommandSender sender,
                                    @AEntitySelectorArgument.ManyPlayers Collection<Player> players,
                                    @ABooleanArgument boolean value) {
        AtomicInteger updates = new AtomicInteger();
        players.stream().filter(x -> NoxesiumUtils.getManager().isUsingNoxesium(x, NoxesiumFeature.MUSIC_SERVER_RULE)).forEach(player -> {
            var rule = NoxesiumUtils.getManager().<Boolean>getServerRule(player, ServerRuleIndices.ENABLE_CUSTOM_MUSIC);
            if (rule == null) return;
            rule.setValue(value);
            if (new ClientboundChangeServerRulesPacket(List.of(rule)).send(player)) {
                updates.getAndIncrement();
            }
        });
        sender.sendMessage(ChatColor.GREEN + String.valueOf(updates.get()) + " player(s) affected.");
    }

    @Subcommand("clientSettings")
    public static void clientSettings(CommandSender sender,
                                      @AEntitySelectorArgument.OnePlayer Player player) {
        var clientSettings = NoxesiumUtils.getManager().getClientSettings(player);
        if (clientSettings == null) {
            sender.sendMessage(Component.text(String.format("%s doesn't have Noxesium installed or didn't yet provide their settings.",
                    player.getName()), NamedTextColor.RED));
            return;
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(clientSettings);
        sender.sendMessage(json);
    }
}
