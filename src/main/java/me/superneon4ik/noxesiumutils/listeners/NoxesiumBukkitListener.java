package me.superneon4ik.noxesiumutils.listeners;

import me.superneon4ik.noxesiumutils.NoxesiumUtils;
import me.superneon4ik.noxesiumutils.enums.VersionStatus;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class NoxesiumBukkitListener implements Listener {
    @EventHandler
    public void on(PlayerJoinEvent event) {
        if (event.getPlayer().isOp()) {
            if (NoxesiumUtils.getUpdateChecker().getLatestStatus() == VersionStatus.OUTDATED) {
                NoxesiumUtils.getUpdateChecker().sendVersionMessage(event.getPlayer());
            }
        }
    }
}
