package me.superneon4ik.noxesiumutils.listeners;

import me.superneon4ik.noxesiumutils.NoxesiumUtils;
import me.superneon4ik.noxesiumutils.OldNoxesiumUtilsImpl;
import me.superneon4ik.noxesiumutils.enums.VersionStatus;
import me.superneon4ik.noxesiumutils.events.NoxesiumPlayerReadyEvent;
import me.superneon4ik.noxesiumutils.modules.ModrinthUpdateChecker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class NoxesiumPlayerReadyEventListener implements Listener {
    private final NoxesiumUtils noxesiumUtils;
    
    public NoxesiumPlayerReadyEventListener(NoxesiumUtils noxesiumUtils) {
        this.noxesiumUtils = noxesiumUtils;
    }
    
    @EventHandler
    public void on(NoxesiumPlayerReadyEvent event) {
        if (!noxesiumUtils.getConfig().isSendDefaultsOnJoin()) return;
        noxesiumUtils.sendDefaultServerRules(event.getPlayer());
    }
}
