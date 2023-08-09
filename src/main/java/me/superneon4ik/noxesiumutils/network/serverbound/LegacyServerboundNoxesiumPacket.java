package me.superneon4ik.noxesiumutils.network.serverbound;

import me.superneon4ik.noxesiumutils.modules.FriendlyByteBuf;
import org.bukkit.entity.Player;

public abstract class LegacyServerboundNoxesiumPacket {
    public final String channel;

    public LegacyServerboundNoxesiumPacket(String channel) {
        this.channel = channel;
    }

    public abstract void receive(Player player, FriendlyByteBuf buffer);
}
