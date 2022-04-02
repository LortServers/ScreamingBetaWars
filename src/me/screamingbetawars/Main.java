package me.screamingbetawars;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import me.screamingbetawars.ConfigManager.*;
import me.screamingbetawars.Game.*;

import java.io.File;
import java.io.IOException;

public class Main extends JavaPlugin implements CommandExecutor, Listener {
    public static String version = "b1.7.3-0.2-dev";
    @Override
    public void onEnable() {
        Bukkit.getLogger().info("ScreamingBetaWars is starting...");
        Bukkit.getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, new Block(), Event.Priority.Highest, this);
        Bukkit.getPluginManager().registerEvent(Event.Type.BLOCK_PLACE, new Block(), Event.Priority.Highest, this);
        Bukkit.getPluginManager().registerEvent(Event.Type.BLOCK_BURN, new Block(), Event.Priority.Highest, this);
        Bukkit.getPluginManager().registerEvent(Event.Type.ENTITY_DEATH, new Death(), Event.Priority.Highest, this);
        Bukkit.getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, new me.screamingbetawars.Player(), Event.Priority.Highest, this);
        Bukkit.getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT_ENTITY, new me.screamingbetawars.Player(), Event.Priority.Highest, this);
        Bukkit.getPluginManager().registerEvent(Event.Type.PLAYER_RESPAWN, new me.screamingbetawars.Player(), Event.Priority.Highest, this);
        this.getCommand("bw").setExecutor(this);
        File file = new File(Bukkit.getServer().getPluginManager().getPlugin("ScreamingBetaWars").getDataFolder(), "config.yml");
        if(cfg.check("config", true).equals("true")) {
            try {
                Bukkit.getServer().getPluginManager().getPlugin("ScreamingBetaWars").getDataFolder().mkdir();
                file.createNewFile();
            } catch (IOException ignored) {}
        }
        if(cfg.get("config", "version") == null) {
            cfg.put("config", "version", version);
            cfg.put("config", "respawn-time", "5");
            cfg.put("config", "debug", "false");
        }
    }
    @Override
    public void onDisable() {
        Bukkit.getLogger().info("ScreamingBetaWars is shutting down...");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = sender.getServer().getPlayer(sender.getName());
        if(args.length == 0) {
            sender.sendMessage(ChatColor.AQUA + "ScreamingBetaWars " + version);
            sender.sendMessage(ChatColor.GOLD + "Experience true mini-games, our newest invention!");
            sender.sendMessage(ChatColor.GOLD + "We will keep on updating this plug-in.");
            sender.sendMessage(ChatColor.RED + "Type /bw help to get started.");
        } else if(args[0].equals("help")) {
            sender.sendMessage(ChatColor.AQUA + "ScreamingBetaWars " + version);
            sender.sendMessage(ChatColor.GOLD + "Experience true mini-games, our newest invention!");
            sender.sendMessage(ChatColor.GOLD + "Our list of commands:");
            sender.sendMessage(ChatColor.RED + "/bw help - brings up this command.");
            sender.sendMessage(ChatColor.RED + "/bw map - everything about maps.");
            sender.sendMessage(ChatColor.RED + "/bw setspawn - sets the spawn.");
            sender.sendMessage(ChatColor.RED + "/bw join <name> - makes you join the map.");
            sender.sendMessage(ChatColor.RED + "/bw leave - makes you leave the map.");
            sender.sendMessage(ChatColor.RED + "/bw start - makes you force start the map.");
            sender.sendMessage(ChatColor.RED + "/bw stop <name> - makes you force stop the map.");
            sender.sendMessage(ChatColor.RED + "/bw pick <name> - makes you pick the team after joining a game.");
        } else if(args[0].equals("map")) {
            if(!shortcuts.permissions(sender)) return true;
            if(args.length == 1) {
                sender.sendMessage(ChatColor.AQUA + "ScreamingBetaWars " + version);
                sender.sendMessage(ChatColor.GOLD + "Everything about maps.");
                sender.sendMessage(ChatColor.RED + "/bw map <name> create - creates the map.");
                sender.sendMessage(ChatColor.RED + "/bw map <name> edit - edits the map.");
                sender.sendMessage(ChatColor.RED + "/bw map <name> save - saves the map.");
                sender.sendMessage(ChatColor.RED + "/bw map <name> pos1 - sets first position of the map.");
                sender.sendMessage(ChatColor.RED + "/bw map <name> pos2 - sets second position of the map.");
                sender.sendMessage(ChatColor.RED + "/bw map <name> lobby1 - sets first position of the lobby.");
                sender.sendMessage(ChatColor.RED + "/bw map <name> lobby2 - sets second position of the lobby.");
                sender.sendMessage(ChatColor.RED + "/bw map <name> lobby - sets spawn position of the lobby.");
                sender.sendMessage(ChatColor.RED + "/bw map <name> spawner <item> <seconds> - sets a spawner.");
                sender.sendMessage(ChatColor.RED + "/bw map <name> spec - sets the position of a spectator.");
                sender.sendMessage(ChatColor.RED + "/bw map <name> team add <name> <color> <amount_of_players> - adds the team.");
                sender.sendMessage(ChatColor.RED + "/bw map <name> team remove <name> - removes the team.");
                sender.sendMessage(ChatColor.RED + "/bw map <name> team spawn <name> - sets the spawn of the team.");
                sender.sendMessage(ChatColor.RED + "/bw map <name> team bed1 <name> - sets the first position of the teams bed (stand on the red side).");
                sender.sendMessage(ChatColor.RED + "/bw map <name> team bed2 <name> - sets the second position of the teams bed (stand on the pillow side).");
                sender.sendMessage(ChatColor.RED + "/bw map <name> team shop <name> - sets the teams shop.");
            } else if(args.length == 2) shortcuts.syntax(sender);
            else {
                if(args[2].equals("create")) sender.sendMessage(cfg.create(args[1], sender));
                else if(args[2].equals("edit")) {
                    if(cfg.check(args[1], true, false).equals("true")) {
                        cfg.put(args[1], "edit", "true");
                        sender.sendMessage(ChatColor.AQUA + "Map status has been set to edit state.");
                    } else sender.sendMessage(ChatColor.RED + "Map is already in edit state!");
                } else if(args[2].equals("save")) {
                    if(cfg.check(args[1], true, true).equals("true")) {
                        cfg.put(args[1], "edit", "false");
                        sender.sendMessage(ChatColor.AQUA + "Map has been saved.");
                    } else sender.sendMessage(ChatColor.RED + cfg.check(args[1], true, true));
                } else if(args[2].equals("pos1")) {
                    if(cfg.check(args[1], true, true).equals("true")) {
                        cfg.putInt(args[1], "pos1x", player.getLocation().getBlockX());
                        cfg.putInt(args[1], "pos1y", player.getLocation().getBlockY());
                        cfg.putInt(args[1], "pos1z", player.getLocation().getBlockZ());
                        sender.sendMessage(ChatColor.AQUA + "First position has been set.");
                    } else sender.sendMessage(ChatColor.RED + cfg.check(args[1], true, true));
                } else if(args[2].equals("pos2")) {
                    if(cfg.check(args[1], true, true).equals("true")) {
                        cfg.putInt(args[1], "pos2x", player.getLocation().getBlockX());
                        cfg.putInt(args[1], "pos2y", player.getLocation().getBlockY());
                        cfg.putInt(args[1], "pos2z", player.getLocation().getBlockZ());
                        sender.sendMessage(ChatColor.AQUA + "Second position has been set.");
                    } else sender.sendMessage(ChatColor.RED + cfg.check(args[1], true, true));
                } else if(args[2].equals("lobby1")) {
                    if(cfg.check(args[1], true, true).equals("true")) {
                        cfg.putInt(args[1], "lobby1x", player.getLocation().getBlockX());
                        cfg.putInt(args[1], "lobby1y", player.getLocation().getBlockY());
                        cfg.putInt(args[1], "lobby1z", player.getLocation().getBlockZ());
                        sender.sendMessage(ChatColor.AQUA + "First position has been set.");
                    } else sender.sendMessage(ChatColor.RED + cfg.check(args[1], true, true));
                } else if(args[2].equals("lobby2")) {
                    if(cfg.check(args[1], true, true).equals("true")) {
                        cfg.putInt(args[1], "lobby2x", player.getLocation().getBlockX());
                        cfg.putInt(args[1], "lobby2y", player.getLocation().getBlockY());
                        cfg.putInt(args[1], "lobby2z", player.getLocation().getBlockZ());
                        sender.sendMessage(ChatColor.AQUA + "Second position has been set.");
                    } else sender.sendMessage(ChatColor.RED + cfg.check(args[1], true, true));
                } else if(args[2].equals("lobby")) {
                    if(cfg.check(args[1], true, true).equals("true")) {
                        cfg.putInt(args[1], "lobby-x", player.getLocation().getBlockX());
                        cfg.putInt(args[1], "lobby-y", player.getLocation().getBlockY());
                        cfg.putInt(args[1], "lobby-z", player.getLocation().getBlockZ());
                        sender.sendMessage(ChatColor.AQUA + "Lobby spawn has been set.");
                    } else sender.sendMessage(ChatColor.RED + cfg.check(args[1], true, true));
                } else if(args[2].equals("spec")) {
                    if(cfg.check(args[1], true, true).equals("true")) {
                        cfg.putInt(args[1], "spec-x", player.getLocation().getBlockX());
                        cfg.putInt(args[1], "spec-y", player.getLocation().getBlockY());
                        cfg.putInt(args[1], "spec-z", player.getLocation().getBlockZ());
                        sender.sendMessage(ChatColor.AQUA + "Spectator spawn has been set.");
                    } else sender.sendMessage(ChatColor.RED + cfg.check(args[1], true, true));
                } else if(args[2].equals("team")) {
                    if(args.length >= 5) {
                        if(args[3].equals("add")) {
                            if(args.length == 7) {
                                if(cfg.check(args[1], true, true).equals("true")) {
                                    try {
                                        String test = "" + ChatColor.valueOf(args[5].toUpperCase());
                                    } catch (IllegalArgumentException e) {
                                        shortcuts.custom(sender, "Incorrect team color!");
                                        return false;
                                    }
                                    try {
                                        int test = Integer.parseInt(args[6]);
                                    } catch (NumberFormatException e) {
                                        shortcuts.custom(sender, "Incorrect team size!");
                                        return false;
                                    }
                                    if (!cfg.find(args[1], "team-" + args[4])) {
                                        cfg.put(args[1], "team-" + args[4], args[5].toUpperCase());
                                        cfg.put(args[1], "size-team-" + args[4], args[6]);
                                        sender.sendMessage(ChatColor.AQUA + "Team \"" + ChatColor.valueOf(args[5].toUpperCase()) + args[4] + ChatColor.AQUA + "\" has been added.");
                                    } else sender.sendMessage(ChatColor.AQUA + "Team already exists.");
                                }
                            } else shortcuts.syntax(sender);
                        } else if(args[3].equals("remove")) {
                            if (cfg.find(args[1], "team-" + args[4])) {
                                cfg.remove(args[1], "team-" + args[4]);
                                cfg.remove(args[1], "size-team-" + args[4]);
                                cfg.remove(args[1], "x1-bed-team-" + args[4]);
                                cfg.remove(args[1], "y1-bed-team-" + args[4]);
                                cfg.remove(args[1], "z1-bed-team-" + args[4]);
                                cfg.remove(args[1], "x2-bed-team-" + args[4]);
                                cfg.remove(args[1], "y2-bed-team-" + args[4]);
                                cfg.remove(args[1], "z2-bed-team-" + args[4]);
                                cfg.remove(args[1], "villager-x-team-" + args[4]);
                                cfg.remove(args[1], "villager-y-team-" + args[4]);
                                cfg.remove(args[1], "villager-z-team-" + args[4]);
                                cfg.remove(args[1], "villager-yaw-team-" + args[4]);
                                cfg.remove(args[1], "villager-pitch-team-" + args[4]);
                                sender.sendMessage(ChatColor.AQUA + "Team \"" + args[4] + "\" has been removed.");
                            } else sender.sendMessage(ChatColor.RED + "Team does not exist.");
                        } else if(args[3].equals("bed1")) {
                            if (cfg.find(args[1], "team-" + args[4])) {
                                cfg.putInt(args[1], "x1-bed-team-" + args[4], player.getLocation().getBlockX());
                                cfg.putInt(args[1], "y1-bed-team-" + args[4], player.getLocation().getBlockY());
                                cfg.putInt(args[1], "z1-bed-team-" + args[4], player.getLocation().getBlockZ());
                                sender.sendMessage(ChatColor.AQUA + "First bed position for team \"" + args[4] + "\" is set.");
                            } else sender.sendMessage(ChatColor.RED + "Team does not exist.");
                        } else if(args[3].equals("bed2")) {
                            if (cfg.find(args[1], "team-" + args[4])) {
                                cfg.putInt(args[1], "x2-bed-team-" + args[4], player.getLocation().getBlockX());
                                cfg.putInt(args[1], "y2-bed-team-" + args[4], player.getLocation().getBlockY());
                                cfg.putInt(args[1], "z2-bed-team-" + args[4], player.getLocation().getBlockZ());
                                sender.sendMessage(ChatColor.AQUA + "Second bed position for team \"" + args[4] + "\" is set.");
                            } else sender.sendMessage(ChatColor.RED + "Team does not exist.");
                        } else if(args[3].equals("spawn")) {
                            if (cfg.find(args[1], "team-" + args[4])) {
                                cfg.putInt(args[1], "x-spawn-team-" + args[4], player.getLocation().getBlockX());
                                cfg.putInt(args[1], "y-spawn-team-" + args[4], player.getLocation().getBlockY());
                                cfg.putInt(args[1], "z-spawn-team-" + args[4], player.getLocation().getBlockZ());
                                sender.sendMessage(ChatColor.AQUA + "Spawn for team \"" + args[4] + "\" is set.");
                            } else sender.sendMessage(ChatColor.RED + "Team does not exist.");
                        } else if(args[3].equals("shop")) {
                            if (cfg.find(args[1], "team-" + args[4])) {
                                cfg.putInt(args[1], "villager-x-team-" + args[4], player.getLocation().getBlockX());
                                cfg.putInt(args[1], "villager-y-team-" + args[4], player.getLocation().getBlockY());
                                cfg.putInt(args[1], "villager-z-team-" + args[4], player.getLocation().getBlockZ());
                                cfg.put(args[1], "villager-yaw-team-" + args[4], String.valueOf(player.getLocation().getYaw()));
                                cfg.put(args[1], "villager-pitch-team-" + args[4], String.valueOf(player.getLocation().getPitch()));
                                sender.sendMessage(ChatColor.AQUA + "Shop position for team \"" + args[4] + "\" is set.");
                            } else sender.sendMessage(ChatColor.RED + "Team does not exist.");
                        } else shortcuts.syntax(sender);
                    } else shortcuts.syntax(sender);
                } else if(args[2].equals("spawner")) {
                    if(args.length == 5) {
                        if (cfg.check(args[1], true, true).equals("true")) {
                            try {
                                Material test = Material.getMaterial(args[3].toUpperCase());
                            } catch (IllegalArgumentException e) {
                                shortcuts.custom(sender, "Incorrect item type!");
                                return false;
                            }
                            try {
                                int test = Integer.parseInt(args[4]);
                            } catch (NumberFormatException e) {
                                shortcuts.custom(sender, "Incorrect amount of seconds!");
                                return false;
                            }
                            int spawner = cfg.getInt(args[1], "spawners");
                            cfg.putInt(args[1], "spawner-x-" + spawner, player.getLocation().getBlockX());
                            cfg.putInt(args[1], "spawner-y-" + spawner, player.getLocation().getBlockY());
                            cfg.putInt(args[1], "spawner-z-" + spawner, player.getLocation().getBlockZ());
                            cfg.put(args[1], "spawner-type-" + spawner, args[3].toUpperCase());
                            cfg.put(args[1], "spawner-time-" + spawner, args[4]);
                            cfg.putInt(args[1], "spawners", spawner + 1);
                            sender.sendMessage(ChatColor.AQUA + "Spawner has been set.");
                        } else sender.sendMessage(ChatColor.RED + cfg.check(args[1], true, true));
                    } else shortcuts.syntax(sender);
                } else shortcuts.syntax(sender);
            }
        } else if(args[0].equals("join")) {
            if(args.length == 2) {
                if (cfg.check(args[1], true, false).equals("true")) sender.sendMessage(game.joinGame(args[1], player.getName()));
                else if (cfg.check(args[1], true, false).equals(ChatColor.RED + "Map not found.")) sender.sendMessage(ChatColor.RED + "Map not found.");
                else sender.sendMessage(ChatColor.RED + "Technical difficulties, sorry!");
            } else shortcuts.syntax(sender);
        } else if(args[0].equals("leave")) sender.sendMessage(game.leaveGame(player.getName()));
        else if(args[0].equals("start")) {
            if(!shortcuts.permissions(sender)) return true;
            sender.sendMessage(game.forceStart(sender.getName()));
        }
        else if(args[0].equals("setspawn")) {
            if(!shortcuts.permissions(sender)) return true;
            cfg.putInt("config", "spawn-x", player.getLocation().getBlockX());
            cfg.putInt("config", "spawn-y", player.getLocation().getBlockY());
            cfg.putInt("config", "spawn-z", player.getLocation().getBlockZ());
            cfg.put("config", "spawn-yaw", String.valueOf(player.getLocation().getYaw()));
            cfg.put("config", "spawn-pitch", String.valueOf(player.getLocation().getPitch()));
            sender.sendMessage(ChatColor.AQUA + "Spawn point has been set successfully!");
        } else if(args[0].equals("stop")) {
            if(!shortcuts.permissions(sender)) return true;
            if(args.length == 2) sender.sendMessage(Game.game.endGame(args[1]));
            else shortcuts.syntax(sender);
        } else if(args[0].equals("pick")) {
            if(args.length == 2) sender.sendMessage(Game.game.joinTeam(sender.getName(), args[1]));
            else shortcuts.syntax(sender);
        }
        return true;
    }

    public static class shortcuts {
        public static void syntax(CommandSender sender) {
            sender.sendMessage(ChatColor.RED + "Incorrect syntax!");
            sender.sendMessage(ChatColor.RED + "Type /bw map to get started.");
        }
        public static void custom(CommandSender sender, String text) {
            sender.sendMessage(ChatColor.RED + text);
            sender.sendMessage(ChatColor.RED + "Type /bw map to get started.");
        }
        public static boolean permissions(CommandSender sender) {
            if((sender.hasPermission("bw.admin")) || (sender.isOp())) return true;
            else sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command!");
            return false;
        }
    }
}
