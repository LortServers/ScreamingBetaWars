package me.screamingbetawars;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import me.screamingbetawars.ConfigManager.*;
import me.screamingbetawars.classes.BWGame;
import me.screamingbetawars.classes.BWPlayer;
import me.screamingbetawars.classes.BWPlayerPreGameInventory;
import me.screamingbetawars.classes.BWTeam;

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
    static HashMap<String, BWGame> games = new HashMap<>();
    public static BWGame getGame(String game) {
        if(games.get(game) == null) games.put(game, new BWGame());
        return games.get(game);
    }
    public static String joinGame(String name, String nick) {
        BWGame game = getGame(name);
        if(!game.isPlayerPlaying(nick)) {
            if(!game.hasStarted()) game.addPlayer(nick);
            else return ChatColor.RED + "This game has already started!";
        } else return ChatColor.RED + "You are already in a game!";
        Player player = Bukkit.getPlayer(nick);
        PlayerInventory player_inventory = player.getInventory();
        game.getPlayer(nick).setPreGameInventory(new BWPlayerPreGameInventory(player_inventory.getContents(), player_inventory.getArmorContents()));
        player_inventory.setArmorContents(new ItemStack[Bukkit.getPlayer(nick).getInventory().getArmorContents().length]);
        player_inventory.clear();
        player.teleport(cfg.getLocation(name, "lobby-"));
        player.sendMessage(ChatColor.AQUA + "You've successfully joined a map!");
        int size = 0;
        for(BWTeam team : getTeams(name)) size += team.getSize();
        if(game.getPlayerCount() == 1) proceedGame(name);
        for(BWPlayer bw_player : game.getPlayers()) Bukkit.getPlayer(bw_player.getName()).sendMessage(ChatColor.AQUA + "Player " + nick + " has joined the map (" + game.getPlayerCount() + "/" + size + ")!");
        ArrayList<BWTeam> available_game_teams = getTeams(name);
        for(BWTeam team : getTeams(name)) { if(team.getSize() == game.getTeamCount(team.getName())) available_game_teams.remove(team); }
        String teams = "";
        boolean check = false;
        for(BWTeam team : available_game_teams) {
            if(check) teams += ", " + team.getName();
            else {
                teams += team.getName();
                check = true;
            }
        }
        return ChatColor.GOLD + "Please pick a team using /bw pick <team>. Available teams: " + teams + ".";
    }

    public static ArrayList<BWTeam> getTeams(String game) {
        ArrayList<BWTeam> team_list = new ArrayList<>();
        Map<String, Object> teams = cfg.getStartingWith(game, "team-");
        for(Map.Entry<String, Object> team_data : teams.entrySet()) {
            String name = team_data.getKey().replace("team-", ""), color = String.valueOf(team_data.getValue());
            int size = Integer.parseInt(cfg.get(game, "size-team-" + name));
            Location bed1 = new Location(Bukkit.getServer().getWorld(cfg.get(game, "world")), cfg.getInt(game, "x1-bed-team-" + name), cfg.getInt(game, "y1-bed-team-" + name), cfg.getInt(game, "z1-bed-team-" + name));
            Location bed2 = new Location(Bukkit.getServer().getWorld(cfg.get(game, "world")), cfg.getInt(game, "x2-bed-team-" + name), cfg.getInt(game, "y2-bed-team-" + name), cfg.getInt(game, "z2-bed-team-" + name));
            Location villager = new Location(Bukkit.getServer().getWorld(cfg.get(game, "world")), cfg.getDouble(game, "villager-x-team-" + name), cfg.getDouble(game, "villager-y-team-" + name), cfg.getDouble(game, "villager-z-team-" + name), Float.parseFloat(cfg.get(game, "villager-yaw-team-" + name)), Float.parseFloat(cfg.get(game, "villager-pitch-team-" + name)));
            Location spawn = new Location(Bukkit.getServer().getWorld(cfg.get(game, "world")), cfg.getDouble(game, "x-spawn-team-" + name), cfg.getDouble(game, "y-spawn-team-" + name), cfg.getDouble(game, "z-spawn-team-" + name));
            team_list.add(new BWTeam(name, color, size, bed1, bed2, villager, spawn));
        }
        return team_list;
    }

    public static BWTeam getTeam(String game, String name) {
        Map<String, Object> team_cfg_data = cfg.getStartingWith(game, "team-" + name);
        for(Map.Entry<String, Object> team_data : team_cfg_data.entrySet()) {
            String color = String.valueOf(team_data.getValue());
            int size = Integer.parseInt(cfg.get(game, "size-team-" + name));
            Location bed1 = new Location(Bukkit.getServer().getWorld(cfg.get(game, "world")), cfg.getInt(game, "x1-bed-team-" + name), cfg.getInt(game, "y1-bed-team-" + name), cfg.getInt(game, "z1-bed-team-" + name));
            Location bed2 = new Location(Bukkit.getServer().getWorld(cfg.get(game, "world")), cfg.getInt(game, "x2-bed-team-" + name), cfg.getInt(game, "y2-bed-team-" + name), cfg.getInt(game, "z2-bed-team-" + name));
            Location villager = new Location(Bukkit.getServer().getWorld(cfg.get(game, "world")), cfg.getDouble(game, "villager-x-team-" + name), cfg.getDouble(game, "villager-y-team-" + name), cfg.getDouble(game, "villager-z-team-" + name), Float.parseFloat(cfg.get(game, "villager-yaw-team-" + name)), Float.parseFloat(cfg.get(game, "villager-pitch-team-" + name)));
            Location spawn = new Location(Bukkit.getServer().getWorld(cfg.get(game, "world")), cfg.getDouble(game, "x-spawn-team-" + name), cfg.getDouble(game, "y-spawn-team-" + name), cfg.getDouble(game, "z-spawn-team-" + name));
            return new BWTeam(name, color, size, bed1, bed2, villager, spawn);
        }
        return null;
    }

    public static Map<String, String> getWithKey(Map<String, String> data, String key) {
        for(String value : data.keySet()) {
            if(!value.startsWith(key)) data.remove(value);
        }
        return data;
    }

    public static Map<String, String> getWithValue(Map<String, String> data, String value) {
        for(Map.Entry<String, String> data_value : data.entrySet()) {
            if(!data_value.getValue().startsWith(value)) data.remove(data_value.getKey());
        }
        return data;
    }

    public static int countWithValue(Map<String, String> data, String value) {
        int count = 0;
        for(Map.Entry<String, String> data_value : data.entrySet()) {
            if(data_value.getValue().startsWith(value)) count++;
        }
        return count;
    }

    public static Map<String, Map<String, Object>> getSpawners(String game) {
        Map<String, Map<String, Object>> spawner_list = new HashMap<>();
        Map<String, Object> spawners = cfg.getStartingWith(game, "spawner-x-");
        for(Object spawner2 : spawners.keySet()) {
            Map<String, Object> current_spawner = new HashMap<>();
            String spawner = spawner2.toString();
            spawner = spawner.replace("spawner-x-", "");
            Location location = new Location(Bukkit.getServer().getWorld(cfg.get(game, "world")), cfg.getInt(game, "spawner-x-" + spawner), cfg.getInt(game, "spawner-y-" + spawner), cfg.getInt(game, "spawner-z-" + spawner));
            current_spawner.put("location", location);
            current_spawner.put("type", cfg.get(game, "spawner-type-" + spawner));
            current_spawner.put("time", cfg.get(game, "spawner-time-" + spawner));
            spawner_list.put(spawner, current_spawner);
        }
        return spawner_list;
    }

    public static String joinTeam(String nick, String team) {
        String name = getPlayerMap(nick);
        BWGame game = getGame(name);
        BWTeam game_team = getTeam(name, team);
        if(game.hasStarted()) return ChatColor.RED + "You cannot change your team after the game has started!";
        if(game.isPlayerPlaying(nick)) {
            if(game_team != null) {
                if(game.getTeamCount(team) != game_team.getSize()) {
                    game.getPlayer(nick).setTeam(game_team);
                    //leaveTeam(nick);
                    return ChatColor.AQUA + "Successfully joined team \"" + team + "\"!";
                } else return ChatColor.RED + "Team \"" + team + "\" is full!";
            } else return ChatColor.RED + "Given team does not exist!";
        } else return ChatColor.RED + "You are not playing on any map!";
    }

    public static BWTeam getPlayerTeam(String nick) {
        return getGame(getPlayerMap(nick)).getPlayer(nick).getTeam();
    }

    public static String leaveTeam(String nick) {
        String map = getPlayerMap(nick);
        BWGame game = getGame(map);
        if(game.hasStarted()) return ChatColor.RED + "You cannot change your team after the game has started!";
        if(game.getPlayer(nick).getTeam() != null) {
            game.getPlayer(nick).setTeam(null);
            return ChatColor.AQUA + "Successfully left the team!";
        } else return ChatColor.RED + "You don't belong to any team!";
    }

    public static String leaveGame(String nick) {
        if(!isPlayerPlaying(nick)) return ChatColor.RED + "You are not playing!";
        String map = getPlayerMap(nick);
        BWGame game = getGame(map);
        //if(!game2.joined_players.contains(nick)) return ChatColor.RED + "You are not playing!";
        Bukkit.getPlayer(nick).getInventory().clear();
        int size = 0;
        for(BWTeam team : getTeams(map)) size += team.getSize();
        for(BWPlayer player : game.getPlayers()) Bukkit.getPlayer(player.getName()).sendMessage(ChatColor.AQUA + "Player " + nick + " has left the map (" + (game.getPlayerCount() - 1) + "/" + size + ")!");
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
        if(cfg.getLocation("config", "spawn-") != null) {
            Location spawn = cfg.getLocation("config", "spawn-");
            spawn.setYaw(Float.parseFloat(cfg.get("config", "spawn-yaw")));
            spawn.setPitch(Float.parseFloat(cfg.get("config", "spawn-pitch")));
            Bukkit.getPlayer(nick).teleport(spawn);
        } else Bukkit.getPlayer(nick).teleport(Bukkit.getPlayer(nick).getWorld().getSpawnLocation());
        BWPlayerPreGameInventory player_inventory = game.getPlayer(nick).getPreGameInventory();
        Bukkit.getPlayer(nick).getInventory().setContents(player_inventory.getInventory());
        Bukkit.getPlayer(nick).getInventory().setArmorContents(player_inventory.getArmor());
        game.removePlayer(nick);
        stopIfEmpty(game, map);
        return ChatColor.AQUA + "You've left a map!";
    }

    public static void proceedGame(String name) {
        BWGame game = getGame(name);
        if(game.getPlayerCount() != 1) return;
        int size = 0;
        for(BWTeam team : getTeams(name)) size += team.getSize();
        AtomicBoolean start = new AtomicBoolean(false);
        final AtomicInteger count = new AtomicInteger(10);
        int finalSize = size;
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(new Main(), () -> {
            if(game.getPlayerCount() > (finalSize / 2)) {
                if(count.get() == 0) {
                    if(game.hasStarted()) return;
                    for(BWPlayer player : game.getPlayers()) Bukkit.getPlayer(player.getName()).sendMessage(ChatColor.AQUA + "Game has been started!");
                    startGame(name);
                    return;
                }
                for(BWPlayer player : game.getPlayers()) Bukkit.getPlayer(player.getName()).sendMessage(ChatColor.AQUA + "Game starts in " + count.get() + " seconds!");
                count.set(count.get()-1);
                start.set(true);
            } else if(start.get()) {
                for(BWPlayer player : game.getPlayers()) Bukkit.getPlayer(player.getName()).sendMessage(ChatColor.AQUA + "Not enough players, counting has been paused.");
                count.set(10);
                start.set(false);
            }
        }, 0L, 20L);
    }

    public static String forceStart(String nick) {
        String map = getPlayerMap(nick);
        BWGame game = getGame(map);
        if(game != null) {
            if((game.getPlayerCount() > 1) || (Objects.equals(cfg.get("config", "debug"), "true"))) {
                startGame(map);
                return ChatColor.AQUA + "Game has been started successfully!";
            } else return ChatColor.RED + "You need at least two players to play!";
        } else return ChatColor.RED + "You are not playing on any map!";
    }

    public static void startGame(String map) {
        BWGame game = getGame(map);
        ArrayList<BWTeam> map_teams = getTeams(map);
        if(game.getPlayersInTeamCount() != game.getPlayerCount()) {
            Map<String, String> game_teams = new HashMap<>();
            for(BWTeam team : map_teams) game_teams.put(team.getName(), String.valueOf(team.getSize())); // I have no idea why this needs size as a string, or basically size at all, see comment just below.
            for(BWPlayer player : game.getPlayers()) {
                if(player.getTeam() == null) {
                    for(BWTeam team : getTeams(map)) {
                        if(game.getTeamCount(team.getName()) < team.getSize()) {
                            joinTeam(player.getName(), team.getName());
                            break;
                        }
                    }
                }
            }
        }
        for(BWTeam team : map_teams) {
            game.beds.put(team.getBedPos1().getBlock().getState().getData(), team.getBedPos1());
            game.beds.put(team.getBedPos2().getBlock().getState().getData(), team.getBedPos2());
        }
        for(BWPlayer player : game.getPlayers()) Bukkit.getPlayer(player.getName()).teleport(getTeam(map, getPlayerTeam(player.getName()).getName()).getSpawn());
        for(BWTeam team : getTeams(map)) createNpc(map, team.getVillager());
        for(Map.Entry<String, Map<String, Object>> spawner : getSpawners(map).entrySet()) {
            int id = Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(new Main(), () -> {
                BWGame game3 = getGame(map);
                if(game3 == null) return;
                game3.items.add(Bukkit.getWorld(cfg.get(map, "world")).dropItem((Location) spawner.getValue().get("location"), new ItemStack(Material.getMaterial(String.valueOf(spawner.getValue().get("type"))), 1)));
            }, 0L, Integer.parseInt(String.valueOf(spawner.getValue().get("time"))) * 20L);
            game.task_ids.add(id);
        }
        Block.blocks.put(map, new ArrayList<>());
        game.setStarted(true);
    }

    public static String endGame(String map) {
        BWGame game = getGame(map);
        if(game == null) return ChatColor.RED + "The given map is not running!";
        for(Map.Entry<String, ArrayList<Location>> blocks : Block.blocks.entrySet()) {
            if(blocks.getKey().equals(map)) {
                for(Location location : blocks.getValue()) location.getBlock().setType(Material.AIR);
            }
        }
        for(Map.Entry<MaterialData, Location> bed : game.beds.entrySet()) {
            //Bukkit.getWorld(cfg.get(game, "world")).getBlockAt(bed.getValue()).setType(bed.getKey());
            Bukkit.getWorld(cfg.get(map, "world")).getBlockAt(bed.getValue()).setTypeIdAndData(bed.getKey().getItemTypeId(), bed.getKey().getData(), false);
        }
        ArrayList<BWPlayer> players_copy = new ArrayList<>(game.getPlayers());
        for(BWPlayer player : players_copy) leaveGame(player.getName());
        ArrayList<Integer> tasks_copy = new ArrayList<>(game.task_ids);
        for(Integer data : tasks_copy) {
            Bukkit.getScheduler().cancelTask(data);
            game.task_ids.remove(data);
        }
        HashMap<Integer, Location> npcs_copy = new HashMap<>(game.npcs);
        for(Map.Entry<Integer, Location> data : npcs_copy.entrySet()) {
            try {
                for(Entity e : data.getValue().getWorld().getEntities()) {
                    if(e.getEntityId() == data.getKey()) e.remove();
                }
                game.npcs.remove(data.getKey());
            } catch(Exception ignored) {}
        }
        ArrayList<Item> items_copy = new ArrayList<>(game.items);
        for(Item item : items_copy) {
            item.remove();
            game.items.remove(item);
        }
        for(Map.Entry<Location, ItemStack[]> entry : game.chests.entrySet()) {
            if(entry.getKey().getBlock().getState() instanceof Chest) {
                Chest chest = (Chest) entry.getKey().getBlock().getState();
                ItemStack[] item_stack = new ItemStack[entry.getValue().length];
                for(int i = 0; i < entry.getValue().length; i++) {
                    ItemStack item_stack_copy = entry.getValue()[i];
                    if(item_stack_copy != null) {
                        item_stack[i] = new ItemStack(item_stack_copy.getType());
                        item_stack[i].setAmount(item_stack_copy.getAmount());
                        item_stack[i].setDurability(item_stack_copy.getDurability());
                        item_stack[i].setData(item_stack_copy.getData());
                    } else item_stack[i] = null;
                }
                chest.getInventory().setContents(item_stack);
                chest.update(true);
            }
        }
        game.reset();
        games.remove(map);
        return ChatColor.AQUA + "Map has been stopped successfully.";
    }

    public static void createNpc(String game, Location location) {
        BWGame game2 = getGame(game);
        Sheep npc = (Sheep) Bukkit.getWorld(cfg.get(game, "world")).spawnCreature(location, CreatureType.SHEEP);
        game2.npcs.put(npc.getEntityId(), location);
        npc.setColor(DyeColor.GRAY);
        int id = Bukkit.getScheduler().scheduleAsyncRepeatingTask(Main.instance, () -> npc.teleport(location), 0L, 1L);
        game2.task_ids.add(id);
    }
    public static boolean isPlayerPlaying(String nick) {
        for(Map.Entry<String, BWGame> data : games.entrySet()) {
            if(data.getValue().isPlayerPlaying(nick)) return true;
        }
        return false;
    }
    public static String getPlayerMap(String nick) {
        for(Map.Entry<String, BWGame> data : games.entrySet()) {
            if(data.getValue().isPlayerPlaying(nick)) return data.getKey();
        }
        return "";
    }
    public static boolean isGameNPC(int id) {
        for(Map.Entry<String, BWGame> data : games.entrySet()) {
            if(data.getValue().npcs.containsKey(id)) return true;
        }
        return false;
    }

    public static boolean stopIfEmpty(BWGame game, String map) {
        if(game.getPlayerCount() > 0) {
            String team = null;
            for (BWPlayer player : game.getPlayers()) {
                String player_team = player.getTeam().getName();
                if(team == null) team = player_team;
                else if(!team.equals(player_team)) return false;
            }
            for(BWPlayer player : game.getPlayers()) Bukkit.getPlayer(player.getName()).sendMessage(ChatColor.AQUA + "Team \"" + team + "\" won!");
        }
        endGame(map);
        return true;
    }
}