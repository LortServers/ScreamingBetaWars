package me.screamingbetawars;

import net.minecraft.server.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import me.screamingbetawars.Main.EventHandler;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Player extends PlayerListener implements Listener, EventListener {
    /*@EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        for (String map : ConfigManager.map_cache.keySet()) {
            if(ConfigManager.cfg.check(map, true, false).equals("true")) {
                Location pos1 = ConfigManager.cfg.getLocation(map, "pos1");
                Location pos2 = ConfigManager.cfg.getLocation(map, "pos2");
                if((event.getAction() == Action.RIGHT_CLICK_BLOCK) || (event.getAction() == Action.LEFT_CLICK_BLOCK)) {
                    assert pos1 != null;
                    if(Block.pos.check(pos1, pos2, event.getClickedBlock().getLocation()) == 3) event.setCancelled(true);
                }
            }
        }
    }*/

    HashMap<String, Integer> respawn_run_ids = new HashMap<>();

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        String nick = event.getPlayer().getName();
        if(Game.isPlayerPlaying(nick)) {
            String game = Game.getPlayerMap(nick);
            if(Death.time.get(nick) <= Instant.now().getEpochSecond()) event.setRespawnLocation(Game.getTeam(game, Game.getPlayerTeam(nick).getName()).getSpawn());
            else {
                event.setRespawnLocation(ConfigManager.cfg.getLocation(game, "spec-"));
                respawn_run_ids.put(nick,
                    Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(new Main(), () -> {
                        if(Death.time.get(nick) <= Instant.now().getEpochSecond()) {
                            event.getPlayer().teleport(Game.getTeam(game, Game.getPlayerTeam(nick).getName()).getSpawn());
                            removePlayerFromRespawn(nick);
                        } else event.getPlayer().sendMessage(ChatColor.AQUA + "You will respawn in " + (Death.time.get(nick) - Instant.now().getEpochSecond()) + " seconds!");
                    }, 0L, 20L)
                );
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) throws NoSuchFieldException, IllegalAccessException {
        playerOverride(event.getPlayer());
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        event.getPlayer().updateInventory();
    }

    public void removePlayerFromRespawn(String nick) {
        Death.time.remove(nick);
        Bukkit.getScheduler().cancelTask(respawn_run_ids.get(nick));
        respawn_run_ids.remove(nick);
    }

    @EventHandler
    public void onMessage(PlayerChatEvent event) {
        String nick = event.getPlayer().getName();
        if((Game.isPlayerPlaying(nick)) && (Game.getGame(Game.getPlayerMap(nick)).hasStarted())) {
            String team = Game.getPlayerTeam(nick).getName();
            event.setFormat("[" + ChatColor.valueOf(Game.getTeam(Game.getPlayerMap(nick), team).getColor()) + team.toUpperCase() + ChatColor.WHITE + "] <" + nick + "> " + event.getMessage());
        }
    }

    @EventHandler
    public void onPlayerSleep(PlayerBedEnterEvent event) {
        if(Game.isPlayerPlaying(event.getPlayer().getName())) event.setCancelled(true);
    }

    public static void playerOverride(org.bukkit.entity.Player player) throws NoSuchFieldException, IllegalAccessException {
        final NetworkManager mgr = ((CraftPlayer) player).getHandle().netServerHandler.networkManager;
        Field field = mgr.getClass().getDeclaredField("m");
        field.setAccessible(true);
        field.set(mgr, Collections.synchronizedList(new ArrayList<Packet>() {
            @Override
            public boolean add(Packet p) {
                if(p instanceof Packet102WindowClick) {
                    Packet102WindowClick packet = (Packet102WindowClick) p;
                    if(packet.a == 100) {
                        if(Game.isPlayerPlaying(player.getName())) {
                            if((packet.b >= 0) && (packet.b <= 8)) {
                                String item = "", item_price = "";
                                int item_amount = 100, cost = 100;
                                String data = ConfigManager.cfg.get("shop_file", "slot-" + (packet.b + 1));
                                Matcher matcher = Pattern.compile("(.+?);").matcher(data);
                                int iter = 1;
                                while(matcher.find()) {
                                    if(iter == 1) item = matcher.group(1);
                                    if(iter == 2) item_amount = Integer.parseInt(matcher.group(1));
                                    if(iter == 3) item_price = matcher.group(1);
                                    if(iter == 4) cost = Integer.parseInt(matcher.group(1));
                                    iter++;
                                }
                                boolean check = false;
                                for(int i = 0; i < 44; i++) {
                                    try {
                                        ItemStack current_item = player.getInventory().getItem(i);
                                        if(current_item == null) continue;
                                        if (current_item.getTypeId() == Material.valueOf(item_price).getId()) {
                                            if (current_item.getAmount() >= cost) {
                                                player.getInventory().clear(i);
                                                if(current_item.getAmount() > cost) {
                                                    current_item.setAmount(current_item.getAmount() - cost);
                                                    player.getInventory().setItem(i, current_item);
                                                }
                                                ItemStack new_item = new ItemStack(Material.valueOf(item));
                                                new_item.setAmount(item_amount);
                                                player.updateInventory();
                                                player.getWorld().dropItemNaturally(player.getLocation(), new_item);
                                                player.sendMessage(ChatColor.GOLD + "You've bought an item!");
                                                check = true;
                                                break;
                                            }
                                        }
                                    } catch (ArrayIndexOutOfBoundsException ignored) {}
                                }
                                if(!check) player.sendMessage(ChatColor.RED + "You don't have enough resources!");
                            }
                        } else player.sendMessage(ChatColor.RED + "You can't do that.");
                        if((packet.b != -999) && (packet.e != null)) {
                            player.updateInventory();
                            Packet103SetSlot packet1 = new Packet103SetSlot();
                            packet1.a = packet.a;
                            packet1.b = packet.b;
                            packet1.c = packet.e;
                            ((CraftPlayer) player).getHandle().netServerHandler.sendPacket(packet1);
                        }
                        return false;
                    }
                }
                super.add(p);
                return true;
            }
        }));
    }
}