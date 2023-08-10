package me.superneon4ik.noxesiumutils.network.clientbound;

import it.unimi.dsi.fastutil.ints.IntList;
import me.superneon4ik.noxesiumutils.NoxesiumUtils;
import me.superneon4ik.noxesiumutils.modules.FriendlyByteBuf;

public class ClientboundResetServerRulesPacket extends ClientboundNoxesiumPacket {

    private final IntList indices;
    public ClientboundResetServerRulesPacket(IntList indices) {
        super(NoxesiumUtils.NOXESIUM_V1_RESET_SERVER_RULES_CHANNEL, null,1);
        this.indices = indices;
    }

    @Override
    public void serialize(FriendlyByteBuf buffer) {
        buffer.writeVarInt(indices.size());
        for (Integer index : indices) {
            buffer.writeVarInt(index);
        }
    }
}
