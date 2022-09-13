package me.screamingbetawars;

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
            if(Game.joined_players.containsKey(((Player) event.getEntity()).getName())) {
                if(!Game.destroyed_beds.containsKey(Game.game.getPlayerTeam(Game.joined_players.get(((Player) event.getEntity()).getName()), ((Player) event.getEntity()).getName()))) {
                    time.put(((Player) event.getEntity()).getName(), (int) Instant.now().getEpochSecond() + ConfigManager.cfg.getInt("config", "respawn-time"));
                } else Game.game.leaveGame(((Player) event.getEntity()).getName());
            }
        }
    }
}
