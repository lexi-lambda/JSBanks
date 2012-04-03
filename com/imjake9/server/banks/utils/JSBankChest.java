package com.imjake9.server.banks.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

public class JSBankChest {
    
    private Location leftChest;
    private Location rightChest;
    
    public JSBankChest(Location loc) {
        leftChest = loc;
    }
    
    public JSBankChest(Location leftLoc, Location rightLoc) {
        leftChest = leftLoc;
        rightChest = rightLoc;
    }
    
    public boolean isDoubleChest() {
        return leftChest != null && rightChest != null;
    }
    
    public Location getLeftChest() {
        return leftChest;
    }
    
    public Location getRightChest() {
        return rightChest;
    }
    
    public Location getChest() {
        return (leftChest == null) ? rightChest : leftChest;
    }
    
    public void setLeftChest(Location loc) {
        leftChest = loc;
    }
    
    public void setRightChest(Location loc) {
        rightChest = loc;
    }
    
    /**
     * Confirms that the chest locations still contain chests.
     */
    public void validate() {
        if (leftChest.getBlock().getType() != Material.CHEST)
            leftChest = null;
        if (rightChest.getBlock().getType() != Material.CHEST)
            rightChest = null;
    }
    
    public String getID() {
        this.validate();
        if (isDoubleChest())
            return getID(leftChest, rightChest);
        else
            return getID(getChest());
    }
    
    public static String getID(Location leftLoc, Location rightLoc) {
        return leftLoc.getWorld().getName() + "-"
                + leftLoc.getBlockX() + "-" + leftLoc.getBlockY() + "-" + leftLoc.getBlockZ() + ":"
                + rightLoc.getBlockX() + "-" + rightLoc.getBlockY() + "-" + rightLoc.getBlockZ();
    }
    
    public static String getID(Location loc) {
        return loc.getWorld().getName() + "-"
                + loc.getBlockX() + "-" + loc.getBlockY() + "-" + loc.getBlockZ();
    }
    
    public static JSBankChest fromID(String id) {
        String[] data = id.split("-");
        if (id.contains(":")) {
            Location leftLoc = new Location(
                    Bukkit.getWorld(data[0]),
                    Integer.parseInt(data[1]),
                    Integer.parseInt(data[2]),
                    Integer.parseInt(data[3]));
            Location rightLoc = new Location(
                    Bukkit.getWorld(data[4]),
                    Integer.parseInt(data[5]),
                    Integer.parseInt(data[6]),
                    Integer.parseInt(data[7]));
            return new JSBankChest(leftLoc, rightLoc);
        } else {
            Location leftLoc = new Location(
                    Bukkit.getWorld(data[0]),
                    Integer.parseInt(data[1]),
                    Integer.parseInt(data[2]),
                    Integer.parseInt(data[3]));
            return new JSBankChest(leftLoc);
        }
    }
    
}
