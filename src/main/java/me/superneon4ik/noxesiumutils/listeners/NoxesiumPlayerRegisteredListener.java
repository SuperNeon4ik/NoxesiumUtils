package me.superneon4ik.noxesiumutils.listeners;

import me.superneon4ik.noxesiumutils.NoxesiumUtils;
import me.superneon4ik.noxesiumutils.events.NoxesiumPlayerRegisteredEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NoxesiumPlayerRegisteredListener implements Listener {
    private final NoxesiumUtils noxesiumUtils;
    
    public NoxesiumPlayerRegisteredListener(NoxesiumUtils noxesiumUtils) {
        this.noxesiumUtils = noxesiumUtils;
    }
    
    @EventHandler
    public void on(NoxesiumPlayerRegisteredEvent event) {
        if (!noxesiumUtils.getConfig().isSendDefaultsOnJoin()) return;
        noxesiumUtils.sendDefaultServerRules(event.getPlayer());
    }
}
