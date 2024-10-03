package me.superneon4ik.noxesiumutils.events;

import com.noxcrew.noxesium.paper.api.network.serverbound.ServerboundQibTriggeredPacket;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class NoxesiumQibTriggeredEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final String behavior;
    private final ServerboundQibTriggeredPacket.Type qibType;
    private final int entityId;

    public NoxesiumQibTriggeredEvent(Player player, String behavior, ServerboundQibTriggeredPacket.Type qibType, int entityId) {
        this.player = player;
        this.behavior = behavior;
        this.qibType = qibType;
        this.entityId = entityId;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
