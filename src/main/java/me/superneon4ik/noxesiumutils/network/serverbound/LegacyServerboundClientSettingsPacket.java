package me.superneon4ik.noxesiumutils.network.serverbound;

import com.noxcrew.noxesium.api.protocol.ClientSettings;
import me.superneon4ik.noxesiumutils.NoxesiumUtils;
import me.superneon4ik.noxesiumutils.modules.FriendlyByteBuf;
import me.superneon4ik.noxesiumutils.objects.PlayerClientSettings;
import org.bukkit.entity.Player;

public class LegacyServerboundClientSettingsPacket extends LegacyServerboundNoxesiumPacket {

    public LegacyServerboundClientSettingsPacket() {
        super(NoxesiumUtils.NOXESIUM_LEGACY_CLIENT_SETTINGS_CHANNEL);
    }

    @Override
    public void receive(Player player, FriendlyByteBuf buffer) {
        int guiScale = buffer.readVarInt();
        boolean enforceUnicode = buffer.readBoolean();
        ClientSettings clientSettings = new ClientSettings(
                guiScale,
                0,
                0,
                0,
                enforceUnicode,
                false,
                0
        );
        // TODO: Update player
    }
}
