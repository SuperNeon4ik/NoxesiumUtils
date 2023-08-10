package me.superneon4ik.noxesiumutils.network.serverbound;

import me.superneon4ik.noxesiumutils.NoxesiumUtils;
import me.superneon4ik.noxesiumutils.modules.FriendlyByteBuf;
import org.bukkit.entity.Player;

public class ServerboundClientInformationPacket extends ServerboundNoxesiumPacket {
    public ServerboundClientInformationPacket() {
        super(NoxesiumUtils.NOXESIUM_V1_CLIENT_INFORMATION_CHANNEL, 1);
    }

    @Override
    public void receive(Player player, int version, FriendlyByteBuf buffer) {
        NoxesiumUtils.getManager().registerClient(player.getUniqueId(), buffer.readVarInt());
    }
}
