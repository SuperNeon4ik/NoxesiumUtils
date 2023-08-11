package me.superneon4ik.noxesiumutils.network.clientbound;

import com.noxcrew.noxesium.api.protocol.NoxesiumFeature;
import io.netty.buffer.Unpooled;
import me.superneon4ik.noxesiumutils.NoxesiumUtils;
import me.superneon4ik.noxesiumutils.modules.FriendlyByteBuf;
import me.superneon4ik.noxesiumutils.network.NoxesiumPacket;
import org.bukkit.entity.Player;

public abstract class ClientboundNoxesiumPacket extends NoxesiumPacket {
    public final String legacyChannel;
    public ClientboundNoxesiumPacket(String channel, String legacyChannel, int version) {
        super(channel, version);
        this.legacyChannel = legacyChannel;
    }

    public abstract void serialize(FriendlyByteBuf buffer);

    public void legacySerialize(FriendlyByteBuf buffer) {
        serialize(buffer);
    }

    public boolean send(Player player) {
        if (NoxesiumUtils.getManager().getProtocolVersion(player) == null) return false;
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        if (NoxesiumUtils.getManager().isUsingNoxesium(player, NoxesiumFeature.API_V1)) {
            buffer.writeVarInt(version);
            serialize(buffer);
            player.sendPluginMessage(NoxesiumUtils.getPlugin(), channel, buffer.array());
            return true;
        }
        else if (legacyChannel != null) {
            legacySerialize(buffer);
            player.sendPluginMessage(NoxesiumUtils.getPlugin(), legacyChannel, buffer.array());
            return true;
        }
        return false;
    }
}
