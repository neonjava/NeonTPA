package me.neonjava.neontpa.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class TPAUtils {
    public static boolean isSafe(Location location) {
        if (location == null)
            return false;

        Block feet = location.getBlock();
        Block head = feet.getRelative(0, 1, 0);
        Block lava = feet.getRelative(0, -1, 0);

        // Check for lava or air below
        if (lava.getType() == Material.LAVA || lava.getType() == Material.AIR)
            return false;

        // Check for blocks inside the body
        if (feet.getType().isSolid() || head.getType().isSolid())
            return false;

        // Check for void
        if (location.getY() < location.getWorld().getMinHeight())
            return false;

        return true;
    }
}
