package me.screamingbetawars.classes;

import org.bukkit.Bukkit;

public class BWPlayer {

    private static String name;

    private BWTeam team;

    private BWPlayerPreGameInventory pre_game_inventory;

    public BWPlayer(String n) {
        name = n;
    }

    public String getName() {
        return name;
    }

    public BWTeam getTeam() {
        return team;
    }

    public void setTeam(BWTeam t) {
        team = t;
    }

    public BWPlayerPreGameInventory getPreGameInventory() {
        return pre_game_inventory;
    }

    public void setPreGameInventory(BWPlayerPreGameInventory pgi) {
        pre_game_inventory = pgi;
    }

    public void sendMessage(String s) {
        Bukkit.getPlayer(name).sendMessage(s);
    }

}
