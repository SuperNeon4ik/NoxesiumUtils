package me.superneon4ik.noxesiumutils.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class NoxesiumPlayerJoinEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Getter private final Player player;
    @Getter private final int protocolVersion;

    public NoxesiumPlayerJoinEvent(Player player, int protocolVersion) {
        this.player = player;
        this.protocolVersion = protocolVersion;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
