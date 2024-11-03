package me.superneon4ik.noxesiumutils.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called on the join sequence, when the client
 * sends its Noxesium version.
 */
@Getter
public class NoxesiumPlayerVersionReceivedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final String version;
    private final int protocolVersion;

    public NoxesiumPlayerVersionReceivedEvent(Player player, String version, int protocolVersion) {
        this.player = player;
        this.version = version;
        this.protocolVersion = protocolVersion;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
