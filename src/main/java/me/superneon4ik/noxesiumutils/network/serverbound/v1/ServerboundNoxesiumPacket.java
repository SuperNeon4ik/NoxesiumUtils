package me.superneon4ik.noxesiumutils.network.serverbound.v1;

import me.superneon4ik.noxesiumutils.modules.FriendlyByteBuf;
import me.superneon4ik.noxesiumutils.network.NoxesiumPacket;
import org.bukkit.entity.Player;

public abstract class ServerboundNoxesiumPacket extends NoxesiumPacket {
    public ServerboundNoxesiumPacket(String channel, int version) {
        super(channel, version);
    }

    public abstract void receive(Player player, int version, FriendlyByteBuf buffer);
}
