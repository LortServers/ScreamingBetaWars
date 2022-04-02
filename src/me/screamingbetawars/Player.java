package me.screamingbetawars;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Sheep;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.time.Instant;

public class Player extends PlayerListener implements Listener {
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        if(Game.joined_players.containsKey(event.getPlayer().getName())) {
            if(event.getRightClicked() instanceof Sheep) {
                if(((Sheep) event.getRightClicked()).getColor().equals(DyeColor.GRAY)) {
                    if (Game.npcs.containsKey((Sheep) event.getRightClicked())) openShop(event.getPlayer());
                }
            }
        }
    }

    public void onPlayerUse(PlayerInteractEvent event) {
        for (String map : ConfigManager.map_cache.keySet()) {
            if(ConfigManager.cfg.check(map, true, false).equals("true")) {
                Location pos1 = ConfigManager.cfg.getLocation(map, "pos1");
                Location pos2 = ConfigManager.cfg.getLocation(map, "pos2");
                if((event.getAction() == Action.RIGHT_CLICK_BLOCK) || (event.getAction() == Action.LEFT_CLICK_BLOCK)) {
                    if(Block.pos.check(pos1, pos2, event.getClickedBlock().getLocation()) == 3) event.setCancelled(true);
                }
            }
        }
    }

    public void onRespawn(PlayerRespawnEvent event) {
        if(Game.joined_players.containsKey(event.getPlayer().getName())) {
            if(Death.time.get(event.getPlayer().getName()) >= Instant.now().getEpochSecond()) event.getPlayer().teleport((Location) Game.game.getTeams(Game.joined_players.get(event.getPlayer().getName())).get(Game.game.getPlayerTeam(Game.joined_players.get(event.getPlayer().getName()), event.getPlayer().getName())).get("spawn"));
            else {
                event.getPlayer().teleport(ConfigManager.cfg.getLocation(Game.joined_players.get(event.getPlayer().getName()), "spec-"));
                Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(new Main(), new Runnable() {
                    public void run() {
                        if(Death.time.get(event.getPlayer().getName()) >= Instant.now().getEpochSecond()) {
                            event.getPlayer().teleport((Location) Game.game.getTeams(Game.joined_players.get(event.getPlayer().getName())).get(Game.game.getPlayerTeam(Game.joined_players.get(event.getPlayer().getName()), event.getPlayer().getName())).get("spawn"));
                            return;
                        } else event.getPlayer().sendMessage(ChatColor.AQUA + "You will respawn in " + (Instant.now().getEpochSecond() - Death.time.get(event.getPlayer().getName())) + " seconds!");
                    }
                }, 0L, 20L);
            }
        }
    }

    public static void openShop(org.bukkit.entity.Player player) {
        player.sendMessage("test");
    }
}
