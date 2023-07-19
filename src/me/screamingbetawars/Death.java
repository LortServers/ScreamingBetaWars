package me.screamingbetawars;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;
import me.screamingbetawars.Main.EventHandler;

import java.time.Instant;
import java.util.Map;

public class Death extends EntityListener implements Listener {

    public static Map<String, Integer> time;

    @EventHandler
    public void onKill(EntityDeathEvent event) {
        if(event.getEntity() instanceof Player) {
            if(Game.game.isPlayerPlaying(((Player) event.getEntity()).getName())) {
                Bukkit.broadcastMessage("test");
                Game.BWGame game2 = Game.game.getGame(Game.game.getPlayerMap(((Player) event.getEntity()).getName()));
                if(!game2.destroyed_beds.contains(Game.game.getPlayerTeam(((Player) event.getEntity()).getName()))) {
                    time.put(((Player) event.getEntity()).getName(), (int) Instant.now().getEpochSecond() + ConfigManager.cfg.getInt("config", "respawn-time"));
                } else Game.game.leaveGame(((Player) event.getEntity()).getName());
            } else Bukkit.broadcastMessage(((Player) event.getEntity()).getName());
        }
    }
}
