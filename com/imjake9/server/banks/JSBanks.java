package com.imjake9.server.banks;

import com.imjake9.server.banks.utils.JSBank;
import com.imjake9.server.lib.plugin.JSPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.RegisteredServiceProvider;

public class JSBanks extends JSPlugin {
    
    private static JSBanks plugin;
    
    private JSBanksCommandHandler commandHandler;
    private JSBanksConfigurationHandler configurationHandler;
    private JSBanksChestListener inventoryListener;
    
    private Economy vaultEconomy;
    
    static {
        ConfigurationSerialization.registerClass(JSBank.class, "Bank");
    }
    
    @Override
    public void onJSEnable() {
        plugin = this;
        
        commandHandler = new JSBanksCommandHandler();
        configurationHandler = new JSBanksConfigurationHandler();
        inventoryListener = new JSBanksChestListener();
        
        JSBanksConfigurationHandler.loadBanks();
        JSBanksConfigurationHandler.loadConfig();
        
        // Load economy
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        vaultEconomy = rsp.getProvider();
        
        getServer().getPluginManager().registerEvents(inventoryListener, plugin);
    }
    
    @Override
    public void onJSDisable() {
        JSBanksConfigurationHandler.saveBanks();
    }
    
    public static JSBanks getPlugin() {
        return plugin;
    }
    
    public static JSBanksCommandHandler getCommandHandler() {
        return plugin.commandHandler;
    }
    
    public static JSBanksConfigurationHandler getConfigurationHandler() {
        return plugin.configurationHandler;
    }
    
    public static Economy getEconomy() {
        return plugin.vaultEconomy;
    }
    
}
