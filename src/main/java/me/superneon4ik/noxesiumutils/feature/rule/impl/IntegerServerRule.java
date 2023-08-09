package me.superneon4ik.noxesiumutils.feature.rule.impl;

import me.superneon4ik.noxesiumutils.feature.rule.ClientboundServerRule;
import me.superneon4ik.noxesiumutils.modules.FriendlyByteBuf;

public class IntegerServerRule extends ClientboundServerRule<Integer> {
    private final int defaultValue;

    public IntegerServerRule(int index, int defaultValue) {
        super(index);
        this.defaultValue = defaultValue;
    }

    @Override
    protected Integer getDefault() {
        return defaultValue;
    }

    @Override
    public void write(Integer value, FriendlyByteBuf buffer) {
        buffer.writeVarInt(value);
    }
}
