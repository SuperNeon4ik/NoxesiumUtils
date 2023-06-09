package me.superneon4ik.noxesiumutils.listeners;

import me.superneon4ik.noxesiumutils.NoxesiumUtils;
import me.superneon4ik.noxesiumutils.enums.VersionStatus;
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
            if (NoxesiumUtils.getUpdateChecker().getLatestStatus() == VersionStatus.OUTDATED) {
                NoxesiumUtils.getUpdateChecker().sendVersionMessage(event.getPlayer());
            }
        }
    }
}
