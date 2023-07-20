package me.screamingbetawars;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import me.screamingbetawars.ConfigManager.*;
import me.screamingbetawars.Game.*;
import org.bukkit.scheduler.BukkitWorker;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.*;

public class Main extends JavaPlugin implements CommandExecutor, Listener, EventListener {
    public static String version = "b1.7.3-0.2-dev";
    public static Plugin instance;
    @Override
    public void onEnable() {
        instance = this;
        Bukkit.getLogger().info("ScreamingBetaWars is starting...");
        registerEvents(new me.screamingbetawars.Block(), this);
        registerEvents(new me.screamingbetawars.Death(), this);
        registerEvents(new me.screamingbetawars.Player(), this);
        registerEvents(new me.screamingbetawars.Entity(), this);
        this.getCommand("bw").setExecutor(this);
        cfg.update();
        File file = new File(Bukkit.getServer().getPluginManager().getPlugin("ScreamingBetaWars").getDataFolder(), "config.yml");
        if(!cfg.check("config", true).equals("true")) {
            try {
                Bukkit.getServer().getPluginManager().getPlugin("ScreamingBetaWars").getDataFolder().mkdir();
                file.createNewFile();
                ConfigManager.map_cache.put("config", new HashMap<>());
            } catch (IOException ignored) {}
        }
        if(cfg.get("config", "version") == null) {
            cfg.put("config", "version", version);
            cfg.put("config", "respawn-time", "5");
            cfg.put("config", "debug", "false");
        }
        File file2 = new File(Bukkit.getServer().getPluginManager().getPlugin("ScreamingBetaWars").getDataFolder(), "shop_file.yml");
        if(!cfg.check("shop_file", true).equals("true")) {
            try {
                Bukkit.getServer().getPluginManager().getPlugin("ScreamingBetaWars").getDataFolder().mkdir();
                file2.createNewFile();
                ConfigManager.map_cache.put("shop_file", new HashMap<>());
            } catch (IOException ignored) {}
        }
        if(cfg.get("shop_file", "slot-1") == null) {
            cfg.put("shop_file", "slot-1", "BOW;1;GOLD_INGOT;10;");
            cfg.put("shop_file", "slot-2", "IRON_SWORD;1;GOLD_INGOT;5;");
            cfg.put("shop_file", "slot-3", "WOOL;15;IRON_INGOT;5;");
            cfg.put("shop_file", "slot-4", "WOOD;10;GOLD_INGOT;5;");
            cfg.put("shop_file", "slot-5", "BOW;1;GOLD_INGOT;10;");
            cfg.put("shop_file", "slot-6", "BOW;1;GOLD_INGOT;10;");
            cfg.put("shop_file", "slot-7", "BOW;1;GOLD_INGOT;10;");
            cfg.put("shop_file", "slot-8", "BOW;1;GOLD_INGOT;10;");
            cfg.put("shop_file", "slot-9", "BOW;1;GOLD_INGOT;10;");
        }
        for(Player player : getServer().getOnlinePlayers()) {
            try { me.screamingbetawars.Player.playerOverride(player); } catch(NoSuchFieldException | IllegalAccessException ignored) {}
        }
    }

