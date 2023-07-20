package me.screamingbetawars;

import net.minecraft.server.Packet60Explosion;
import net.minecraft.server.Packet9Respawn;
import org.bukkit.*;
import org.bukkit.block.NoteBlock;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import me.screamingbetawars.Main.EventHandler;
import org.bukkit.util.noise.OctaveGenerator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.bukkit.Bukkit.getServer;

public class Death extends EntityListener implements Listener {

    public static Map<String, Integer> time = new HashMap<>();

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if(Game.game.isGameNPC(event.getEntity().getEntityId())) event.setCancelled(true);
    }

    @EventHandler
    public void onEntityHit(EntityDamageByEntityEvent event) {
        if((event.getEntity() instanceof Player) && (event.getDamager() instanceof Player)) {
            if(Game.game.getPlayerTeam(((Player) event.getEntity()).getName()).equals(Game.game.getPlayerTeam(((Player) event.getDamager()).getName()))) event.setCancelled(true);
        }
    }

    @EventHandler
    public void onKill(EntityDeathEvent event) {
        if(event.getEntity() instanceof Player) {
            String name = ((Player) event.getEntity()).getName();
            if(Game.game.isPlayerPlaying(name)) {
                String game = Game.game.getPlayerMap(name);
                Game.BWGame game2 = Game.game.getGame(game);
                if(!game2.destroyed_beds.contains(Game.game.getPlayerTeam(name))) {
                    time.put(((Player) event.getEntity()).getName(), (int) Instant.now().getEpochSecond() + (Integer.parseInt(ConfigManager.cfg.get("config", "respawn-time"))));
                } else {
                    Game.game.leaveGame(name);
                    event.getDrops().clear();
                }
                if(Game.game.stopIfEmpty(game2, game)) event.getDrops().clear();
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        for (String map : ConfigManager.map_cache.keySet()) {
            if (ConfigManager.cfg.check(map, true, false).equals("true")) {
                Location pos1 = ConfigManager.cfg.getLocation(map, "pos1");
                Location pos2 = ConfigManager.cfg.getLocation(map, "pos2");
                for(org.bukkit.block.Block block : event.blockList()) {
                    if(Block.pos.check(pos1, pos2, block.getLocation()) == 3) {
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
            }
        }
    }
}
