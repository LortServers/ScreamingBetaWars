package me.screamingbetawars;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import me.screamingbetawars.ConfigManager.*;
import me.screamingbetawars.classes.*;

import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;

public class Game {

    static ArrayList<BWGame> games = new ArrayList<>();

    public static BWGame getGame(String map) {
        boolean exists = false; // I doubt that this is the proper way to do this.
        for (BWGame game : games) {
            if (game.getName().equals(map)) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            games.add(new BWGame(map));
        }
        BWGame game = games.get(games.size() - 1); // I'm unsure of how access to variables works in Java, so it gets the game from the ArrayList instead of directly putting it into a variable (just a bit above).
        if (game.getTeams() == null) {
            ArrayList<BWTeam> team_list = new ArrayList<>();
            Map<String, Object> teams = cfg.getStartingWith(map, "team-");
            for (Map.Entry<String, Object> team_data : teams.entrySet()) {
                String name = team_data.getKey().replace("team-", ""), color = String.valueOf(team_data.getValue());
                int size = Integer.parseInt(cfg.get(map, "size-team-" + name));
                Location bed1 = new Location(Bukkit.getServer().getWorld(cfg.get(map, "world")), cfg.getInt(map, "x1-bed-team-" + name), cfg.getInt(map, "y1-bed-team-" + name), cfg.getInt(map, "z1-bed-team-" + name));
                Location bed2 = new Location(Bukkit.getServer().getWorld(cfg.get(map, "world")), cfg.getInt(map, "x2-bed-team-" + name), cfg.getInt(map, "y2-bed-team-" + name), cfg.getInt(map, "z2-bed-team-" + name));
                Location villager = new Location(Bukkit.getServer().getWorld(cfg.get(map, "world")), cfg.getDouble(map, "villager-x-team-" + name), cfg.getDouble(map, "villager-y-team-" + name), cfg.getDouble(map, "villager-z-team-" + name), Float.parseFloat(cfg.get(map, "villager-yaw-team-" + name)), Float.parseFloat(cfg.get(map, "villager-pitch-team-" + name)));
                Location spawn = new Location(Bukkit.getServer().getWorld(cfg.get(map, "world")), cfg.getDouble(map, "x-spawn-team-" + name), cfg.getDouble(map, "y-spawn-team-" + name), cfg.getDouble(map, "z-spawn-team-" + name));
                team_list.add(new BWTeam(name, color, size, bed1, bed2, villager, spawn));
            }
            game.setTeams(team_list);
        }
        if (game.getSpawners() == null) {
            ArrayList<BWSpawner> spawners = new ArrayList<>();
            Map<String, Object> spawner_data = cfg.getStartingWith(map, "spawner-x-");
            for (Object spawner_name : spawner_data.keySet()) {
                String spawner = spawner_name.toString().replace("spawner-x-", "");
                Location location = new Location(Bukkit.getServer().getWorld(cfg.get(map, "world")), cfg.getInt(map, "spawner-x-" + spawner), cfg.getInt(map, "spawner-y-" + spawner), cfg.getInt(map, "spawner-z-" + spawner));
                spawners.add(new BWSpawner(location, cfg.get(map, "spawner-type-" + spawner), cfg.getInt(map, "spawner-time-" + spawner)));
            }
            game.setSpawners(spawners);
        }
        return game;
    }

    public static String joinGame(String map, String nick) {
        BWGame game = getGame(map);
        if (!game.isPlayerPlaying(nick)) {
            if (!game.hasStarted()) {
                game.addPlayer(nick);
            } else {
                return ChatColor.RED + "This game has already started!";
            }
        } else {
            return ChatColor.RED + "You are already in a game!";
        }
        Player player = Bukkit.getPlayer(nick);
        PlayerInventory player_inventory = player.getInventory();
        game.getPlayer(nick).setPreGameInventory(new BWPlayerPreGameInventory(player_inventory.getContents(), player_inventory.getArmorContents()));
        player_inventory.setArmorContents(new ItemStack[Bukkit.getPlayer(nick).getInventory().getArmorContents().length]);
        player_inventory.clear();
        player.teleport(cfg.getLocation(map, "lobby-"));
        player.sendMessage(ChatColor.AQUA + "You've successfully joined a map!");
        int size = 0;
        for (BWTeam team : game.getTeams()) {
            size += team.getSize();
        }
        if (game.getPlayerCount() == 1) {
            proceedGame(map);
        }
        for (BWPlayer bw_player : game.getPlayers()) {
            bw_player.sendMessage(ChatColor.AQUA + "Player " + nick + " has joined the map (" + game.getPlayerCount() + "/" + size + ")!");
        }
        ArrayList<BWTeam> available_game_teams = game.getTeams();
        for (BWTeam team : game.getTeams()) {
            if (team.getSize() == game.getTeamCount(team.getName())) {
                available_game_teams.remove(team);
            }
        }
        String teams = "";
        boolean check = false;
        for (BWTeam team : available_game_teams) {
            if (check) {
                teams += ", " + team.getName();
            } else {
                teams += team.getName();
                check = true;
            }
        }
        return ChatColor.GOLD + "Please pick a team using /bw pick <team>. Available teams: " + teams + ".";
    }

