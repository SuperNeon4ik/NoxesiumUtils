package me.superneon4ik.noxesiumutils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import dev.jorel.commandapi.annotations.Subcommand;
import dev.jorel.commandapi.annotations.arguments.AEntitySelectorArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.LinkedList;

@Command("noxesiumutils")
public class NoxesiumUtilsCommand {
    @Default
    @Permission("noxesiumutils.about")
    public static void def(CommandSender sender) {
        var url = Component.text("https://github.com/SuperNeon4ik/NoxesiumUtils#readme", NamedTextColor.YELLOW)
                .clickEvent(ClickEvent.openUrl("https://github.com/SuperNeon4ik/NoxesiumUtils#readme"))
                .hoverEvent(Component.text("Click to open in browser!", NamedTextColor.GREEN, TextDecoration.ITALIC).asHoverEvent());
        
        sender.sendMessage(Component.text("For help refer to ", NamedTextColor.GREEN).append(url));
        sender.sendMessage(Component.text("Checking for updates...", NamedTextColor.DARK_GRAY));
        var future = NoxesiumUtils.getUpdateChecker().checkForUpdates();
        future.thenAccept(versionStatus -> NoxesiumUtils.getUpdateChecker().sendVersionMessage(sender, versionStatus));
    }

    @Subcommand("check")
    @Permission("noxesiumutils.check")
    public static void check(CommandSender sender, @AEntitySelectorArgument.OnePlayer Player player) {
        Integer protocolVersion = NoxesiumUtils.getManager().getProtocolVersion(player);
        if (protocolVersion == null) {
            sender.sendMessage(Component.text(player.getName() + " doesn't have Noxesium installed.", NamedTextColor.RED));
            return;
        }

        sender.sendMessage(Component.text(player.getName() + " has Noxesium installed.", NamedTextColor.GREEN)
                .append(Component.text(" (Protocol Version: " + protocolVersion + ")", NamedTextColor.YELLOW)));
    }

    @Subcommand("check")
    @Permission("noxesiumutils.check")
    public static void check(CommandSender sender, @AEntitySelectorArgument.ManyPlayers Collection<Player> players) {
        LinkedList<Component> strings = new LinkedList<>();
        for (Player player : players) {
            Integer protocolVersion = NoxesiumUtils.getManager().getProtocolVersion(player);
            if (protocolVersion == null) {
                strings.add(Component.text(player.getName(), NamedTextColor.RED));
                continue;
            }

            strings.add(Component.text(player.getName(), NamedTextColor.GREEN)
                    .append(Component.text(" (%s)".formatted(protocolVersion), NamedTextColor.YELLOW)));
        }
        
        sender.sendMessage(Component.join(JoinConfiguration.separator(Component.text(", ", NamedTextColor.DARK_GRAY)), strings));
    }
    
    @Subcommand("clientSettings")
    @Permission("noxesiumutils.check")
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
