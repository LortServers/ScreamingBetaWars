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

    public static class cfg {
        public static DumperOptions getYamlOptions() {
            DumperOptions options = new DumperOptions();
            options.setIndent(2);
            options.setPrettyFlow(true);
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            return options;
        }

        public static String check(String arg, boolean map_exists) {
            File check_file = new File(Bukkit.getServer().getPluginManager().getPlugin("ScreamingBetaWars").getDataFolder(), arg + ".yml");
            if (check_file.exists() == map_exists) return "true";
            else return ChatColor.RED + "Map already exists!";
        }

        public static String check(String arg, boolean map_exists, boolean edit) {
            File check_file = new File(Bukkit.getServer().getPluginManager().getPlugin("ScreamingBetaWars").getDataFolder(), arg + ".yml");
            if (check_file.exists() == map_exists) {
                //try {
                    //Yaml yaml = new Yaml(cfg.getYamlOptions());
                    //InputStream file = new FileInputStream(check_file);
                    //Map<String, Object> map_data = (Map<String, Object>) yaml.load(file);
                    if(cfg.get(arg, "edit") == null) return "false";
                    if(cfg.get(arg, "edit").equals(String.valueOf(edit))) return "true";
                    else return ChatColor.RED + "Map is not being edited!";
                //} catch (FileNotFoundException e) {
                //    return ChatColor.RED + "Something went wrong. Please contact us using this error code: 0x000001.";
                //}
            } else return ChatColor.RED + "Map not found.";
        }

        public static void put(String arg, String key, String value) {
            try {
                File check_file = new File(Bukkit.getServer().getPluginManager().getPlugin("ScreamingBetaWars").getDataFolder(), arg + ".yml");
                Yaml yaml = new Yaml(cfg.getYamlOptions());
                InputStream file = new FileInputStream(check_file);
                Map<String, Object> map_data = (Map<String, Object>) yaml.load(file);
                PrintWriter file_save = new PrintWriter(check_file);
                map_data.put(key, value);
                yaml.dump(map_data, file_save);
            } catch(Exception ignored) {}
        }

        public static void putInt(String arg, String key, int value) {
            try {
                File check_file = new File(Bukkit.getServer().getPluginManager().getPlugin("ScreamingBetaWars").getDataFolder(), arg + ".yml");
                Yaml yaml = new Yaml(cfg.getYamlOptions());
                InputStream file = new FileInputStream(check_file);
                Map<String, Object> map_data = (Map<String, Object>) yaml.load(file);
                PrintWriter file_save = new PrintWriter(check_file);
                map_data.put(key, value);
                yaml.dump(map_data, file_save);
            } catch(Exception ignored) {}
        }
        /*
        public static void put(String arg, String category, String item, String key, String value) {
            try {
                File check_file = new File(Bukkit.getServer().getPluginManager().getPlugin("ScreamingBetaWars").getDataFolder(), arg + ".yml");
                Yaml yaml = new Yaml(cfg.getYamlOptions());
                InputStream file = new FileInputStream(check_file);
                Map<String, Object> map_data = (Map<String, Object>) yaml.load(file);
                Map<String, Map<String, String>> list_current = new HashMap<>((Map<String, Map<String, String>>) map_data.get(category));
                PrintWriter file_save = new PrintWriter(check_file);
                Map<String, String> data = new HashMap<>();
                data.put(key, value);
                list_current.put(item, data);
                map_data.put(category, list_current);
                yaml.dump(map_data, file_save);
            } catch(Exception ignored) {}
        }
        */
        public static void remove(String arg, String key) {
            try {
                File check_file = new File(Bukkit.getServer().getPluginManager().getPlugin("ScreamingBetaWars").getDataFolder(), arg + ".yml");
                Yaml yaml = new Yaml(cfg.getYamlOptions());
                InputStream file = new FileInputStream(check_file);
                Map<String, Object> map_data = (Map<String, Object>) yaml.load(file);
                PrintWriter file_save = new PrintWriter(check_file);
                map_data.remove(key);
                yaml.dump(map_data, file_save);
            } catch(Exception ignored) {}
        }

        public static boolean find(String arg, String key) {
            try {
                File check_file = new File(Bukkit.getServer().getPluginManager().getPlugin("ScreamingBetaWars").getDataFolder(), arg + ".yml");
                Yaml yaml = new Yaml(cfg.getYamlOptions());
                InputStream file = new FileInputStream(check_file);
                Map<String, Object> map_data = (Map<String, Object>) yaml.load(file);
                return map_data.containsKey(key);
            } catch(Exception e) { return false; }
        }

        public static String create(String arg, CommandSender sender) {
            File file = new File(Bukkit.getServer().getPluginManager().getPlugin("ScreamingBetaWars").getDataFolder(), arg + ".yml");
            if(arg.equals("config")) return ChatColor.RED + "You can't name your map \"config\", it's been used by the plugin itself!";
            if(!file.exists()) {
                try {
                    Bukkit.getServer().getPluginManager().getPlugin("ScreamingBetaWars").getDataFolder().mkdir();
                    file.createNewFile();
                    cfg.put(arg, "version", "b1.7.3-0.1");
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
            try {
                File check_file = new File(Bukkit.getServer().getPluginManager().getPlugin("ScreamingBetaWars").getDataFolder(), arg + ".yml");
                Yaml yaml = new Yaml(cfg.getYamlOptions());
                InputStream file = new FileInputStream(check_file);
                Map<String, Object> map_data = (Map<String, Object>) yaml.load(file);
                return (String) map_data.get(key);
            } catch(Exception e) { return null; }
        }

        public static int getInt(String arg, String key) {
            try {
                File check_file = new File(Bukkit.getServer().getPluginManager().getPlugin("ScreamingBetaWars").getDataFolder(), arg + ".yml");
                Yaml yaml = new Yaml(cfg.getYamlOptions());
                InputStream file = new FileInputStream(check_file);
                Map<String, Object> map_data = (Map<String, Object>) yaml.load(file);
                return (int) map_data.get(key);
            } catch(Exception e) { return 0; }
        }

        public static Map<String, Object> get(String arg) {
            try {
                File check_file = new File(Bukkit.getServer().getPluginManager().getPlugin("ScreamingBetaWars").getDataFolder(), arg + ".yml");
                Yaml yaml = new Yaml(cfg.getYamlOptions());
                InputStream file = new FileInputStream(check_file);
                return (Map<String, Object>) yaml.load(file);
            } catch(Exception e) { return new HashMap<>(); }
        }

        public static Location getLocation(String arg, String key) {
            return new Location(Bukkit.getServer().getWorld(cfg.get(arg, "world")), cfg.getInt(arg, key + "x"), cfg.getInt(arg, key + "y"), cfg.getInt(arg, key + "z"));
        }

        public static Map<String, Object> getStartingWith(String arg, String pattern) {
            Map<String, Object> list = cfg.get(arg);
            for(String key : cfg.get(arg).keySet()) {
                if(!key.startsWith(pattern)) list.remove(key);
            }
            return list;
        }

        public static Map<String, Object> getLocStartingWith(String arg, String pattern) {
            Map<String, Object> list = cfg.get(arg);
            for(String key : cfg.get(arg).keySet()) {
                if(!key.startsWith(pattern)) list.remove(key);
            }
            return list;
        }

    }
}
