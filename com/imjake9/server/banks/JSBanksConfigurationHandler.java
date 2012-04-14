package com.imjake9.server.banks;

import com.imjake9.server.banks.utils.JSBCurrencyManager;
import com.imjake9.server.banks.utils.JSBank;
import java.io.File;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class JSBanksConfigurationHandler {
    
    private static YamlConfiguration banks;
    
    /**
     * Reloads the config.yml.
     */
    public static void loadConfig() {
        JSBanks.getPlugin().reloadConfig();
        JSBCurrencyManager.loadCurrencies();
    }
    
    /**
     * Saves the config.yml.
     */
    public static void saveConfig() {
        JSBanks.getPlugin().saveConfig();
    }
    
    /**
     * Retrieves the config.yml.
     * 
     * @return 
     */
    public static FileConfiguration getConfig() {
        return JSBanks.getPlugin().getConfig();
    }
    
    /**
     * Stores a bank with a string data id.
     * 
     * @param id
     * @param bank 
     */
    public static void setBank(String id, JSBank bank) {
        banks.set(id, bank);
    }
    
    /**
     * Retrieves a bank given a string data key.
     * 
     * @param id
     * @return 
     */
    public static JSBank getBank(String id) {
        return (JSBank) banks.get(id);
    }
    
    /**
     * Loads the banks.yml.
     */
    public static void loadBanks() {
        try {
            
            File b = new File(JSBanks.getPlugin().getDataFolder(), "banks.yml");
            if (!JSBanks.getPlugin().getDataFolder().exists()) JSBanks.getPlugin().getDataFolder().mkdirs();
            if (!b.exists()) b.createNewFile();
            
            banks = new YamlConfiguration();
            banks.load(b);
            
        } catch (Exception ex) {
            JSBanks.getPlugin().getMessager().severe("Error loading config.");
            ex.printStackTrace();
        }
    }
    
    /**
     * Saves the banks.yml.
     */
    public static void saveBanks() {
        try {
            
            File b = new File(JSBanks.getPlugin().getDataFolder(), "banks.yml");
            banks.save(b);
            
        } catch (Exception ex) {
            JSBanks.getPlugin().getMessager().severe("Error saving config.");
            ex.printStackTrace();
        }
    }
    
}