    @Override
    public void onDisable() {
        for(BukkitWorker test : Bukkit.getScheduler().getActiveWorkers()) {
            if(test.getOwner() == this) Bukkit.getScheduler().cancelTask(test.getTaskId());
        }
        Game.games.clear();
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
                        if(Game.game.getGame(args[1]).started) {
                            sender.sendMessage(ChatColor.RED + "You cannot change the map state after the game has started!");
                            return true;
                        }
                        Game.game.endGame(args[1]);
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
                        cfg.putLocation(args[1], "pos1?", player.getName());
                        sender.sendMessage(ChatColor.AQUA + "First position has been set.");
                    } else sender.sendMessage(ChatColor.RED + cfg.check(args[1], true, true));
                } else if(args[2].equals("pos2")) {
                    if(cfg.check(args[1], true, true).equals("true")) {
                        cfg.putLocation(args[1], "pos2?", player.getName());
                        sender.sendMessage(ChatColor.AQUA + "Second position has been set.");
                    } else sender.sendMessage(ChatColor.RED + cfg.check(args[1], true, true));
                } else if(args[2].equals("lobby1")) {
                    if(cfg.check(args[1], true, true).equals("true")) {
                        cfg.putLocation(args[1], "lobby1?", player.getName());
                        sender.sendMessage(ChatColor.AQUA + "First position has been set.");
                    } else sender.sendMessage(ChatColor.RED + cfg.check(args[1], true, true));
                } else if(args[2].equals("lobby2")) {
                    if(cfg.check(args[1], true, true).equals("true")) {
                        cfg.putLocation(args[1], "lobby2?", player.getName());
                        sender.sendMessage(ChatColor.AQUA + "Second position has been set.");
                    } else sender.sendMessage(ChatColor.RED + cfg.check(args[1], true, true));
                } else if(args[2].equals("lobby")) {
                    if(cfg.check(args[1], true, true).equals("true")) {
                        cfg.putLocation(args[1], "lobby-?", player.getName());
                        sender.sendMessage(ChatColor.AQUA + "Lobby spawn has been set.");
                    } else sender.sendMessage(ChatColor.RED + cfg.check(args[1], true, true));
                } else if(args[2].equals("spec")) {
                    if(cfg.check(args[1], true, true).equals("true")) {
                        cfg.putLocation(args[1], "spec-?", player.getName());
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
                                cfg.removeLocation(args[1], "?1-bed-team-" + args[4]);
                                cfg.removeLocation(args[1], "?2-bed-team-" + args[4]);
                                cfg.removeLocation(args[1], "villager-?-team-" + args[4]);
                                cfg.remove(args[1], "villager-yaw-team-" + args[4]);
                                cfg.remove(args[1], "villager-pitch-team-" + args[4]);
                                sender.sendMessage(ChatColor.AQUA + "Team \"" + args[4] + "\" has been removed.");
                            } else sender.sendMessage(ChatColor.RED + "Team does not exist.");
                        } else if(args[3].equals("bed1")) {
                            if (cfg.find(args[1], "team-" + args[4])) {
                                cfg.putLocation(args[1], "?1-bed-team-" + args[4], player.getName());
                                sender.sendMessage(ChatColor.AQUA + "First bed position for team \"" + args[4] + "\" is set.");
                            } else sender.sendMessage(ChatColor.RED + "Team does not exist.");
                        } else if(args[3].equals("bed2")) {
                            if (cfg.find(args[1], "team-" + args[4])) {
                                cfg.putLocation(args[1], "?2-bed-team-" + args[4], player.getName());
                                sender.sendMessage(ChatColor.AQUA + "Second bed position for team \"" + args[4] + "\" is set.");
                            } else sender.sendMessage(ChatColor.RED + "Team does not exist.");
                        } else if(args[3].equals("spawn")) {
                            if (cfg.find(args[1], "team-" + args[4])) {
                                cfg.putLocation(args[1], "?-spawn-team-" + args[4], player.getName());
                                sender.sendMessage(ChatColor.AQUA + "Spawn for team \"" + args[4] + "\" is set.");
                            } else sender.sendMessage(ChatColor.RED + "Team does not exist.");
                        } else if(args[3].equals("shop")) {
                            if (cfg.find(args[1], "team-" + args[4])) {
                                cfg.putLocationExact(args[1], "villager-?-team-" + args[4], player.getName());
                                cfg.put(args[1], "villager-yaw-team-" + args[4], String.valueOf(player.getLocation().getYaw()));
                                cfg.put(args[1], "villager-pitch-team-" + args[4], String.valueOf(player.getLocation().getPitch()));
                                sender.sendMessage(ChatColor.AQUA + "Shop position for team \"" + args[4] + "\" is set.");
                            } else sender.sendMessage(ChatColor.RED + "Team does not exist.");
                        } else shortcuts.syntax(sender);
                    } else shortcuts.syntax(sender);
                } else if(args[2].equals("spawner")) {
                    if(args.length == 5) {
                        if (cfg.check(args[1], true, true).equals("true")) {
                            Material test = Material.getMaterial(args[3].toUpperCase());
                            if(test == null) {
                                shortcuts.custom(sender, "Incorrect item type!");
                                return false;
                            }
                            try {
                                int test2 = Integer.parseInt(args[4]);
                            } catch (NumberFormatException e) {
                                shortcuts.custom(sender, "Incorrect amount of seconds!");
                                return false;
                            }
                            int spawner = Integer.parseInt(cfg.get(args[1], "spawners"));
                            cfg.putLocationExact(args[1], "spawner-?-" + spawner, player.getName());
                            cfg.put(args[1], "spawner-type-" + spawner, args[3].toUpperCase());
                            cfg.put(args[1], "spawner-time-" + spawner, args[4]);
                            cfg.put(args[1], "spawners", spawner + 1);
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
        } else if(args[0].equals("setspawn")) {
            if(!shortcuts.permissions(sender)) return true;
            cfg.putLocation("config", "spawn-?", player.getName());
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

    public void registerEvents(Listener listener, Plugin plugin) {
        for (final Method method : listener.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(EventHandler.class)) {
                final EventHandler eventHandler = method.getAnnotation(EventHandler.class);
                String temp = method.getParameters()[0].getType().getSimpleName();
                temp = temp.replace("Event", "").replaceFirst(Character.toString(temp.charAt(0)), Character.toString(temp.toLowerCase().charAt(0)));
                for(Character match : temp.toCharArray()) {
                    if(Character.isUpperCase(match)) temp = temp.replace(Character.toString(match), "_" + Character.toLowerCase(match));
                }
                temp = temp.toUpperCase();
                try {
                    Bukkit.getPluginManager().registerEvent(
                            Event.Type.valueOf(temp),
                            listener,
                            (listenerInstance, event) -> {
                                try {
                                    method.invoke(listenerInstance, event);
                                } catch (Throwable e) {
                                    try {
                                        throw new EventException(e);
                                    } catch (EventException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                }
                            },
                            eventHandler.priority(),
                            plugin
                    );
                } catch(IllegalArgumentException ignored) {}
            }
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface EventHandler {
        Event.Priority priority() default Event.Priority.Normal;
        boolean ignoreCancelled() default false;
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
