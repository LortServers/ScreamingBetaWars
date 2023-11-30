package me.screamingbetawars.classes;

import org.bukkit.Location;

public class BWSpawner {

    private static Location location;

    private static String type;

    private static int time;

    public BWSpawner(Location l, String ty, int ti) {
        location = l;
        type = ty;
        time = ti;
    }

    public Location getLocation() {
        return location;
    }

    public String getType() {
        return type;
    }

    public int getTime() {
        return time;
    }
}
