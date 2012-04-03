package com.imjake9.server.banks.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;

@SerializableAs("Bank")
public class JSBank implements ConfigurationSerializable {
    
    private List<String> owners;
    private double amount;
    
    public JSBank (List<String> owners, double amount) {
        this.owners = owners;
        this.amount = amount;
    }
    
    public JSBank (String owner, double amount) {
        List<String> newOwners = new ArrayList<String>();
        newOwners.add(owner);
        this.owners = newOwners;
        this.amount = amount;
    }
    
    @SuppressWarnings("unchecked")
    public JSBank (Map<String, Object> data) {
        this.owners = (List<String>) data.get("owners");
        this.amount = (Double) data.get("amount");
    }
    
    public List<String> getOwners() {
        return this.owners;
    }
    
    public double getAmount() {
        return this.amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public boolean hasOwner(Player player) {
        return hasOwner(player.getName());
    }
    
    public boolean hasOwner(String player) {
        return owners.contains(player.toLowerCase());
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> struct = new HashMap<String, Object>();
        struct.put("owners", owners);
        struct.put("amount", amount);
        return struct;
    }
    
    public static JSBank deserialize(Map<String, Object> data) {
        return new JSBank (data);
    }
    
}
