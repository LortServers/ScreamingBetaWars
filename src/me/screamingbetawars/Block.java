package me.screamingbetawars;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import me.screamingbetawars.ConfigManager.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Block extends BlockListener implements Listener {
    static Map<String, Location> blocks = new HashMap<>();
    public static class pos {
        public static int check(Location pos1, Location pos2, Location current) {
            int check_val = 0;
            if (pos1.getX() >= current.getX() && pos2.getX() <= current.getX()) check_val++;
            else if (pos1.getX() <= current.getX() && pos2.getX() >= current.getX()) check_val++;
            if (pos1.getY() >= current.getY() && pos2.getY() <= current.getY()) check_val++;
            else if (pos1.getY() <= current.getY() && pos2.getY() >= current.getY()) check_val++;
            if (pos1.getZ() >= current.getZ() && pos2.getZ() <= current.getZ()) check_val++;
            else if (pos1.getZ() <= current.getZ() && pos2.getZ() >= current.getZ()) check_val++;
            return check_val;
        }
    }

    public void onBlockBreak(BlockBreakEvent event) {
        for (String map : ConfigManager.map_cache.keySet()) {
            if (cfg.check(map, true, false).equals("true")) {
                Location pos1 = cfg.getLocation(map, "pos1");
                Location pos2 = cfg.getLocation(map, "pos2");
                if(pos.check(pos1, pos2, event.getBlock().getLocation()) == 3) {
                    if (Game.joined_players.get(event.getPlayer().getName()).equals(map)) {
                        ArrayList<Location> locations = new ArrayList<>();
                        for(Map<String, Object> entry : Game.game.getTeams(Game.joined_players.get(event.getPlayer().getName())).values()) {
                            locations.add((Location) entry.get("bed1"));
                            locations.add((Location) entry.get("bed2"));
                        }
                        boolean check = false;
                        for(Location bed : locations) {
                            if(bed.distance(event.getBlock().getLocation()) == 0) {
                                check = true;
                                for(Map.Entry<String, Map<String, Object>> entry : Game.game.getTeams(Game.joined_players.get(event.getPlayer().getName())).entrySet()) {
                                    if((event.getBlock().getLocation().getBlockX() == ((Location) entry.getValue().get("bed1")).getBlockX()) && (event.getBlock().getLocation().getBlockY() == ((Location) entry.getValue().get("bed1")).getBlockY()) && (event.getBlock().getLocation().getBlockZ() == ((Location) entry.getValue().get("bed1")).getBlockZ())) {
                                        if(Game.destroyed_beds.get(entry.getKey()) != null) return;
                                        Game.destroyed_beds.put(entry.getKey(), map);
                                        Map<String, String> list2 = Game.game.getWithValue(Game.joined_players, map);
                                        for(String player : list2.keySet()) Bukkit.getPlayer(player).sendMessage(ChatColor.AQUA + "Player " + event.getPlayer().getName() + " has destroyed the bed of team \"" + entry.getKey() + "\".");
                                    } else if((event.getBlock().getLocation().getBlockX() == ((Location) entry.getValue().get("bed2")).getBlockX()) && (event.getBlock().getLocation().getBlockY() == ((Location) entry.getValue().get("bed2")).getBlockY()) && (event.getBlock().getLocation().getBlockZ() == ((Location) entry.getValue().get("bed2")).getBlockZ())) {
                                        if(Game.destroyed_beds.get(entry.getKey()) != null) return;
                                        Game.destroyed_beds.put(entry.getKey(), map);
                                        Map<String, String> list2 = Game.game.getWithValue(Game.joined_players, map);
                                        for(String player : list2.keySet()) Bukkit.getPlayer(player).sendMessage(ChatColor.AQUA + "Player " + event.getPlayer().getName() + " has destroyed the bed of team \"" + entry.getKey() + "\".");
                                    }
                                }
                                break;
                            }
                        }
                        if(blocks.containsValue(event.getBlock().getLocation())) {
                            blocks.remove(map, event.getBlock().getLocation());
                        } else if(!check) event.setCancelled(true);
                    } else event.setCancelled(true);
                }
            }
        }
    }

    public void onBlockPlace(BlockPlaceEvent event) {
        for (String map : ConfigManager.map_cache.keySet()) {
            if (!map.equals("config.yml")) {
                if (cfg.check(map, true, false).equals("true")) {
                    Location pos1 = cfg.getLocation(map, "pos1");
                    Location pos2 = cfg.getLocation(map, "pos2");
                    if(pos.check(pos1, pos2, event.getBlock().getLocation()) == 3) {
                        if (Game.joined_players.get(event.getPlayer().getName()).equals(map)) {
                            blocks.put(map, event.getBlock().getLocation());
                        } else event.setCancelled(true);
                    }
                }
            }
        }
    }

    public void onBlockBurn(BlockBurnEvent event) {
        for (String map : ConfigManager.map_cache.keySet()) {
            if(cfg.check(map, true, false).equals("true")) {
                Location pos1 = cfg.getLocation(map, "pos1");
                Location pos2 = cfg.getLocation(map, "pos2");
                if (pos.check(pos1, pos2, event.getBlock().getLocation()) == 3) event.setCancelled(true);
            }
        }
    }
}
