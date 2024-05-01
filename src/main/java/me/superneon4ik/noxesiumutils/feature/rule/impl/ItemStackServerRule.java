package me.superneon4ik.noxesiumutils.feature.rule.impl;

import me.superneon4ik.noxesiumutils.feature.rule.ClientboundServerRule;
import me.superneon4ik.noxesiumutils.modules.FriendlyByteBuf;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemStackServerRule extends ClientboundServerRule<ItemStack> {
    public ItemStackServerRule(int index) {
        super(index);
    }

    @Override
    protected ItemStack getDefault() {
        return new ItemStack(Material.AIR);
    }

    @Override
    public void write(ItemStack value, FriendlyByteBuf buffer) {
        // TODO: Make a writer
    }
}
