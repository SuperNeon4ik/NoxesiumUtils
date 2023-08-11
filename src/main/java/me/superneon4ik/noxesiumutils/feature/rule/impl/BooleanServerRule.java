package me.superneon4ik.noxesiumutils.feature.rule.impl;

import me.superneon4ik.noxesiumutils.feature.rule.ClientboundServerRule;
import me.superneon4ik.noxesiumutils.modules.FriendlyByteBuf;

public class BooleanServerRule extends ClientboundServerRule<Boolean> {
    private final boolean defaultValue;

    public BooleanServerRule(int index, boolean defaultValue) {
        super(index);
        this.defaultValue = defaultValue;
    }

    @Override
    protected Boolean getDefault() {
        return defaultValue;
    }

    @Override
    public void write(Boolean value, FriendlyByteBuf buffer) {
        buffer.writeBoolean(value);
    }
}