    public static Map<String, String> getWithKey(Map<String, String> data, String key) {
        for (String value : data.keySet()) {
            if (!value.startsWith(key)) {
                data.remove(value);
            }
        }
        return data;
    }

    public static Map<String, String> getWithValue(Map<String, String> data, String value) {
        for (Map.Entry<String, String> data_value : data.entrySet()) {
            if (!data_value.getValue().startsWith(value)) {
                data.remove(data_value.getKey());
            }
        }
        return data;
    }

    public static int countWithValue(Map<String, String> data, String value) {
        int count = 0;
        for (Map.Entry<String, String> data_value : data.entrySet()) {
            if (data_value.getValue().startsWith(value)) {
                count++;
            }
        }
        return count;
    }

    public static String joinTeam(String nick, String team) {
        String map = getPlayerMap(nick);
        BWGame game = getGame(map);
        BWTeam game_team = game.getTeam(team);
        if (game.hasStarted()) {
            return ChatColor.RED + "You cannot change your team after the game has started!";
        }
        if (game.isPlayerPlaying(nick)) {
            if (game_team != null) {
                if (game.getTeamCount(team) != game_team.getSize()) {
                    game.getPlayer(nick).setTeam(game_team);
                    //leaveTeam(nick);
                    return ChatColor.AQUA + "Successfully joined team \"" + team + "\"!";
                } else {
                    return ChatColor.RED + "Team \"" + team + "\" is full!";
                }
            } else {
                return ChatColor.RED + "Given team does not exist!";
            }
        } else {
            return ChatColor.RED + "You are not playing on any map!";
        }
    }

    public static BWTeam getPlayerTeam(String nick) {
        return getGame(getPlayerMap(nick)).getPlayer(nick).getTeam();
    }

    public static String leaveTeam(String nick) {
        String map = getPlayerMap(nick);
        BWGame game = getGame(map);
        if (game.hasStarted()) {
            return ChatColor.RED + "You cannot change your team after the game has started!";
        }
        if (game.getPlayer(nick).getTeam() != null) {
            game.getPlayer(nick).setTeam(null);
            return ChatColor.AQUA + "Successfully left the team!";
        } else {
            return ChatColor.RED + "You don't belong to any team!";
        }
    }

    public static String leaveGame(String nick) {
        if (!isPlayerPlaying(nick)) {
            return ChatColor.RED + "You are not playing!";
        }
        String map = getPlayerMap(nick);
        BWGame game = getGame(map);
        //if(!game2.joined_players.contains(nick)) return ChatColor.RED + "You are not playing!";
        Bukkit.getPlayer(nick).getInventory().clear();
        int size = 0;
        for (BWTeam team : game.getTeams()) {
            size += team.getSize();
        }
        for (BWPlayer player : game.getPlayers()) {
            player.sendMessage(ChatColor.AQUA + "Player " + nick + " has left the map (" + (game.getPlayerCount() - 1) + "/" + size + ")!");
        }
        /*String random_team = "";
        if(game2.joined_players.size() > 0) {
            for (String team : game2.joined_players) {
                random_team = team;
                break;
            }
            if(game2.joined_players.size() == getWithValue(game2.teams, random_team).size()) {
                for (String player : game2.joined_players) {
                    Bukkit.getPlayer(player).sendMessage(ChatColor.AQUA + "Team " + random_team + " has won!");
                    endGame(game);
                }
            }
        } else*/
        if (cfg.getLocation("config", "spawn-") != null) {
            Location spawn = cfg.getLocation("config", "spawn-");
            spawn.setYaw(Float.parseFloat(cfg.get("config", "spawn-yaw")));
            spawn.setPitch(Float.parseFloat(cfg.get("config", "spawn-pitch")));
            Bukkit.getPlayer(nick).teleport(spawn);
        } else {
            Bukkit.getPlayer(nick).teleport(Bukkit.getPlayer(nick).getWorld().getSpawnLocation());
        }
        BWPlayerPreGameInventory player_inventory = game.getPlayer(nick).getPreGameInventory();
        Bukkit.getPlayer(nick).getInventory().setContents(player_inventory.getInventory());
        Bukkit.getPlayer(nick).getInventory().setArmorContents(player_inventory.getArmor());
        game.removePlayer(nick);
        stopIfEmpty(game, map);
        return ChatColor.AQUA + "You've left a map!";
    }

