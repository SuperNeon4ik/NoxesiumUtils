package me.superneon4ik.noxesiumutils.feature.rule.impl;

import me.superneon4ik.noxesiumutils.feature.rule.ClientboundServerRule;
import me.superneon4ik.noxesiumutils.modules.FriendlyByteBuf;

import java.util.Collections;
import java.util.List;

public class AdventureModeCheckServerRule extends ClientboundServerRule<List<String>> {

    public AdventureModeCheckServerRule(int index) {
        super(index);
    }

    @Override
    protected List<String> getDefault() {
        return Collections.emptyList();
    }

    @Override
    public void write(List<String> value, FriendlyByteBuf buffer) {
        buffer.writeVarInt(value.size());
        for (String s : value) {
            buffer.writeUtf(s);
        }
    }
}
