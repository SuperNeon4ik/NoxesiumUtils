package me.superneon4ik.noxesiumutils.listeners;

import io.netty.buffer.Unpooled;
import me.superneon4ik.noxesiumutils.NoxesiumUtils;
import me.superneon4ik.noxesiumutils.modules.FriendlyByteBuf;
import me.superneon4ik.noxesiumutils.network.NoxesiumPackets;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

public class NoxesiumMessageListener implements PluginMessageListener {
    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
        NoxesiumUtils.getPlugin().getLogger().info(String.format("%s sent a packet over %s", player.getName(), channel));
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.copiedBuffer(message));
        NoxesiumPackets.handle(player, channel, buffer);
    }
}
