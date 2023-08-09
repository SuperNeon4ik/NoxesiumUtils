package me.superneon4ik.noxesiumutils.network;

import lombok.Getter;

public abstract class NoxesiumPacket {
    public final String channel;
    public final int version;

    public NoxesiumPacket(String channel, int version) {
        this.channel = channel;
        this.version = version;
    }
}
