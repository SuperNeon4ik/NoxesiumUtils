package me.superneon4ik.noxesiumutils.network;

public abstract class NoxesiumPacket {
    public final String channel;
    public final int version;

    public NoxesiumPacket(String channel, int version) {
        this.channel = channel;
        this.version = version;
    }
}
