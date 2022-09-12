package me.screamingbetawars;

import net.minecraft.server.*;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerListener;
import me.screamingbetawars.Main.EventHandler;

import java.util.EventListener;

public class Entity extends PlayerListener implements Listener, EventListener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        if(Game.joined_players.containsKey(event.getPlayer().getName())) {
            if(Game.npcs.containsKey(event.getRightClicked().getEntityId())) openShop(event.getPlayer());
        }
    }
    public static void openShop(Player player) {
        try {
            Packet100OpenWindow test = new Packet100OpenWindow();
            test.a = 100;
            test.b = 0;
            test.c = "BedWars Shop";
            test.d = 9;
            ((CraftPlayer) player).getHandle().netServerHandler.sendPacket(test);
            Packet103SetSlot test2 = new Packet103SetSlot();
            test2.a = test.a;
            test2.b = 0;
            ItemStack item = new ItemStack(Item.BOW);
            item.count = 1;
            test2.c = item;
            ((CraftPlayer) player).getHandle().netServerHandler.sendPacket(test2);
        } catch(NullPointerException e) { player.sendMessage(e.getStackTrace()[0].toString()); }
        player.sendMessage(ChatColor.GOLD + "You've opened the shop!");
    }
}
