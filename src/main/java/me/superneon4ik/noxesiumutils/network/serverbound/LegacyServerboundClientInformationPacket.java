package me.superneon4ik.noxesiumutils.network.serverbound;

import me.superneon4ik.noxesiumutils.NoxesiumUtils;
import me.superneon4ik.noxesiumutils.modules.FriendlyByteBuf;
import org.bukkit.entity.Player;

public class LegacyServerboundClientInformationPacket extends LegacyServerboundNoxesiumPacket {

    public LegacyServerboundClientInformationPacket() {
        super(NoxesiumUtils.NOXESIUM_LEGACY_CLIENT_INFORMATION_CHANNEL);
    }

    @Override
    public void receive(Player player, FriendlyByteBuf buffer) {
        NoxesiumUtils.getManager().registerClient(player.getUniqueId(), buffer.readInt());
    }
}
