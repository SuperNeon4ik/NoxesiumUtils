package me.superneon4ik.noxesiumutils.listeners;

import me.superneon4ik.noxesiumutils.NoxesiumUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class NoxesiumBukkitListener implements Listener {
    @EventHandler
    public void on(PlayerQuitEvent event) {
        NoxesiumUtils.getNoxesiumPlayers().remove(event.getPlayer().getUniqueId());
    }
}
