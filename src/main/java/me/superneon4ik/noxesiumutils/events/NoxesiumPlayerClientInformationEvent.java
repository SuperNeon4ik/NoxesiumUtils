package me.superneon4ik.noxesiumutils.events;

import lombok.Getter;
import me.superneon4ik.noxesiumutils.objects.PlayerClientSettings;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class NoxesiumPlayerClientInformationEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Getter private final Player player;
    @Getter private final PlayerClientSettings playerClientSettings;

    public NoxesiumPlayerClientInformationEvent(Player player, PlayerClientSettings playerClientSettings) {
        this.player = player;
        this.playerClientSettings = playerClientSettings;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
