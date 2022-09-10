package me.screamingbetawars;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import me.screamingbetawars.ConfigManager.*;

import org.bukkit.*;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.ItemStack;

public class Game {
    static HashMap<String, Boolean> games = new HashMap<>();
    public static HashMap<String, String> joined_players = new HashMap<>();
    static HashMap<String, Map<String, String>> teams = new HashMap<>();
    static HashMap<Material, Location> beds = new HashMap<>();
    public static HashMap<Integer, Map.Entry<String, Location>> npcs = new HashMap<>();
    public static HashMap<String, String> destroyed_beds = new HashMap<>();
    public static HashMap<Integer, String> task_ids = new HashMap<>();
    public static class game {
        public static String joinGame(String game, String nick) {
            if (!joined_players.containsKey(nick)) {
                if (!games.containsKey(game)) games.put(game, false);
                if (!teams.containsKey(game)) teams.put(game, new HashMap<>());
                if(games.get(game).equals(false)) joined_players.put(nick, game);
                else return ChatColor.RED + "This game has already started!";
            } else return ChatColor.RED + "You are already in a game!";
            Bukkit.getPlayer(nick).teleport(cfg.getLocation(game, "lobby-"));
            Bukkit.getPlayer(nick).sendMessage(ChatColor.AQUA + "You've successfully joined a map!");
            int size = 0;
            for(Map<String, Object> team : getTeams(game).values()) size += Integer.parseInt(String.valueOf(team.get("size")));
            Map<String, String> list = getWithValue(joined_players, game);
            if(list.size() == 1) proceedGame(game);
            for(String player : list.keySet()) Bukkit.getPlayer(player).sendMessage(ChatColor.AQUA + "Player " + nick + " has joined the map (" + list.size() + "/" + size + ")!");
            Map<String, Map<String, Object>> game_teams = new HashMap<>(getTeams(game));
            for (Map.Entry<String, Map<String, Object>> entry : getTeams(game).entrySet()) {
                if(Integer.parseInt((String) entry.getValue().get("size")) == countWithValue(joined_players, entry.getKey())) game_teams.remove(entry.getKey(), entry.getValue());
            }
            String teams = "";
            boolean check = false;
            for (String entry : game_teams.keySet()) {
                if(check) teams += ", " + entry;
                else {
                    teams += entry;
                    check = true;
                }
            }
            return ChatColor.GOLD + "Please pick a team using /bw pick <team>. Available teams: " + teams + ".";
        }

        public static Map<String, Map<String, Object>> getTeams(String game) {
            Map<String, Map<String, Object>> team_list = new HashMap<>();
            Map<String, Object> teams = cfg.getStartingWith(game, "team-");
            for (Map.Entry<String, Object> team_data : teams.entrySet()) {
                Map<String, Object> current_team = new HashMap<>();
                String team = team_data.getKey().replace("team-", "");
                Object color = team_data.getValue();
                current_team.put("color", color);
                current_team.put("size", cfg.get(game, "size-team-" + team));
                Location bed1 = new Location(Bukkit.getServer().getWorld(cfg.get(game, "world")), cfg.getInt(game, "x1-bed-team-" + team), cfg.getInt(game, "y1-bed-team-" + team), cfg.getInt(game, "z1-bed-team-" + team));
                Location bed2 = new Location(Bukkit.getServer().getWorld(cfg.get(game, "world")), cfg.getInt(game, "x2-bed-team-" + team), cfg.getInt(game, "y2-bed-team-" + team), cfg.getInt(game, "z2-bed-team-" + team));
                Location villager = new Location(Bukkit.getServer().getWorld(cfg.get(game, "world")), cfg.getInt(game, "villager-x-team-" + team), cfg.getInt(game, "villager-y-team-" + team), cfg.getInt(game, "villager-z-team-" + team), Float.parseFloat(cfg.get(game, "villager-yaw-team-" + team)), Float.parseFloat(cfg.get(game, "villager-pitch-team-" + team)));
                Location spawn = new Location(Bukkit.getServer().getWorld(cfg.get(game, "world")), cfg.getInt(game, "x-spawn-team-" + team), cfg.getInt(game, "y-spawn-team-" + team), cfg.getInt(game, "z-spawn-team-" + team));
                current_team.put("bed1", bed1);
                current_team.put("bed2", bed2);
                current_team.put("villager", villager);
                current_team.put("spawn", spawn);
                team_list.put(team, current_team);
            }
            return team_list;
        }

        public static Map<String, String> getWithKey(Map<String, String> data, String key) {
            Map<String, String> data2 = data;
            for(String value : data2.keySet()) {
                if(!value.startsWith(key)) data2.remove(value);
            }
            return data2;
        }

