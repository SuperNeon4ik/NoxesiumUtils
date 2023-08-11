package me.superneon4ik.noxesiumutils.network.clientbound;

import me.superneon4ik.noxesiumutils.NoxesiumUtils;
import me.superneon4ik.noxesiumutils.modules.FriendlyByteBuf;

public class ClientboundServerInformationPacket extends ClientboundNoxesiumPacket {
    private final int maxProtocolVersion;

    public ClientboundServerInformationPacket(int maxProtocolVersion) {
        super(NoxesiumUtils.NOXESIUM_V1_SERVER_INFORMATION_CHANNEL, null, 1);
        this.maxProtocolVersion = maxProtocolVersion;
    }

    @Override
    public void serialize(FriendlyByteBuf buffer) {
        buffer.writeVarInt(maxProtocolVersion);
    }
}
