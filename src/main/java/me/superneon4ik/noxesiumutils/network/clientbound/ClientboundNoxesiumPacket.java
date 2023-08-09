package me.superneon4ik.noxesiumutils.network.clientbound;

import io.netty.buffer.Unpooled;
import me.superneon4ik.noxesiumutils.NoxesiumUtils;
import me.superneon4ik.noxesiumutils.modules.FriendlyByteBuf;
import me.superneon4ik.noxesiumutils.network.NoxesiumPacket;
import org.bukkit.entity.Player;

public abstract class ClientboundNoxesiumPacket extends NoxesiumPacket {
    public ClientboundNoxesiumPacket(String channel, int version) {
        super(channel, version);
    }

    public abstract void serialize(FriendlyByteBuf buffer);
    public abstract void legacySerialize(FriendlyByteBuf buffer);

    public void send(Player player) {
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeVarInt(version);
        serialize(buffer);
        player.sendPluginMessage(NoxesiumUtils.getPlugin(), channel, buffer.array());
    }
}
