package me.superneon4ik.noxesiumutils.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when player becomes ready to start receiving packets.
 * Called before {@link NoxesiumPlayerRegisteredEvent}.
 */
@Getter
public class NoxesiumPlayerReadyEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;

    public NoxesiumPlayerReadyEvent(Player player) {
        this.player = player;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
