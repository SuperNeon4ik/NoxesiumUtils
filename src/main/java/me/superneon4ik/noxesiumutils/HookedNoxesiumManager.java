package me.superneon4ik.noxesiumutils;

import com.noxcrew.noxesium.api.protocol.ClientSettings;
import com.noxcrew.noxesium.paper.api.NoxesiumManager;
import me.superneon4ik.noxesiumutils.events.NoxesiumPlayerReadyEvent;
import me.superneon4ik.noxesiumutils.events.NoxesiumPlayerRegisteredEvent;
import me.superneon4ik.noxesiumutils.events.NoxesiumPlayerSettingsReceivedEvent;
import me.superneon4ik.noxesiumutils.events.NoxesiumPlayerVersionReceivedEvent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class HookedNoxesiumManager extends NoxesiumManager {
    public HookedNoxesiumManager(@NotNull Plugin plugin, @NotNull Logger logger) {
        super(plugin, logger);
    }

    @Override
    protected void onReady(@NotNull Player player) {
        new NoxesiumPlayerReadyEvent(player).callEvent();
    }

    @Override
    protected void onPlayerRegistered(@NotNull Player player) {
        new NoxesiumPlayerRegisteredEvent(player).callEvent();
    }

    @Override
    protected void onPlayerVersionReceived(@NotNull Player player, @NotNull String version, int protocolVersion) {
        new NoxesiumPlayerVersionReceivedEvent(player, version, protocolVersion).callEvent();
    }

    @Override
    protected void onPlayerSettingsReceived(@NotNull Player player, @NotNull ClientSettings clientSettings) {
        new NoxesiumPlayerSettingsReceivedEvent(player, clientSettings).callEvent();
    }
}