    public static void proceedGame(String name) {
        BWGame game = getGame(name);
        if (game.getPlayerCount() != 1) {
            return;
        }
        int size = 0;
        for (BWTeam team : game.getTeams()) {
            size += team.getSize();
        }
        AtomicBoolean start = new AtomicBoolean(false);
        final AtomicInteger count = new AtomicInteger(10);
        int finalSize = size;
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(new Main(), () -> {
            if (game.getPlayerCount() > (finalSize / 2)) {
                if (count.get() == 0) {
                    if (game.hasStarted()) {
                        return;
                    }
                    for (BWPlayer player : game.getPlayers()) {
                        player.sendMessage(ChatColor.AQUA + "Game has been started!");
                    }
                    startGame(name);
                    return;
                }
                for (BWPlayer player : game.getPlayers()) {
                    player.sendMessage(ChatColor.AQUA + "Game starts in " + count.get() + " seconds!");
                }
                count.set(count.get()-1);
                start.set(true);
            } else if (start.get()) {
                for (BWPlayer player : game.getPlayers()) {
                    player.sendMessage(ChatColor.AQUA + "Not enough players, counting has been paused.");
                }
                count.set(10);
                start.set(false);
            }
        }, 0L, 20L);
    }

    public static String forceStart(String nick) {
        String map = getPlayerMap(nick);
        BWGame game = getGame(map);
        if (game != null) {
            if ((game.getPlayerCount() > 1) || (Objects.equals(cfg.get("config", "debug"), "true"))) {
                startGame(map);
                return ChatColor.AQUA + "Game has been started successfully!";
            } else {
                return ChatColor.RED + "You need at least two players to play!";
            }
        } else {
            return ChatColor.RED + "You are not playing on any map!";
        }
    }

    public static void startGame(String map) {
        BWGame game = getGame(map);
        ArrayList<BWTeam> map_teams = game.getTeams();
        if (game.getPlayersInTeamCount() != game.getPlayerCount()) {
            for (BWPlayer player : game.getPlayers()) {
                if (player.getTeam() == null) {
                    for (BWTeam team : game.getTeams()) {
                        if (game.getTeamCount(team.getName()) < team.getSize()) {
                            joinTeam(player.getName(), team.getName());
                            break;
                        }
                    }
                }
            }
        }
        for (BWTeam team : map_teams) {
            game.beds.put(team.getBedPos1().getBlock().getState().getData(), team.getBedPos1());
            game.beds.put(team.getBedPos2().getBlock().getState().getData(), team.getBedPos2());
        }
        for (BWPlayer player : game.getPlayers()) Bukkit.getPlayer(player.getName()).teleport(game.getTeam(getPlayerTeam(player.getName()).getName()).getSpawn());
        for (BWTeam team : game.getTeams()) createNpc(map, team.getVillager());
        for (BWSpawner spawner : game.getSpawners()) {
            int id = Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(new Main(), () -> {
                if(game == null) {
                    return;
                }
                game.items.add(
                    Bukkit.getWorld(cfg.get(map, "world")).dropItem(
                        spawner.getLocation(),
                        new ItemStack(Material.getMaterial(spawner.getType()), 1)
                    )
                );
            }, 0L, (spawner.getTime() * 20L));
            game.task_ids.add(id);
        }
        Block.blocks.put(map, new ArrayList<>());
        game.setStarted(true);
    }

