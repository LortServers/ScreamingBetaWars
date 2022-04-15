package me.screamingbetawars;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    public static Map<String, Map<String, Object>> map_cache = new HashMap<>();
    public static class cfg {
        public static DumperOptions getYamlOptions() {
            DumperOptions options = new DumperOptions();
            options.setIndent(2);
            options.setPrettyFlow(true);
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            return options;
        }

        public static void update() {
            File[] list = Bukkit.getServer().getPluginManager().getPlugin("ScreamingBetaWars").getDataFolder().listFiles();
            if (list != null) {
                for (File yaml_file : list) {
                    if (!yaml_file.isDirectory()) {
                        try {
                        Yaml yaml = new Yaml(cfg.getYamlOptions());
                        InputStream file = new FileInputStream(yaml_file);
                        map_cache.put(yaml_file.getName().replace(".yml", ""), (Map<String, Object>) yaml.load(file));
                        } catch(Exception ignored) {}
                    }
                }
            }
        }

        public static String check(String arg, boolean map_exists) {
            if (map_cache.containsKey(arg) == map_exists) return "true";
            else return ChatColor.RED + "Map already exists!";
        }

        public static String check(String arg, boolean map_exists, boolean edit) {
            if (check(arg, true).equals(String.valueOf(map_exists))) {
                if(cfg.get(arg, "edit") == null) return "false";
                if(cfg.get(arg, "edit").equals(String.valueOf(edit))) return "true";
                else return ChatColor.RED + "Map is not being edited!";
            } else return ChatColor.RED + "Map not found.";
        }

        public static void put(String arg, String key, Object value) {
            try {
                File check_file = new File(Bukkit.getServer().getPluginManager().getPlugin("ScreamingBetaWars").getDataFolder(), arg + ".yml");
                Yaml yaml = new Yaml(cfg.getYamlOptions());
                Map<String, Object> map_data = map_cache.get(arg);
                PrintWriter file_save = new PrintWriter(check_file);
                map_data.put(key, value);
                yaml.dump(map_data, file_save);
                if(map_cache.containsKey(arg)) map_cache.get(arg).replace(key, value);
                else map_cache.get(arg).put(key, value);
            } catch(Exception ignored) {}
        }

        public static void putLocation(String arg, String key, String nick) {
            cfg.put(arg, key.replace("?", "x"), Bukkit.getPlayer(nick).getLocation().getBlockX());
            cfg.put(arg, key.replace("?", "y"), Bukkit.getPlayer(nick).getLocation().getBlockY());
            cfg.put(arg, key.replace("?", "z"), Bukkit.getPlayer(nick).getLocation().getBlockZ());
        }

        public static void remove(String arg, String key) {
            try {
                File check_file = new File(Bukkit.getServer().getPluginManager().getPlugin("ScreamingBetaWars").getDataFolder(), arg + ".yml");
                Yaml yaml = new Yaml(cfg.getYamlOptions());
                Map<String, Object> map_data = map_cache.get(arg);
                PrintWriter file_save = new PrintWriter(check_file);
                map_data.remove(key);
                yaml.dump(map_data, file_save);
                map_cache.get(arg).remove(key);
            } catch(Exception ignored) {}
        }

        public static void removeLocation(String arg, String key) {
            cfg.remove(arg, key.replace("?", "x"));
            cfg.remove(arg, key.replace("?", "y"));
            cfg.remove(arg, key.replace("?", "z"));
        }

        public static boolean find(String arg, String key) {
            return map_cache.get(arg).containsKey(key);
        }

        public static String create(String arg, CommandSender sender) {
            if(arg.equals("config")) return ChatColor.RED + "You can't name your map \"config\", it's been used by the plugin itself!";
            if(!map_cache.containsKey(arg)) {
                try {
                    Bukkit.getServer().getPluginManager().getPlugin("ScreamingBetaWars").getDataFolder().mkdir();
                    new File(Bukkit.getServer().getPluginManager().getPlugin("ScreamingBetaWars").getDataFolder(), arg + ".yml").createNewFile();
                    map_cache.put(arg, new HashMap<>());
                    cfg.put(arg, "version", Main.version);
                    cfg.put(arg, "edit", "true");
                    cfg.put(arg, "spawners", "1");
                    cfg.put(arg, "world", sender.getServer().getPlayer(sender.getName()).getWorld().getName());
                    return ChatColor.AQUA + "Map \"" + arg + "\" created successfully.";
                } catch (Exception e) {
                    return ChatColor.RED + "Something went wrong. Please contact us using this error code: 0x000002.";
                }
            } else return ChatColor.RED + "Map already exists!";
        }

        public static String get(String arg, String key) {
            return (String) map_cache.get(arg).get(key);
        }

        public static int getInt(String arg, String key) {
            return (int) map_cache.get(arg).get(key);
        }

        public static Map<String, Object> get(String arg) {
            return map_cache.get(arg);
        }

        public static Location getLocation(String arg, String key) {
            return new Location(Bukkit.getServer().getWorld(cfg.get(arg, "world")), cfg.getInt(arg, key + "x"), cfg.getInt(arg, key + "y"), cfg.getInt(arg, key + "z"));
        }

        public static Map<String, Object> getStartingWith(String arg, String pattern) {
            HashMap<String, Object> list = new HashMap<>(cfg.get(arg));
            for(String key : cfg.get(arg).keySet()) {
                if(!key.startsWith(pattern)) list.remove(key);
            }
            return list;
        }
    }
}
