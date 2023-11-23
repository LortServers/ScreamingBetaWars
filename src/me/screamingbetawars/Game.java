package me.screamingbetawars;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import me.screamingbetawars.ConfigManager.*;

import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class Game {
    static class BWGame {
        public static boolean started = false;
        public static ArrayList<String> joined_players = new ArrayList<>();
        static HashMap<String, String> teams = new HashMap<>();
        static HashMap<MaterialData, Location> beds = new HashMap<>();
        public static HashMap<Integer, Location> npcs = new HashMap<>();
        public static ArrayList<String> destroyed_beds = new ArrayList<>();
        public static ArrayList<Integer> task_ids = new ArrayList<>();
        public static ArrayList<Item> items = new ArrayList<>();
        public static HashMap<String, HashMap<ItemStack[], ItemStack[]>> player_inventories = new HashMap<>();
        public static HashMap<Location, ItemStack[]> chests = new HashMap<>();
        public void reset() {
            started = false;
            joined_players = new ArrayList<>();
            teams = new HashMap<>();
            beds = new HashMap<>();
            npcs = new HashMap<>();
            destroyed_beds = new ArrayList<>();
            task_ids = new ArrayList<>();
            items = new ArrayList<>();
            player_inventories = new HashMap<>();
            chests = new HashMap<>();
        }
    }

    static class BWTeam {
        public String name, color;
        public int size;
        public Location bed1, bed2, villager, spawn;
        public BWTeam(String n, String c, int s, Location b1, Location b2, Location v, Location s2) {
            name = n;
            color = c;
            size = s;
            bed1 = b1;
            bed2 = b2;
            villager = v;
            spawn = s2;
        }
    }

    static HashMap<String, BWGame> games = new HashMap<>();
    public static BWGame getGame(String game) {
        if(games.get(game) == null) games.put(game, new BWGame());
        return games.get(game);
    }
    public static String joinGame(String game, String nick) {
        BWGame game2 = getGame(game);
        if(!game2.joined_players.contains(nick)) {
            if(!game2.started) game2.joined_players.add(nick);
            else return ChatColor.RED + "This game has already started!";
        } else return ChatColor.RED + "You are already in a game!";
        HashMap<ItemStack[], ItemStack[]> player_inventory = new HashMap<>();
        player_inventory.put(Bukkit.getPlayer(nick).getInventory().getContents(), Bukkit.getPlayer(nick).getInventory().getArmorContents());
        game2.player_inventories.put(nick, player_inventory);
        Bukkit.getPlayer(nick).getInventory().setArmorContents(new ItemStack[Bukkit.getPlayer(nick).getInventory().getArmorContents().length]);
        Bukkit.getPlayer(nick).getInventory().clear();
        Bukkit.getPlayer(nick).teleport(cfg.getLocation(game, "lobby-"));
        Bukkit.getPlayer(nick).sendMessage(ChatColor.AQUA + "You've successfully joined a map!");
        int size = 0;
        for(BWTeam team : getTeams(game)) size += team.size;
        if(game2.joined_players.size() == 1) proceedGame(game);
        for(String player : game2.joined_players) Bukkit.getPlayer(player).sendMessage(ChatColor.AQUA + "Player " + nick + " has joined the map (" + game2.joined_players.size() + "/" + size + ")!");
        ArrayList<BWTeam> available_game_teams = getTeams(game);
        for(BWTeam team : getTeams(game)) {
            Collection<String> teams = game2.teams.values();
            for(String team2 : game2.teams.values()) { if(!team2.equals(team.name)) teams.remove(team.name); }
            if(team.size == teams.size()) available_game_teams.remove(team);
        }
        String teams = "";
        boolean check = false;
        for(BWTeam team : available_game_teams) {
            if(check) teams += ", " + team.name;
            else {
                teams += team.name;
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
        String game = getPlayerMap(nick);
        BWGame game2 = getGame(game);
        BWTeam game_team = getTeam(game, team);
        if(game2.started) return ChatColor.RED + "You cannot change your team after the game has started!";
        if(game2.joined_players.contains(nick)) {
            if(game_team != null) {
                int count = 0;
                for(Map.Entry<String, String> team_count : game2.teams.entrySet()) {
                    if(team_count.getKey().equals(team)) count++;
                }
                if(count != game_team.size) {
                    game2.teams.put(nick, team);
                    //leaveTeam(nick);
                    return ChatColor.AQUA + "Successfully joined team \"" + team + "\"!";
                } else return ChatColor.RED + "Team \"" + team + "\" is full!";
            } else return ChatColor.RED + "Given team does not exist!";
        } else return ChatColor.RED + "You are not playing on any map!";
    }

    public static String getPlayerTeam(String nick) {
        return getGame(getPlayerMap(nick)).teams.getOrDefault(nick, null);
    }

    public static String leaveTeam(String nick) {
        String game = getPlayerMap(nick);
        BWGame game2 = getGame(game);
        if(game2.started) return ChatColor.RED + "You cannot change your team after the game has started!";
        if(game2.teams.containsKey(nick)) {
            game2.teams.remove(nick);
            return ChatColor.AQUA + "Successfully left the team!";
        } else return ChatColor.RED + "You don't belong to any team!";
    }

    public static String leaveGame(String nick) {
        if(!isPlayerPlaying(nick)) return ChatColor.RED + "You are not playing!";
        String game = getPlayerMap(nick);
        BWGame game2 = getGame(game);
        //if(!game2.joined_players.contains(nick)) return ChatColor.RED + "You are not playing!";
        Bukkit.getPlayer(nick).getInventory().clear();
        game2.joined_players.remove(nick);
        game2.teams.remove(nick);
        int size = 0;
        for(BWTeam team : getTeams(game)) size += team.size;
        for(String player : game2.joined_players) Bukkit.getPlayer(player).sendMessage(ChatColor.AQUA + "Player " + nick + " has left the map (" + game2.joined_players.size() + "/" + size + ")!");
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
        Map.Entry<ItemStack[], ItemStack[]> player_inventory = game2.player_inventories.get(nick).entrySet().stream().findFirst().get();
        Bukkit.getPlayer(nick).getInventory().setContents(player_inventory.getKey());
        Bukkit.getPlayer(nick).getInventory().setArmorContents(player_inventory.getValue());
        game2.player_inventories.remove(nick);
        stopIfEmpty(game2, game);
        return ChatColor.AQUA + "You've left a map!";
    }

    public static void proceedGame(String game) {
        BWGame game2 = getGame(game);
        if(game2.joined_players.size() != 1) return;
        int size = 0;
        for(BWTeam team : getTeams(game)) size += team.size;
        AtomicBoolean start = new AtomicBoolean(false);
        final AtomicInteger count = new AtomicInteger(10);
        int finalSize = size;
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(new Main(), () -> {
            if(game2.joined_players.size() > finalSize/2) {
                if(count.get() == 0) {
                    if(game2.started) return;
                    for(String player : game2.joined_players) Bukkit.getPlayer(player).sendMessage(ChatColor.AQUA + "Game has been started!");
                    startGame(game);
                    return;
                }
                for(String player : game2.joined_players) Bukkit.getPlayer(player).sendMessage(ChatColor.AQUA + "Game starts in " + count.get() + " seconds!");
                count.set(count.get()-1);
                start.set(true);
            } else if(start.get()) {
                for(String player : game2.joined_players) Bukkit.getPlayer(player).sendMessage(ChatColor.AQUA + "Not enough players, counting has been paused.");
                count.set(10);
                start.set(false);
            }
        }, 0L, 20L);
    }

    public static String forceStart(String nick) {
        String game = getPlayerMap(nick);
        BWGame game2 = getGame(game);
        if(game2 != null) {
            if((game2.joined_players.size() > 1) || (Objects.equals(cfg.get("config", "debug"), "true"))) {
                startGame(game);
                return ChatColor.AQUA + "Game has been started successfully!";
            } else return ChatColor.RED + "You need at least two players to play!";
        } else return ChatColor.RED + "You are not playing on any map!";
    }

    public static void startGame(String game) {
        BWGame game2 = getGame(game);
        ArrayList<BWTeam> map_teams = getTeams(game);
        if(game2.teams.size() != game2.joined_players.size()) {
            Map<String, String> game_teams = new HashMap<>();
            for(BWTeam team : map_teams) game_teams.put(team.name, String.valueOf(team.size)); // I have no idea why this needs size as a string, or basically size at all, see comment just below.
            for(String player : game2.joined_players) {
                if(!game2.teams.containsKey(player)) {
                    for(Map.Entry<String, String> team : game_teams.entrySet()) {
                        int count = 0;
                        for(Map.Entry<String, String> team_count : game2.teams.entrySet()) {
                            if(team_count.getValue().equals(team.getKey())) count++; // Comparing size with team name? Will have to figure this out later, currently only fixing up the code after changing team to a class.
                        }
                        if(count < getTeam(game, team.getKey()).size) {
                            joinTeam(player, team.getKey());
                            break;
                        }
                        /*if((count != Integer.parseInt(team.getValue())) && (game2.joined_players.size() > 2)) {
                            joinTeam(player, team.getKey());
                            break;
                        } else if(count == 0) {
                            joinTeam(player, team.getKey());
                            break;
                        }*/

                    }
                }
            }
        }
        for(BWTeam team : map_teams) {
            game2.beds.put(team.bed1.getBlock().getState().getData(), team.bed1);
            game2.beds.put(team.bed2.getBlock().getState().getData(), team.bed2);
        }
        for(String player : game2.joined_players) Bukkit.getPlayer(player).teleport(getTeam(game, getPlayerTeam(player)).spawn);
        for(BWTeam team : getTeams(game)) createNpc(game, team.villager);
        for(Map.Entry<String, Map<String, Object>> spawner : getSpawners(game).entrySet()) {
            int id = Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(new Main(), () -> {
                BWGame game3 = getGame(game);
                if(game3 == null) return;
                game3.items.add(Bukkit.getWorld(cfg.get(game, "world")).dropItem((Location) spawner.getValue().get("location"), new ItemStack(Material.getMaterial(String.valueOf(spawner.getValue().get("type"))), 1)));
            }, 0L, Integer.parseInt(String.valueOf(spawner.getValue().get("time"))) * 20L);
            game2.task_ids.add(id);
        }
        Block.blocks.put(game, new ArrayList<>());
        game2.started = true;
    }

    public static String endGame(String game) {
        BWGame game2 = getGame(game);
        if(game2 == null) return ChatColor.RED + "The given map is not running!";
        for(Map.Entry<String, ArrayList<Location>> blocks : Block.blocks.entrySet()) {
            if(blocks.getKey().equals(game)) {
                for(Location location : blocks.getValue()) location.getBlock().setType(Material.AIR);
            }
        }
        for(Map.Entry<MaterialData, Location> bed : game2.beds.entrySet()) {
            //Bukkit.getWorld(cfg.get(game, "world")).getBlockAt(bed.getValue()).setType(bed.getKey());
            Bukkit.getWorld(cfg.get(game, "world")).getBlockAt(bed.getValue()).setTypeIdAndData(bed.getKey().getItemTypeId(), bed.getKey().getData(), false);
        }
        ArrayList<String> joined_players_copy = new ArrayList<>(game2.joined_players);
        for(String player : joined_players_copy) leaveGame(player);
        ArrayList<Integer> tasks_copy = new ArrayList<>(game2.task_ids);
        for(Integer data : tasks_copy) {
            Bukkit.getScheduler().cancelTask(data);
            game2.task_ids.remove(data);
        }
        HashMap<Integer, Location> npcs_copy = new HashMap<>(game2.npcs);
        for(Map.Entry<Integer, Location> data : npcs_copy.entrySet()) {
            try {
                for(Entity e : data.getValue().getWorld().getEntities()) {
                    if(e.getEntityId() == data.getKey()) e.remove();
                }
                game2.npcs.remove(data.getKey());
            } catch(Exception ignored) {}
        }
        ArrayList<Item> items_copy = new ArrayList<>(game2.items);
        for(Item item : items_copy) {
            item.remove();
            game2.items.remove(item);
        }
        for(Map.Entry<Location, ItemStack[]> entry : game2.chests.entrySet()) {
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
        game2.reset();
        games.remove(game);
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
            if(data.getValue().joined_players.contains(nick)) return true;
        }
        return false;
    }
    public static String getPlayerMap(String nick) {
        for(Map.Entry<String, BWGame> data : games.entrySet()) {
            if(data.getValue().joined_players.contains(nick)) {
                return data.getKey();
            }
        }
        return "";
    }
    public static boolean isGameNPC(int id) {
        for(Map.Entry<String, BWGame> data : games.entrySet()) {
            if(data.getValue().npcs.containsKey(id)) return true;
        }
        return false;
    }

    public static boolean stopIfEmpty(BWGame game2, String game) {
        if(game2.joined_players.size() > 0) {
            String team = null;
            for (String nick : game2.joined_players) {
                String player_team = getPlayerTeam(nick);
                if(team == null) team = player_team;
                else if(!team.equals(player_team)) return false;
            }
            for(String player : game2.joined_players) Bukkit.getPlayer(player).sendMessage(ChatColor.AQUA + "Team \"" + team + "\" won!");
        }
        endGame(game);
        return true;
    }
}