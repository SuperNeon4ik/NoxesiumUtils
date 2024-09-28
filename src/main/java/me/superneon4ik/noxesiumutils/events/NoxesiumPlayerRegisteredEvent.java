package me.superneon4ik.noxesiumutils.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class NoxesiumPlayerRegisteredEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;

    public NoxesiumPlayerRegisteredEvent(Player player) {
        this.player = player;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
