package me.screamingbetawars.classes;

import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.HashMap;

public class BWGame {
    private boolean started = false;
    public static ArrayList<BWPlayer> players = new ArrayList<>();
    public static ArrayList<BWTeam> teams = new ArrayList<>();
    public static HashMap<MaterialData, Location> beds = new HashMap<>();
    public static HashMap<Integer, Location> npcs = new HashMap<>();
    public static ArrayList<Integer> task_ids = new ArrayList<>();
    public static ArrayList<Item> items = new ArrayList<>();
    public static HashMap<Location, ItemStack[]> chests = new HashMap<>();
    public void reset() {
        started = false;
        players = new ArrayList<>();
        beds = new HashMap<>();
        npcs = new HashMap<>();
        task_ids = new ArrayList<>();
        items = new ArrayList<>();
        chests = new HashMap<>();
    }
    public void addPlayer(String name) {
        players.add(new BWPlayer(name)); // We're assuming that whoever uses this public function does acknowledge that you are able to add the same player multiple times.
    }
    public void removePlayer(String name) {
        players.remove(getPlayer(name));
    }
    public BWPlayer getPlayer(String name) {
        for(BWPlayer player : players) { if(player.getName().equals(name)) return player; }
        return null;
    }
    public boolean isPlayerPlaying(String name) { return (getPlayer(name) != null); }
    public int getPlayerCount() { return players.size(); }
    public ArrayList<BWPlayer> getPlayers() { return players; }
    public int getPlayersInTeamCount() {
        int count = 0;
        for(BWPlayer player : players) { if(player.getTeam() != null) count++; }
        return count;
    }

    public boolean hasStarted() { return started; }
    public void setStarted(boolean s) { started = s; }

    public int getTeamCount(String name) {
        int count = 0;
        for(BWPlayer player : players) { if(player.getTeam().getName().equals(name)) count++; }
        return count;
    }

    public BWTeam getTeam(String name) {
        for(BWTeam team : teams) { if(team.getName().equals(name)) return team; }
        return null;
    }
    public ArrayList<BWTeam> getTeams() { return teams; }
    public void setTeams(ArrayList<BWTeam> t) { teams = t; }
}