package me.screamingbetawars;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;

import me.screamingbetawars.ConfigManager.*;
import me.screamingbetawars.Main.EventHandler;
import me.screamingbetawars.classes.BWGame;
import me.screamingbetawars.classes.BWTeam;
import me.screamingbetawars.classes.BWPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Block extends BlockListener implements Listener {
    static Map<String, ArrayList<Location>> blocks = new HashMap<>();
    public static class pos {
        public static boolean check(String map, Location current) {
            int check_val = 0;
            Location pos1 = cfg.getLocation(map, "pos1"), pos2 = cfg.getLocation(map, "pos2");
            if (pos1.getX() >= current.getX() && pos2.getX() <= current.getX()) check_val++;
            else if (pos1.getX() <= current.getX() && pos2.getX() >= current.getX()) check_val++;
            if (pos1.getY() >= current.getY() && pos2.getY() <= current.getY()) check_val++;
            else if (pos1.getY() <= current.getY() && pos2.getY() >= current.getY()) check_val++;
            if (pos1.getZ() >= current.getZ() && pos2.getZ() <= current.getZ()) check_val++;
            else if (pos1.getZ() <= current.getZ() && pos2.getZ() >= current.getZ()) check_val++;
            return (check_val == 3);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location location = event.getBlock().getLocation();
        String player_name = event.getPlayer().getName();
        new ConfigIterator(location, map -> {
            if(Game.getPlayerMap(player_name).equals(map)) {
                ArrayList<Location> locations = new ArrayList<>();
                for(BWTeam team : Game.getTeams(map)) {
                    locations.add(team.getBedPos1());
                    locations.add(team.getBedPos2());
                }
                boolean check = false;
                for(Location bed : locations) {
                    if(bed.distance(location) == 0) {
                        check = true;
                        BWGame game = Game.getGame(map);
                        for(BWTeam team : Game.getTeams(map)) {
                            Location bed1 = team.getBedPos1(), bed2 = team.getBedPos2();
                            if(((location.getBlockX() == bed1.getBlockX()) && (location.getBlockY() == bed1.getBlockY()) && (location.getBlockZ() == bed1.getBlockZ())) || ((location.getBlockX() == bed2.getBlockX()) && (location.getBlockY() == bed2.getBlockY()) && (location.getBlockZ() == bed2.getBlockZ()))) {
                                if(Game.getPlayerTeam(player_name).equals(team.getName())) {
                                    event.getPlayer().sendMessage(ChatColor.RED + "You can't destroy your own bed!");
                                    event.setCancelled(true);
                                    return;
                                }
                                if(!game.destroyed_beds.contains(team.getName())) {
                                    game.destroyed_beds.add(team.getName());
                                    for(BWPlayer player : game.getPlayers()) Bukkit.getPlayer(player.getName()).sendMessage(ChatColor.AQUA + "Player " + player_name + " has destroyed the bed of team \"" + team.getName() + "\".");
                                }
                            }
                        }
                        break;
                    }
                }
                if(blocks.get(map).contains(location)) {
                    ArrayList<Location> block_list = blocks.get(map);
                    block_list.remove(location);
                    blocks.put(map, block_list);
                } else if(!check) event.setCancelled(true);
            } else event.setCancelled(true);
        });
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Location location = event.getBlock().getLocation();
        new ConfigIterator(location, map -> {
            if(Game.getPlayerMap(event.getPlayer().getName()).equals(map)) {
                ArrayList<Location> block_list = blocks.get(map);
                if(block_list == null) block_list = new ArrayList<>();
                block_list.add(location);
                blocks.put(map, block_list);
            } else event.setCancelled(true);
        });
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        Location location = event.getBlock().getLocation();
        new ConfigIterator(location, map -> {
            if(!blocks.get(map).contains(location)) event.setCancelled(true);
        });
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        org.bukkit.block.Block block = event.getClickedBlock();
        if((block.getType().equals(Material.CHEST)) && (event.getAction().equals(Action.RIGHT_CLICK_BLOCK))) {
            Location location = block.getLocation();
            new ConfigIterator(block.getLocation(), map -> {
                if(!blocks.get(map).contains(location)) {
                    if(!Game.getGame(map).hasStarted()) event.setCancelled(true);
                    else Game.getGame(map).chests.putIfAbsent(location, ((Chest) block.getState()).getInventory().getContents());
                }
            });
        }
    }
}