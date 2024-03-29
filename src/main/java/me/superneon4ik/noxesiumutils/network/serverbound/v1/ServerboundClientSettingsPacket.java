package me.superneon4ik.noxesiumutils.network.serverbound.v1;

import com.noxcrew.noxesium.api.protocol.ClientSettings;
import me.superneon4ik.noxesiumutils.NoxesiumUtils;
import me.superneon4ik.noxesiumutils.modules.FriendlyByteBuf;
import org.bukkit.entity.Player;

public class ServerboundClientSettingsPacket extends ServerboundNoxesiumPacket {

    public ServerboundClientSettingsPacket() {
        super(NoxesiumUtils.NOXESIUM_V1_CLIENT_SETTINGS_CHANNEL, 1);
    }

    @Override
    public void receive(Player player, int version, FriendlyByteBuf buffer) {
        ClientSettings clientSettings = new ClientSettings(
                buffer.readVarInt(),
                buffer.readDouble(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readDouble()
        );

        NoxesiumUtils.getManager().updateClientSettings(player.getUniqueId(), clientSettings);
    }
}
