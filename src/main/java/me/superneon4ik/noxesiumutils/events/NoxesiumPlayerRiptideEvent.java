package me.superneon4ik.noxesiumutils.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when player riptides. Used for implementing
 * the {@code enableSmootherClientTrident} server rule.
 * <p>
 * NOTE: NoxesiumUtils doesn't yet implement all the needed
 * logic for {@code enableSmootherClientTrident} to work
 * as intended.
 */
@Getter
public class NoxesiumPlayerRiptideEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final int slot;

    public NoxesiumPlayerRiptideEvent(Player player, int slot) {
        this.player = player;
        this.slot = slot;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