    public static String endGame(String map) {
        BWGame game = getGame(map);
        if (game == null) {
            return ChatColor.RED + "The given map is not running!";
        }
        for (Map.Entry<String, ArrayList<Location>> blocks : Block.blocks.entrySet()) {
            if (blocks.getKey().equals(map)) {
                for (Location location : blocks.getValue()) {
                    location.getBlock().setType(Material.AIR);
                }
            }
        }
        for (Map.Entry<MaterialData, Location> bed : game.beds.entrySet()) {
            //Bukkit.getWorld(cfg.get(game, "world")).getBlockAt(bed.getValue()).setType(bed.getKey());
            Bukkit.getWorld(cfg.get(map, "world")).getBlockAt(bed.getValue()).setTypeIdAndData(bed.getKey().getItemTypeId(), bed.getKey().getData(), false);
        }
        ArrayList<BWPlayer> players_copy = new ArrayList<>(game.getPlayers());
        for (BWPlayer player : players_copy) leaveGame(player.getName());
        ArrayList<Integer> tasks_copy = new ArrayList<>(game.task_ids);
        for (Integer data : tasks_copy) {
            Bukkit.getScheduler().cancelTask(data);
            game.task_ids.remove(data);
        }
        HashMap<Integer, Location> npcs_copy = new HashMap<>(game.npcs);
        for (Map.Entry<Integer, Location> data : npcs_copy.entrySet()) {
            try {
                for (Entity e : data.getValue().getWorld().getEntities()) {
                    if (e.getEntityId() == data.getKey()) {
                        e.remove();
                    }
                }
                game.npcs.remove(data.getKey());
            } catch(Exception ignored) {}
        }
        ArrayList<Item> items_copy = new ArrayList<>(game.items);
        for (Item item : items_copy) {
            item.remove();
            game.items.remove(item);
        }
        for (Map.Entry<Location, ItemStack[]> entry : game.chests.entrySet()) {
            if (entry.getKey().getBlock().getState() instanceof Chest) {
                Chest chest = (Chest) entry.getKey().getBlock().getState();
                ItemStack[] item_stack = new ItemStack[entry.getValue().length];
                for (int i = 0; i < entry.getValue().length; i++) {
                    ItemStack item_stack_copy = entry.getValue()[i];
                    if (item_stack_copy != null) {
                        item_stack[i] = new ItemStack(item_stack_copy.getType());
                        item_stack[i].setAmount(item_stack_copy.getAmount());
                        item_stack[i].setDurability(item_stack_copy.getDurability());
                        item_stack[i].setData(item_stack_copy.getData());
                    } else {
                        item_stack[i] = null;
                    }
                }
                chest.getInventory().setContents(item_stack);
                chest.update(true);
            }
        }
        game.reset();
        games.remove(map);
        return ChatColor.AQUA + "Map has been stopped successfully.";
    }

    public static void createNpc(String map, Location location) {
        BWGame game2 = getGame(map);
        Sheep npc = (Sheep) Bukkit.getWorld(cfg.get(map, "world")).spawnCreature(location, CreatureType.SHEEP);
        game2.npcs.put(npc.getEntityId(), location);
        npc.setColor(DyeColor.GRAY);
        int id = Bukkit.getScheduler().scheduleAsyncRepeatingTask(Main.instance, () -> npc.teleport(location), 0L, 1L);
        game2.task_ids.add(id);
    }

    public static boolean isPlayerPlaying(String nick) {
        for (BWGame game : games) {
            if (game.isPlayerPlaying(nick)) {
                return true;
            }
        }
        return false;
    }

    public static String getPlayerMap(String nick) {
        for (BWGame game : games) {
            if (game.isPlayerPlaying(nick)) {
                return game.getName();
            }
        }
        return "";
    }

    public static boolean isGameNPC(int id) {
        for (BWGame game : games) {
            if (game.npcs.containsKey(id)) {
                return true;
            }
        }
        return false;
    }

    public static boolean stopIfEmpty(BWGame game, String map) {
        if (game.getPlayerCount() > 0) {
            String team = null;
            for (BWPlayer player : game.getPlayers()) {
                String player_team = player.getTeam().getName();
                if (team == null) {
                    team = player_team;
                } else if (!team.equals(player_team)) {
                    return false;
                }
            }
            for (BWPlayer player : game.getPlayers()) {
                player.sendMessage(ChatColor.AQUA + "Team \"" + team + "\" won!");
            }
        }
        endGame(map);
        return true;
    }

}