        public static Map<String, String> getWithValue(Map<String, String> data, String value) {
            Map<String, String> data2 = data;
            for(Map.Entry<String, String> data_value : data2.entrySet()) {
                if(!data_value.getValue().startsWith(value)) data2.remove(data_value.getKey());
            }
            return data2;
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
            for (Object spawner2 : spawners.keySet()) {
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
            if (joined_players.containsKey(nick)) {
                String game = joined_players.get(nick);
                if (getTeams(game).containsKey(team)) {
                    int count = 0;
                    for (Map.Entry<String, String> team_count : teams.get(game).entrySet()) {
                        if (team_count.getKey().equals(team)) count++;
                    }
                    if (count != Integer.parseInt((String) getTeams(game).get(team).get("size"))) {
                        Map<String, String> data = new HashMap<>(teams.get(game));
                        data.put(nick, team);
                        leaveTeam(nick);
                        teams.put(game, data);
                        return ChatColor.AQUA + "Successfully joined team \"" + team + "\"!";
                    } else return ChatColor.RED + "Team \"" + team + "\" is full!";
                } else return ChatColor.RED + "Given team does not exist!";
            } else return ChatColor.RED + "You are not playing on any map!";
        }

        public static String getPlayerTeam(String game, String nick) {
            return teams.get(game).getOrDefault(nick, null);
        }

        public static String leaveTeam(String nick) {
            String game = joined_players.get(nick);
            if(teams.get(game).containsKey(nick)) {
                teams.get(game).remove(nick);
                return ChatColor.AQUA + "Successfully left the team!";
            } else return ChatColor.RED + "You don't belong to any team!";
        }

        public static String leaveGame(String nick) {
            if (!joined_players.containsKey(nick)) return ChatColor.RED + "You are not playing!";
            String game = joined_players.get(nick);
            joined_players.remove(nick);
            teams.get(game).remove(nick);
            int size = 0;
            for(Map<String, Object> team : getTeams(game).values()) size += Integer.parseInt(String.valueOf(team.get("size")));
            Map<String, String> list = getWithValue(joined_players, game);
            for(String player : list.keySet()) Bukkit.getPlayer(player).sendMessage(ChatColor.AQUA + "Player " + nick + " has left the map (" + list.size() + "/" + size + ")!");
            String random_team = "";
            if(getWithValue(joined_players, game).size() > 0) {
                for (Map.Entry<String, String> team : teams.get(game).entrySet()) {
                    random_team = team.getKey();
                    break;
                }
                if (getWithValue(joined_players, game).size() == getWithValue(teams.get(game), random_team).size()) {
                    Map<String, String> list2 = getWithValue(joined_players, game);
                    for (String player : list2.keySet()) {
                        Bukkit.getPlayer(player).sendMessage(ChatColor.AQUA + "Team " + random_team + " has won!");
                        endGame(game);
                    }
                }
            }
            if(cfg.getLocation("config", "spawn-") != null) {
                Location spawn = cfg.getLocation("config", "spawn-");
                spawn.setYaw(Float.valueOf(cfg.get("config", "spawn-yaw")));
                spawn.setPitch(Float.valueOf(cfg.get("config", "spawn-pitch")));
                Bukkit.getPlayer(nick).teleport(spawn);
            } else Bukkit.getPlayer(nick).teleport(Bukkit.getPlayer(nick).getWorld().getSpawnLocation());
            return ChatColor.AQUA + "You've left a map!";
        }

        public static void proceedGame(String game) {
            int size = 0;
            for(Map<String, Object> team : getTeams(game).values()) size += Integer.valueOf((String) team.get("size"));
            AtomicBoolean start = new AtomicBoolean(false);
            final AtomicInteger count = new AtomicInteger(10);
            int finalSize = size;
            Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(new Main(), new Runnable() {
                public void run() {
                    Map<String, String> list = getWithValue(joined_players, game);
                    if(list.size() > finalSize/2) {
                        if(count.get() == 0) {
                            for(String player : list.keySet()) Bukkit.getPlayer(player).sendMessage(ChatColor.AQUA + "Game has been started!");
                            startGame(game);
                            return;
                        }
                        for(String player : list.keySet()) Bukkit.getPlayer(player).sendMessage(ChatColor.AQUA + "Game starts in " + count.get() + " seconds!");
                        count.set(count.get()-1);
                        start.set(true);
                    } else if(start.get()) {
                        for(String player : list.keySet()) Bukkit.getPlayer(player).sendMessage(ChatColor.AQUA + "Not enough players, counting has been paused.");
                        count.set(10);
                        start.set(false);
                    }
                }
            }, 0L, 20L);
        }

        public static String forceStart(String nick) {
            String game = joined_players.get(nick);
            if(game != null) {
                if((getWithValue(joined_players, game).size() > 1) || (Objects.equals(cfg.get("config", "debug"), "true"))) {
                    startGame(game);
                    return ChatColor.AQUA + "Game has been started successfully!";
                } else return ChatColor.RED + "You need at least two players to play!";
            } else return ChatColor.RED + "You are not playing on any map!";
        }

        public static void startGame(String game) {
            games.replace(game, true);
            Map<String, Map<String, Object>> map_teams = getTeams(game);
            if(teams.get(game).size() != getWithValue(joined_players, game).size()) {
                Map<String, String> game_teams = new HashMap<>();
                for (Map.Entry<String, Map<String, Object>> entry : map_teams.entrySet()) {
                    game_teams.put(entry.getKey(), String.valueOf(entry.getValue().get("size")));
                }
                for(String player : getWithValue(joined_players, game).keySet()) {
                    if(!teams.get(game).containsKey(player)) {
                        for(Map.Entry<String, String> team : game_teams.entrySet()) {
                            int count = 0;
                            for(Map.Entry<String, String> team_count : teams.get(game).entrySet()) {
                                if(team_count.getKey().equals(team.getKey())) count++;
                            }
                            if((count != Integer.parseInt(team.getValue()))&&(getWithValue(joined_players, game).size()>2)) {
                                joinTeam(player, team.getKey());
                                break;
                            } else if(count == 0) {
                                joinTeam(player, team.getKey());
                                break;
                            }
                        }
                    }
                }
            }
            for(Map<String, Object> entry : getTeams(game).values()) {
                beds.put(((Location) entry.get("bed1")).getBlock().getType(), (Location) entry.get("bed1"));
                beds.put(((Location) entry.get("bed2")).getBlock().getType(), (Location) entry.get("bed2"));
            }
            for(String player : getWithValue(joined_players, game).keySet()) {
                Bukkit.getPlayer(player).teleport((Location) getTeams(game).get(getPlayerTeam(game, player)).get("spawn"));
            }
            for(Map<String, Object> shop : getTeams(game).values()) createNpc(game, (Location) shop.get("villager"));
            for(Map.Entry<String, Map<String, Object>> spawner : getSpawners(game).entrySet()) {
                int id = Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(new Main(), new Runnable() {
                    public void run() {
                        if(!games.get(game)) return;
                        Bukkit.getWorld(cfg.get(game, "world")).dropItem((Location) spawner.getValue().get("location"), new ItemStack(Material.getMaterial(String.valueOf(spawner.getValue().get("type"))), 1));
                    }
                }, 0L, Integer.parseInt(String.valueOf(spawner.getValue().get("time"))) * 20L);
                task_ids.put(id, game);
            }
        }

        public static String endGame(String game) {
            for(Map.Entry<String, Location> blocks : Block.blocks.entrySet()) {
                if(blocks.getKey().equals(game)) blocks.getValue().getBlock().setType(Material.AIR);
            }
            for(Map.Entry<Material, Location> bed : beds.entrySet()) {
                Bukkit.getWorld(cfg.get(game, "world")).getBlockAt(bed.getValue()).setType(bed.getKey());
            }
            //for(Map.Entry<Integer, String> npc : npcs.entrySet()) ;
            Map<String, String> list = getWithValue(joined_players, game);
            for(String player : list.keySet()) leaveGame(player);
            HashMap<Integer, String> tasks_copy = new HashMap<>(task_ids);
            for(Map.Entry<Integer, String> data : tasks_copy.entrySet()) {
                Bukkit.getServer().broadcastMessage(data.getKey() + " " + data.getValue() + " " + task_ids.size());
                if(data.getValue().equals(game)) {
                    Bukkit.getScheduler().cancelTask(data.getKey());
                    task_ids.remove(data.getKey());
                }
            }
            HashMap<Integer, Map.Entry<String, Location>> npcs_copy = new HashMap<>(npcs);
            for(Map.Entry<Integer, Map.Entry<String, Location>> data : npcs_copy.entrySet()) {
                try {
                    if(data.getValue().getKey().equals(game)) data.getValue().getValue().getWorld().getEntities().get(data.getKey()).remove();
                    npcs.remove(data.getKey());
                } catch(Exception ignored) {}
            }
            games.remove(game);
            return ChatColor.AQUA + "Map has been stopped successfully.";
        }

        public static void createNpc(String game, Location location) {
            Sheep npc = (Sheep) Bukkit.getWorld(cfg.get(game, "world")).spawnCreature(location, CreatureType.SHEEP);
            npcs.put(npc.getEntityId(), new AbstractMap.SimpleEntry<>(game, location));
            npc.setColor(DyeColor.GRAY);
            int id = Bukkit.getScheduler().scheduleAsyncRepeatingTask(Main.instance, new Runnable() {
                @Override
                public void run() {
                    npc.teleport(location);
                }
            }, 0L, 1L);
            task_ids.put(id, game);
            Bukkit.getServer().broadcastMessage(game + " " + id + " " + task_ids.size());
        }
    }
}
