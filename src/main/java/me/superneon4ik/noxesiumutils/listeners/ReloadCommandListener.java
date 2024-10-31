package me.superneon4ik.noxesiumutils.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class ReloadCommandListener implements Listener {
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!event.getPlayer().isOp()) return;

        if (event.getMessage().equalsIgnoreCase("/reload confirm")) {
            // Send a warning
            event.getPlayer().sendRichMessage(
                    "<yellow><bold>WARNING:</bold> NoxesiumUtils doesn't support reloads, " +
                    "and players who are using Noxesium will have to rejoin to register correctly."
            );
        }
    }
}
