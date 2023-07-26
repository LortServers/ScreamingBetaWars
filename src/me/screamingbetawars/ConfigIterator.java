package me.screamingbetawars;

import org.bukkit.Location;

public class ConfigIterator {

    public interface ConfigRunnable {
        void run(String map);
    }
    public ConfigIterator(boolean map_exists, boolean edit, Location location, ConfigRunnable code) {
        for(String map : ConfigManager.map_cache.keySet()) {
            if((ConfigManager.cfg.check(map, map_exists, edit).equals("true")) && ((location == null) || (Block.pos.check(map, location)))) code.run(map);
        }
    }
    public ConfigIterator(Location location, ConfigRunnable code) { new ConfigIterator(true, false, location, code); }
}
