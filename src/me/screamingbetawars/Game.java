package me.screamingbetawars;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import me.screamingbetawars.ConfigManager.*;

import org.bukkit.*;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.ItemStack;

public class Game {
    static class BWGame {
        public static boolean started = false;
        public static ArrayList<String> joined_players = new ArrayList<>();
        static HashMap<String, String> teams = new HashMap<>();
        static HashMap<Material, Location> beds = new HashMap<>();
        public static HashMap<Integer, Location> npcs = new HashMap<>();
        public static ArrayList<String> destroyed_beds = new ArrayList<>();
        public static ArrayList<Integer> task_ids = new ArrayList<>();
    }
    static HashMap<String, BWGame> games = new HashMap<>();
    public static class game {
        public static BWGame getGame(String game) {
            if(games.get(game) == null) games.put(game, new BWGame());
            return games.get(game);
        }
        public static String joinGame(String game, String nick) {
            BWGame game2 = getGame(game);
            if (!game2.joined_players.contains(nick)) {
                if(!game2.started) game2.joined_players.add(nick);
                else return ChatColor.RED + "This game has already started!";
            } else return ChatColor.RED + "You are already in a game!";
            Bukkit.getPlayer(nick).teleport(cfg.getLocation(game, "lobby-"));
            Bukkit.getPlayer(nick).sendMessage(ChatColor.AQUA + "You've successfully joined a map!");
            int size = 0;
            for(Map<String, Object> team : getTeams(game).values()) size += Integer.parseInt(String.valueOf(team.get("size")));
            if(game2.joined_players.size() == 1) proceedGame(game);
            for(String player : game2.joined_players) Bukkit.getPlayer(player).sendMessage(ChatColor.AQUA + "Player " + nick + " has joined the map (" + game2.joined_players.size() + "/" + size + ")!");
            Map<String, Map<String, Object>> game_teams = new HashMap<>(getTeams(game));
            for (Map.Entry<String, Map<String, Object>> entry : getTeams(game).entrySet()) {
                if(Integer.parseInt((String) entry.getValue().get("size")) == game2.joined_players.size()) game_teams.remove(entry.getKey(), entry.getValue());
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
            String game = getPlayerMap(nick);
            BWGame game2 = getGame(game);
            if(game2.joined_players.contains(nick)) {
                if (getTeams(game).containsKey(team)) {
                    int count = 0;
                    for (Map.Entry<String, String> team_count : game2.teams.entrySet()) {
                        if (team_count.getKey().equals(team)) count++;
                    }
                    if (count != Integer.parseInt((String) getTeams(game).get(team).get("size"))) {
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
            if(game2.teams.containsKey(nick)) {
                game2.teams.remove(nick);
                return ChatColor.AQUA + "Successfully left the team!";
            } else return ChatColor.RED + "You don't belong to any team!";
        }

        public static String leaveGame(String nick) {
            String game = getPlayerMap(nick);
            BWGame game2 = getGame(game);
            if (!game2.joined_players.contains(nick)) return ChatColor.RED + "You are not playing!";
            game2.joined_players.remove(nick);
            game2.teams.remove(nick);
            int size = 0;
            for(Map<String, Object> team : getTeams(game).values()) size += Integer.parseInt(String.valueOf(team.get("size")));
            for(String player : game2.joined_players) Bukkit.getPlayer(player).sendMessage(ChatColor.AQUA + "Player " + nick + " has left the map (" + game2.joined_players.size() + "/" + size + ")!");
            String random_team = "";
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
            BWGame game2 = getGame(game);
            int size = 0;
            for(Map<String, Object> team : getTeams(game).values()) size += Integer.valueOf((String) team.get("size"));
            AtomicBoolean start = new AtomicBoolean(false);
            final AtomicInteger count = new AtomicInteger(10);
            int finalSize = size;
            Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(new Main(), new Runnable() {
                public void run() {
                    if(game2.joined_players.size() > finalSize/2) {
                        if(count.get() == 0) {
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
            game2.started = true;
            Map<String, Map<String, Object>> map_teams = getTeams(game);
            if(game2.teams.size() != game2.joined_players.size()) {
                Map<String, String> game_teams = new HashMap<>();
                for(Map.Entry<String, Map<String, Object>> entry : map_teams.entrySet()) {
                    game_teams.put(entry.getKey(), String.valueOf(entry.getValue().get("size")));
                }
                for(String player : game2.joined_players) {
                    if(!game2.teams.containsKey(player)) {
                        Bukkit.getLogger().info("test");
                        for(Map.Entry<String, String> team : game_teams.entrySet()) {
                            int count = 0;
                            for(Map.Entry<String, String> team_count : game2.teams.entrySet()) {
                                if(team_count.getKey().equals(team.getKey())) count++;
                            }
                            if((count != Integer.parseInt(team.getValue())) && (game2.joined_players.size() > 2)) {
                                joinTeam(player, team.getKey());
                                break;
                            } else if(count == 0) {
                                joinTeam(player, team.getKey());
                                break;
                            }
                        }
                    }
                }
                Bukkit.getLogger().info(getPlayerTeam("Lort533"));
            }
            for(Map<String, Object> entry : getTeams(game).values()) {
                game2.beds.put(((Location) entry.get("bed1")).getBlock().getType(), (Location) entry.get("bed1"));
                game2.beds.put(((Location) entry.get("bed2")).getBlock().getType(), (Location) entry.get("bed2"));
            }
            for(String player : game2.joined_players) {
                Bukkit.getPlayer(player).teleport((Location) getTeams(game).get(getPlayerTeam(player)).get("spawn"));
            }
            for(Map<String, Object> shop : getTeams(game).values()) createNpc(game, (Location) shop.get("villager"));
            for(Map.Entry<String, Map<String, Object>> spawner : getSpawners(game).entrySet()) {
                int id = Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(new Main(), new Runnable() {
                    public void run() {
                        if(getGame(game) == null) return;
                        Bukkit.getWorld(cfg.get(game, "world")).dropItem((Location) spawner.getValue().get("location"), new ItemStack(Material.getMaterial(String.valueOf(spawner.getValue().get("type"))), 1));
                    }
                }, 0L, Integer.parseInt(String.valueOf(spawner.getValue().get("time"))) * 20L);
                game2.task_ids.add(id);
            }
        }

        public static String endGame(String game) {
            BWGame game2 = getGame(game);
            if(game2 == null) return ChatColor.RED + "The given map is not running!";
            for(Map.Entry<String, Location> blocks : Block.blocks.entrySet()) {
                if(blocks.getKey().equals(game)) blocks.getValue().getBlock().setType(Material.AIR);
            }
            for(Map.Entry<Material, Location> bed : game2.beds.entrySet()) {
                Bukkit.getWorld(cfg.get(game, "world")).getBlockAt(bed.getValue()).setType(bed.getKey());
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
            game2.destroyed_beds.clear();
            game2.started = false;
            games.remove(game);
            return ChatColor.AQUA + "Map has been stopped successfully.";
        }

        public static void createNpc(String game, Location location) {
            BWGame game2 = getGame(game);
            Sheep npc = (Sheep) Bukkit.getWorld(cfg.get(game, "world")).spawnCreature(location, CreatureType.SHEEP);
            game2.npcs.put(npc.getEntityId(), location);
            npc.setColor(DyeColor.GRAY);
            int id = Bukkit.getScheduler().scheduleAsyncRepeatingTask(Main.instance, new Runnable() {
                @Override
                public void run() {
                    npc.teleport(location);
                }
            }, 0L, 1L);
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
                if(data.getValue().joined_players.contains(nick)) return data.getKey();
            }
            return null;
        }
        public static boolean isGameNPC(int id) {
            for(Map.Entry<String, BWGame> data : games.entrySet()) {
                if(data.getValue().npcs.containsKey(id)) return true;
            }
            return false;
        }
    }
}
