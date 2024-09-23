package me.superneon4ik.noxesiumutils.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class NoxesiumPlayerVersionReceivedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Getter private final Player player;
    @Getter private final String version;
    @Getter private final int protocolVersion;

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
