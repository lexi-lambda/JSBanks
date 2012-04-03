package com.imjake9.server.banks.utils;

import com.imjake9.server.banks.JSBanksConfigurationHandler;
import java.util.*;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class JSBCurrencyManager {
    
    private static Map<Material, Double> currencies;
    
    public static void loadCurrencies() {
        ConfigurationSection currency = JSBanksConfigurationHandler.getConfig().getConfigurationSection("currency");
        currencies = new EnumMap<Material, Double>(Material.class);
        if (currency == null) return;
        for (String key : currency.getKeys(false)) {
            ConfigurationSection data = currency.getConfigurationSection(key);
            currencies.put(Material.getMaterial(data.getInt("id")), data.getDouble("amount"));
        }
        currencies.put(Material.AIR, 0d);
    }
    
    public static boolean isLegalTender(Material mat) {
        return mat == Material.AIR || currencies.containsKey(mat);
    }
    
    /**
     * Gets the combined value of an entire inventory.
     * 
     * @param inventory
     * @return 
     */
    public static double getCurrencyValue(Inventory inventory) {
        List<ItemStack> items = new ArrayList<ItemStack>();
        for (ItemStack stack : inventory.getContents()) {
            if (stack != null)
                items.add(stack);
        }
        return getCurrencyValue(items);
    }
    
    /**
     * Gets the combined value of a series of item stacks.
     * 
     * @param items
     * @return 
     */
    public static double getCurrencyValue(List<ItemStack> items) {
        double amount = 0;
        for (ItemStack item : items)
            amount += getCurrencyValue(item);
        return amount;
    }
    
    /**
     * Gets the combined value of an entire item stack.
     * 
     * @param item
     * @return 
     */
    public static double getCurrencyValue(ItemStack item) {
        return getCurrencyValue(item.getType()) * item.getAmount();
    }
    
    /**
     * Gets the currency value for a certain material.
     * 
     * @param mat
     * @return 
     */
    public static double getCurrencyValue(Material mat) {
        if (!isLegalTender(mat)) return 0;
        return currencies.get(mat);
    }
    
    public static void formatChestInventory(Inventory inventory, double amount) {
        
        // Clear old inventories
        inventory.clear();
        
        // Set up variables
        double left = amount;
        Map<Currency, Integer> stacks = new EnumMap<Currency, Integer>(Currency.class);
        
        // Init stack values
        for (Currency currency : Currency.values()) {
            stacks.put(currency, 0);
        }
        
        if (JSBankChestInventory.inventoryIsDoubleChest(inventory)) { // Double chests
            
            // Distribute data
            while (left >= Currency.IRON.getValue()) {
                for (Currency currency : Currency.values()) {

                    if (left < currency.getValue())
                        continue;

                    stacks.put(currency, stacks.get(currency) + 1);

                    left -= currency.getValue();

                }
            }
            
            // Display data
            for (int i = 0; i < 6; i++) {
                
                Currency currency = Currency.values()[i];
                int data = stacks.get(currency);
                int remainder = data % 64;
                int fullstacks = (data - remainder)/64;
                
                boolean full = fullstacks >= 9;
                for (int j = 0; j < (full ? 9 : fullstacks); j++) {
                    inventory.setItem((i * 9) + j, new ItemStack(currency.getMaterial(), 64));
                }
                
                if (!full && remainder > 0) {
                    inventory.setItem((i * 9) + fullstacks, new ItemStack(currency.getMaterial(), remainder));
                }
                
            }
            
        } else { // Single chests
            
            // Distribute data
            int minCurrency = 5;
            while (left >= Currency.values()[minCurrency].getValue()) {
                for (Currency currency : Currency.values()) {

                    if (left < currency.getValue())
                        continue;
                    if (Arrays.asList(Currency.ingots()).contains(currency) && stacks.get(currency) >= 64) {
                        if (Currency.values()[minCurrency] == currency)
                            minCurrency--;
                        if (minCurrency <= 0)
                            break;
                        continue;
                    }

                    stacks.put(currency, stacks.get(currency) + 1);

                    left -= currency.getValue();

                }
            }
            
            for (int i = 0; i < 3; i++) {
                
                // Display ingots
                Currency ingot = Currency.ingots()[i];
                if (stacks.get(ingot) > 0)
                    inventory.setItem((i * 9) + 8, new ItemStack(ingot.getMaterial(), stacks.get(ingot)));
                
                // Display blocks
                Currency block = Currency.blocks()[i];
                int data = stacks.get(block);
                int remainder = data % 64;
                int fullstacks = (data - remainder)/64;
                
                boolean full = fullstacks >= 8;
                for (int j = 0; j < (full ? 8 : fullstacks); j++) {
                    inventory.setItem((i * 9) + j, new ItemStack(block.getMaterial(), 64));
                }
                
                if (!full && remainder > 0) {
                    inventory.setItem((i * 9) + fullstacks, new ItemStack(block.getMaterial(), remainder));
                }
                
            }
            
        }
    }
    
    public static enum Currency {
        DIAMOND_BLOCK (9000, Material.DIAMOND_BLOCK),
        DIAMOND (1000, Material.DIAMOND),
        GOLD_BLOCK (1800, Material.GOLD_BLOCK),
        GOLD (200, Material.GOLD_INGOT),
        IRON_BLOCK (900, Material.IRON_BLOCK),
        IRON (100, Material.IRON_INGOT);
        
        private double value;
        private Material material;
        
        Currency (double value, Material mat) {
            this.value = value;
            material = mat;
        }
        
        public double getValue() {
            return value;
        }
        
        public Material getMaterial() {
            return material;
        }
        
        public static Currency[] blocks() {
            return new Currency[] {DIAMOND_BLOCK, GOLD_BLOCK, IRON_BLOCK};
        }
        
        public static Currency[] ingots() {
            return new Currency[] {DIAMOND, GOLD, IRON};
        }
    }
    
}
