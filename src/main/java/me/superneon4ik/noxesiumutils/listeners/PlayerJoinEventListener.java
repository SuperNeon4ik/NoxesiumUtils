package me.superneon4ik.noxesiumutils.listeners;

import me.superneon4ik.noxesiumutils.NoxesiumUtils;
import me.superneon4ik.noxesiumutils.OldNoxesiumUtilsImpl;
import me.superneon4ik.noxesiumutils.enums.VersionStatus;
import me.superneon4ik.noxesiumutils.events.NoxesiumPlayerReadyEvent;
import me.superneon4ik.noxesiumutils.modules.ModrinthUpdateChecker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinEventListener implements Listener {
    private final ModrinthUpdateChecker modrinthUpdateChecker;
    
    public PlayerJoinEventListener(ModrinthUpdateChecker modrinthUpdateChecker) {
        this.modrinthUpdateChecker = modrinthUpdateChecker;
    }
    
    @EventHandler
    public void on(PlayerJoinEvent event) {
        if (event.getPlayer().isOp()) {
            if (modrinthUpdateChecker.getLatestStatus() == VersionStatus.OUTDATED) {
                event.getPlayer().sendMessage(modrinthUpdateChecker.generateVersionMessage());
            }
        }
    }
}
