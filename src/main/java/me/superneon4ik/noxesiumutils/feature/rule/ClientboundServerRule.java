package me.superneon4ik.noxesiumutils.feature.rule;

import com.noxcrew.noxesium.api.protocol.rule.ServerRule;
import me.superneon4ik.noxesiumutils.modules.FriendlyByteBuf;

public abstract class ClientboundServerRule<T> extends ServerRule<T, FriendlyByteBuf> {

    private final int index;
    private T value = getDefault();

    public ClientboundServerRule(int index) {
        this.index = index;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public T read(FriendlyByteBuf friendlyByteBuf) {
        throw new UnsupportedOperationException("Cannot write a server-side server rule to a buffer");
    }

    public void write(FriendlyByteBuf buffer) {
        write(getValue(), buffer);
    }
}
