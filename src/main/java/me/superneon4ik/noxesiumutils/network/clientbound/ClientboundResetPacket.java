package me.superneon4ik.noxesiumutils.network.clientbound;

import me.superneon4ik.noxesiumutils.NoxesiumUtils;
import me.superneon4ik.noxesiumutils.enums.ResetFlag;
import me.superneon4ik.noxesiumutils.modules.FriendlyByteBuf;

public class ClientboundResetPacket extends ClientboundNoxesiumPacket {

    private final ResetFlag[] flags;
    public ClientboundResetPacket(ResetFlag... flags) {
        super(NoxesiumUtils.NOXESIUM_V1_RESET_CHANNEL, null, 1);
        this.flags = flags;
    }

    @Override
    public void serialize(FriendlyByteBuf buffer) {
        byte bitMask = 0x00;
        for (ResetFlag flag : flags) {
            bitMask |= flag.getFlag();
        }
        buffer.writeByte(bitMask);
    }
}
