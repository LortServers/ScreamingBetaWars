package me.screamingbetawars.classes;

import org.bukkit.inventory.ItemStack;

public class BWPlayerPreGameInventory {
    private static ItemStack[] inventory;
    private static ItemStack[] armor;
    public BWPlayerPreGameInventory(ItemStack[] i, ItemStack[] a) {
        inventory = i;
        armor = a;
    }
    public ItemStack[] getInventory() { return inventory; }
    public ItemStack[] getArmor() { return armor; }
}
