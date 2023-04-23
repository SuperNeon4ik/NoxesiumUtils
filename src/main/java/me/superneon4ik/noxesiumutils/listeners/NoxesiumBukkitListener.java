package me.superneon4ik.noxesiumutils.listeners;

import me.superneon4ik.noxesiumutils.NoxesiumUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class NoxesiumBukkitListener implements Listener {
    @EventHandler
    public void on(PlayerQuitEvent event) {
        // Remove offline player from the cached values
        NoxesiumUtils.getNoxesiumPlayers().remove(event.getPlayer().getUniqueId());
        NoxesiumUtils.getNoxesiumClientSettings().remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void on(PlayerJoinEvent event) {
        if (event.getPlayer().isOp()) {
            // Check for updates
            new BukkitRunnable() {
                @Override
                public void run() {
                    NoxesiumUtils.getUpdateChecker().checkVersion(event.getPlayer());
                }
            }.runTaskAsynchronously(NoxesiumUtils.getPlugin());
        }
    }
}
