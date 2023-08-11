package me.superneon4ik.noxesiumutils.events;

import com.noxcrew.noxesium.api.protocol.ClientSettings;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class NoxesiumPlayerClientSettingsEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Getter private final Player player;
    @Getter private final ClientSettings clientSettings;

    public NoxesiumPlayerClientSettingsEvent(Player player, ClientSettings clientSettings) {
        this.player = player;
        this.clientSettings = clientSettings;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
