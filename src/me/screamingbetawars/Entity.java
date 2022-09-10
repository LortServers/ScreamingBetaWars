package me.screamingbetawars;

import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerListener;

import java.util.EventListener;

public class Entity extends PlayerListener implements Listener, EventListener {
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        if(Game.joined_players.containsKey(event.getPlayer().getName())) {
            if(Game.npcs.containsKey(event.getRightClicked().getEntityId())) openShop(event.getPlayer());
        }
        event.getPlayer().sendMessage("test");
    }
    public static void openShop(org.bukkit.entity.Player player) {
        player.sendMessage("test");
    }
}
