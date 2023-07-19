package me.screamingbetawars;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import me.screamingbetawars.ConfigManager.*;
import me.screamingbetawars.Main.EventHandler;

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

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        for (String map : ConfigManager.map_cache.keySet()) {
            if (cfg.check(map, true, false).equals("true")) {
                Location pos1 = cfg.getLocation(map, "pos1");
                Location pos2 = cfg.getLocation(map, "pos2");
                if(pos.check(pos1, pos2, event.getBlock().getLocation()) == 3) {
                    if(Game.game.isPlayerPlaying(event.getPlayer().getName())) {
                        if(Game.game.getPlayerMap(event.getPlayer().getName()).equals(map)) {
                            ArrayList<Location> locations = new ArrayList<>();
                            for (Map<String, Object> entry : Game.game.getTeams(map).values()) {
                                locations.add((Location) entry.get("bed1"));
                                locations.add((Location) entry.get("bed2"));
                            }
                            boolean check = false;
                            for (Location bed : locations) {
                                if (bed.distance(event.getBlock().getLocation()) == 0) {
                                    check = true;
                                    Game.BWGame game2 = Game.game.getGame(map);
                                    for (Map.Entry<String, Map<String, Object>> entry : Game.game.getTeams(map).entrySet()) {
                                        if (((event.getBlock().getLocation().getBlockX() == ((Location) entry.getValue().get("bed1")).getBlockX()) && (event.getBlock().getLocation().getBlockY() == ((Location) entry.getValue().get("bed1")).getBlockY()) && (event.getBlock().getLocation().getBlockZ() == ((Location) entry.getValue().get("bed1")).getBlockZ())) || ((event.getBlock().getLocation().getBlockX() == ((Location) entry.getValue().get("bed2")).getBlockX()) && (event.getBlock().getLocation().getBlockY() == ((Location) entry.getValue().get("bed2")).getBlockY()) && (event.getBlock().getLocation().getBlockZ() == ((Location) entry.getValue().get("bed2")).getBlockZ()))) {
                                            if(Game.game.getPlayerTeam(event.getPlayer().getName()).equals(entry.getKey())) {
                                                event.getPlayer().sendMessage(ChatColor.RED + "You can't destroy your own bed!");
                                                event.setCancelled(true);
                                                return;
                                            }
                                            if(game2.destroyed_beds.contains(entry.getKey())) return;
                                            game2.destroyed_beds.add(entry.getKey());
                                            for(String player : game2.joined_players) Bukkit.getPlayer(player).sendMessage(ChatColor.AQUA + "Player " + event.getPlayer().getName() + " has destroyed the bed of team \"" + entry.getKey() + "\".");
                                            game2.destroyed_beds.add(entry.getKey());
                                        }
                                    }
                                    break;
                                }
                            }
                            if (blocks.containsValue(event.getBlock().getLocation())) {
                                blocks.remove(map, event.getBlock().getLocation());
                            } else if (!check) event.setCancelled(true);
                        } else event.setCancelled(true);
                    } else event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        for (String map : ConfigManager.map_cache.keySet()) {
            if (!map.equals("config.yml")) {
                if (cfg.check(map, true, false).equals("true")) {
                    Location pos1 = cfg.getLocation(map, "pos1");
                    Location pos2 = cfg.getLocation(map, "pos2");
                    if(pos.check(pos1, pos2, event.getBlock().getLocation()) == 3) {
                        if(Game.game.isPlayerPlaying(event.getPlayer().getName())) {
                            if (Game.game.getPlayerMap(event.getPlayer().getName()).equals(map)) {
                                blocks.put(map, event.getBlock().getLocation());
                            } else event.setCancelled(true);
                        } else event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
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
