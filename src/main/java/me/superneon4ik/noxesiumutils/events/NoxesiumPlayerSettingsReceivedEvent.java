package me.superneon4ik.noxesiumutils.events;

import com.noxcrew.noxesium.api.protocol.ClientSettings;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called on the join sequence, when the client
 * sends its settings.
 */
@Getter
public class NoxesiumPlayerSettingsReceivedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final ClientSettings clientSettings;

    public NoxesiumPlayerSettingsReceivedEvent(Player player, ClientSettings clientSettings) {
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
