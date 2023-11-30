package me.screamingbetawars;

import net.minecraft.server.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerListener;
import me.screamingbetawars.Main.EventHandler;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Entity extends PlayerListener implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        if ((!Game.getPlayerMap(event.getPlayer().getName()).equals("")) && (Game.isGameNPC(event.getRightClicked().getEntityId()))) {
            openShop(event.getPlayer());
        }
    }

    // Packet102WindowClick - possible way of receiving clicked item slots

    public static void openShop(Player player) {
        try {
            Packet100OpenWindow shop = new Packet100OpenWindow();
            shop.a = 100;
            shop.b = 0;
            shop.c = "BedWars Shop";
            shop.d = 27;
            ((CraftPlayer) player).getHandle().netServerHandler.sendPacket(shop);
            for (int i = 1; i<=9; i++) {
                String data = ConfigManager.cfg.get("shop_file", "slot-" + i);
                Matcher matcher = Pattern.compile("(.+?);").matcher(data);
                ArrayList<String> data_list = new ArrayList<>();
                while (matcher.find()) {
                    data_list.add(matcher.group(1));
                }
                Packet103SetSlot packet1 = new Packet103SetSlot();
                packet1.a = shop.a;
                packet1.b = i-1;
                ItemStack item1 = new ItemStack(Item.byId[Material.valueOf(data_list.get(0)).getId()]);
                item1.count = Integer.parseInt(data_list.get(1));
                packet1.c = item1;
                ((CraftPlayer) player).getHandle().netServerHandler.sendPacket(packet1);
                Packet103SetSlot packet2 = new Packet103SetSlot();
                packet2.a = shop.a;
                packet2.b = i+17;
                ItemStack item2 = new ItemStack(Item.byId[Material.valueOf(data_list.get(2)).getId()]);
                item2.count = Integer.parseInt(data_list.get(3));
                packet2.c = item2;
                ((CraftPlayer) player).getHandle().netServerHandler.sendPacket(packet2);
            }
        } catch (NullPointerException e) {
            player.sendMessage(e.getStackTrace()[0].toString());
        }
        player.sendMessage(ChatColor.GOLD + "You've opened the shop!");
    }
}