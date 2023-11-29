package me.screamingbetawars;

import net.minecraft.server.Packet60Explosion;

import org.bukkit.*;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;

import me.screamingbetawars.Main.EventHandler;
import me.screamingbetawars.classes.BWGame;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Death extends EntityListener implements Listener {
    public static Map<String, Integer> time = new HashMap<>();

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if(Game.isGameNPC(event.getEntity().getEntityId())) event.setCancelled(true);
    }

    @EventHandler
    public void onEntityHit(EntityDamageByEntityEvent event) {
        if((event.getEntity() instanceof Player) && (event.getDamager() instanceof Player)) {
            if(Game.getPlayerTeam(((Player) event.getEntity()).getName()).equals(Game.getPlayerTeam(((Player) event.getDamager()).getName()))) event.setCancelled(true);
        }
    }

    @EventHandler
    public void onKill(EntityDeathEvent event) {
        if(event.getEntity() instanceof Player) {
            String name = ((Player) event.getEntity()).getName();
            if(Game.isPlayerPlaying(name)) {
                String game = Game.getPlayerMap(name);
                BWGame game2 = Game.getGame(game);
                if(!Game.getPlayerTeam(name).isBedDestroyed()) {
                    time.put(((Player) event.getEntity()).getName(), (int) Instant.now().getEpochSecond() + (Integer.parseInt(ConfigManager.cfg.get("config", "respawn-time"))));
                } else {
                    Game.leaveGame(name);
                    event.getDrops().clear();
                }
                if(Game.stopIfEmpty(game2, game)) event.getDrops().clear();
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        new ConfigIterator(null, map -> {
            for(org.bukkit.block.Block block : event.blockList()) {
                if(Block.pos.check(map, block.getLocation())) {
                    event.setCancelled(true);
                    for(Player player : Bukkit.getOnlinePlayers()) {
                        player.playEffect(event.getLocation(), Effect.SMOKE, 10);
                        Location location = event.getLocation();
                        ((CraftPlayer) player).getHandle().netServerHandler.sendPacket(new Packet60Explosion(location.getX(), location.getY(), location.getZ(), 0.2f, new HashSet<>()));
                    }
                    if(Block.blocks.get(map).contains(block.getLocation())) {
                        block.setType(Material.AIR);
                        ArrayList<Location> block_list = new ArrayList<>(Block.blocks.get(map));
                        block_list.remove(block.getLocation());
                        Block.blocks.put(map, block_list);
                    }
                }
            }
        });
    }
}