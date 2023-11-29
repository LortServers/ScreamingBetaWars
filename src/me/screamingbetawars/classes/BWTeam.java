package me.screamingbetawars.classes;

import com.sun.istack.internal.NotNull;
import org.bukkit.Location;

public class BWTeam {
    private static String name;
    private static String color;
    private static int size;
    private static Location bed1;
    private static Location bed2;
    private static Location villager;
    private static Location spawn;
    private static boolean bed_destroyed = false;

    public BWTeam(String n, String c, int s, Location b1, Location b2, Location v, Location s2) {
        name = n;
        color = c;
        size = s;
        bed1 = b1;
        bed2 = b2;
        villager = v;
        spawn = s2;
    }

    @NotNull
    public String getName() { return name; }

    @NotNull
    public String getColor() { return color; }

    @NotNull
    public int getSize() { return size; }

    @NotNull
    public Location getBedPos1() { return bed1; }

    @NotNull
    public Location getBedPos2() { return bed2; }

    @NotNull
    public Location getVillager() { return villager; }

    @NotNull
    public Location getSpawn() { return spawn; }

    @NotNull
    public void destroyBed() { bed_destroyed = true; }

    @NotNull
    public boolean isBedDestroyed() { return bed_destroyed; }
}