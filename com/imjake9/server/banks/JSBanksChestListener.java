package com.imjake9.server.banks;

import com.imjake9.server.banks.utils.JSBCurrencyManager;
import com.imjake9.server.banks.utils.JSBMessage;
import com.imjake9.server.banks.utils.JSBank;
import com.imjake9.server.banks.utils.JSBankChestInventory;
import com.imjake9.server.lib.Messaging;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class JSBanksChestListener implements Listener {
    
    public static Map<String, List<String>> registeringPlayers = new HashMap<String, List<String>>();
    public static List<String> unregisteringPlayers = new ArrayList<String>();
    public static Map<String, Double> depositingPlayers = new HashMap<String, Double>();
    public static Map<String, Double> withdrawingPlayers = new HashMap<String, Double>();
    
    public static void clearFlags(String player) {
        registeringPlayers.remove(player);
        unregisteringPlayers.remove(player);
        depositingPlayers.remove(player);
        withdrawingPlayers.remove(player);
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Only manage banked inventories
        JSBankChestInventory inventory = JSBankChestInventory.fromInventoryEvent(event);
        if (inventory == null) return;
        JSBank bank = inventory.getData();
        if (bank == null) return;
        // Display the balance
        Messaging.send(JSBMessage.NEW_BALANCE, (Player) event.getPlayer(), String.valueOf(bank.getAmount()));
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        
        // Only manage banked inventories
        JSBankChestInventory inventory = JSBankChestInventory.fromInventoryEvent(event);
        if (inventory == null || inventory.getData() == null) return;
        // Don't do anything if the action is in the player's inventory (unless it's a shift-click)
        if (!JSBankChestInventory.clickIsInChest(event) && !event.isShiftClick()) return;
        // Only allow legal tender operations
        if (!JSBCurrencyManager.isLegalTender(event.getCursor().getType())) {
            event.setCancelled(true);
            return;
        }
        
        // Get bank data
        JSBank bank = inventory.getData();
        // Don't let vanilla MC handle transactions
        if (event.isLeftClick() && !event.isShiftClick()) {
            // Cancel event
            event.setCancelled(true);
            // Declare transaction
            double transaction = 0;
            // Handle deposits/withdrawals
            if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
                transaction += JSBCurrencyManager.getCurrencyValue(event.getCursor());
                event.setCursor(null);
            } else {
                transaction -= JSBCurrencyManager.getCurrencyValue(event.getCurrentItem());
                event.setCursor(event.getCurrentItem());
            }
            // Update balance
            bank.setAmount(bank.getAmount() + transaction);
        } else if (event.isLeftClick() && event.isShiftClick() && JSBankChestInventory.clickIsInChest(event)) {
            // Cancel event
            event.setCancelled(true);
            // Declare transaction
            double transaction = 0;
            // Get the new item and check for possible overflow
            HashMap<Integer, ItemStack> overflow = event.getWhoClicked().getInventory().addItem(event.getCurrentItem());
            // Commit the transaction
            transaction -= JSBCurrencyManager.getCurrencyValue(event.getCurrentItem());
            // Undo any overflow transactions
            for (ItemStack stack : overflow.values()) {
                transaction += JSBCurrencyManager.getCurrencyValue(stack);
            }
            // Update balance
            bank.setAmount(bank.getAmount() + transaction);
        } else if (event.isLeftClick() && event.isShiftClick() && !JSBankChestInventory.clickIsInChest(event)) {
            // Cancel event
            event.setCancelled(true);
            // Declare transaction
            double transaction = 0;
            // Commit the transaction
            transaction += JSBCurrencyManager.getCurrencyValue(event.getCurrentItem());
            // Null the item moved into the bank
            event.setCurrentItem(null);
            // Update balance
            bank.setAmount(bank.getAmount() + transaction);
        } else {
            // Must be implemented
            event.setCancelled(true);
            return;
        }
        // Save bank
        JSBanksConfigurationHandler.setBank(inventory.getID(), bank);
        JSBanksConfigurationHandler.saveBanks();
        // Update display
        JSBCurrencyManager.formatChestInventory(event.getInventory(), bank.getAmount());
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // Prevent breaking of unowned banks
        if (event.isCancelled()) return;
        Block block = event.getBlock();
        if (block.getType() != Material.CHEST) return;
        // Get managed inventory
        JSBankChestInventory inventory = new JSBankChestInventory((Chest) block.getState());
        JSBank bank = inventory.getData();
        // If not a bank, do nothing
        if (bank == null) return;
        // Fail if not owned
        if (!bank.hasOwner(event.getPlayer())) {
            event.setCancelled(true);
            Messaging.send(JSBMessage.PRIVATE_BANK, event.getPlayer());
        } else {
            // Fail if the chest contains items
            if (bank.getAmount() > 0) {
                event.setCancelled(true);
                Messaging.send(JSBMessage.CHEST_NOT_EMPTY, event.getPlayer());
            } else {
                // Unregister the bank
                JSBanksConfigurationHandler.setBank(inventory.getID(), null);
                JSBanksConfigurationHandler.saveBanks();
                Messaging.send(JSBMessage.UNREGISTRATION_COMPLETE, event.getPlayer());
            }
        }
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        // Prevent chests being placed next to banks (converting single chests to double chests)
        Block block = event.getBlock();
        if (block.getType() != Material.CHEST) return;
        Location loc = block.getLocation();
        // Check surrounding blocks
        for (BlockFace face : JSBankChestInventory.cardinals) {
            Location newLoc = new Location(loc.getWorld(),
                    loc.getBlockX() + face.getModX(),
                    loc.getBlockY() + face.getModY(),
                    loc.getBlockZ() + face.getModZ());
            if (newLoc.getBlock().getType() != Material.CHEST) continue;
            JSBankChestInventory inventory = new JSBankChestInventory((Chest) newLoc.getBlock().getState());
            if (inventory.getData() == null) continue;
            event.setCancelled(true);
            Messaging.send(JSBMessage.ADJACENT_TO_CHEST, event.getPlayer());
            return;
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) return;
        
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block clicked = event.getClickedBlock();
        
        // Handle chest opening
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (clicked.getType() != Material.CHEST) return;
            // Get managed inventory
            JSBankChestInventory inventory = new JSBankChestInventory((Chest) clicked.getState());
            // Get bank instance
            JSBank bank = inventory.getData();
            // If the chest isn't registered, return
            if (bank == null) return;
            // If the player doesn't own the bank, cancel the event
            if (!bank.getOwners().contains(event.getPlayer().getName().toLowerCase())) {
                event.setCancelled(true);
                Messaging.send(JSBMessage.PRIVATE_BANK, (Player) event.getPlayer());
            }
            return;
        }
        
        // Handle chest registration
        if (registeringPlayers.containsKey(event.getPlayer().getName())) {
            event.setCancelled(true);
            // If not a chest, cancel registration.
            if (clicked.getType() != Material.CHEST) {
                Messaging.send(JSBMessage.REGISTRATION_CANCELED, event.getPlayer());
            } else {
                // Get managed inventory
                JSBankChestInventory inventory = new JSBankChestInventory((Chest) clicked.getState());
                // Fail if the chest contains items
                if (!inventory.getFullInventory().isEmpty()) {
                    Messaging.send(JSBMessage.CHEST_NOT_EMPTY, event.getPlayer());
                } else {
                    // Fail if already registered
                    if (inventory.getData() != null) {
                        Messaging.send(JSBMessage.CHEST_ALREADY_REGISTERED, event.getPlayer());
                    } else {
                        // Register new bank
                        JSBank bank = new JSBank(registeringPlayers.get(event.getPlayer().getName()), 0);
                        JSBanksConfigurationHandler.setBank(inventory.getID(), bank);
                        JSBanksConfigurationHandler.saveBanks();
                        Messaging.send(JSBMessage.REGISTRATION_COMPLETE, event.getPlayer());
                    }
                }
            }
            registeringPlayers.remove(event.getPlayer().getName());
            return;
        }
        
        // Handle chest unregistration
        if (unregisteringPlayers.contains(event.getPlayer().getName())) {
            event.setCancelled(true);
            // If not a chest, don't do anything
            if (clicked.getType() != Material.CHEST) {
                Messaging.send(JSBMessage.UNREGISTRATION_CANCELED, event.getPlayer());
            } else {
                // Get managed inventory
                JSBankChestInventory inventory = new JSBankChestInventory((Chest) clicked.getState());
                // Fail if not registered
                JSBank bank = inventory.getData();
                if (bank == null) {
                    Messaging.send(JSBMessage.CHEST_NOT_REGISTERED, event.getPlayer());
                } else {
                    // Fail if not empty
                    if (bank.getAmount() > 0) {
                        Messaging.send(JSBMessage.CHEST_NOT_EMPTY, event.getPlayer());
                    } else {
                        // Make sure the player owns the bank
                        if (!bank.hasOwner(event.getPlayer())) {
                            Messaging.send(JSBMessage.PRIVATE_BANK, event.getPlayer());
                        } else {
                            // Unregister the bank
                            JSBanksConfigurationHandler.setBank(inventory.getID(), null);
                            JSBanksConfigurationHandler.saveBanks();
                            Messaging.send(JSBMessage.UNREGISTRATION_COMPLETE, event.getPlayer());
                        }
                    }
                }
            }
            unregisteringPlayers.remove(event.getPlayer().getName());
            return;
        }
        
        // Handle deposits and withdrawals
        boolean depositing = false;
        if ((depositing = depositingPlayers.containsKey(event.getPlayer().getName()))
                || withdrawingPlayers.containsKey(event.getPlayer().getName())) {
            event.setCancelled(true);
            // If not a chest, don't do anything
            if (clicked.getType() != Material.CHEST) {
                Messaging.send(JSBMessage.TRANSACTION_CANCELED, event.getPlayer());
            } else {
                // Get managed inventory
                JSBankChestInventory inventory = new JSBankChestInventory((Chest) clicked.getState());
                // Fail if not registered
                JSBank bank = inventory.getData();
                if (bank == null) {
                    Messaging.send(JSBMessage.CHEST_NOT_REGISTERED, event.getPlayer());
                } else {
                    // Make sure the player owns the bank
                    if (!bank.hasOwner(event.getPlayer())) {
                        Messaging.send(JSBMessage.PRIVATE_BANK, event.getPlayer());
                    } else {
                        // Commit transaction
                        double amount = (depositing ? depositingPlayers : withdrawingPlayers).get(event.getPlayer().getName());
                        if (depositing) {
                            JSBanks.getEconomy().bankWithdraw(event.getPlayer().getName(), amount);
                            bank.setAmount(bank.getAmount() + amount);
                            Messaging.send(JSBMessage.DEPOSITED_TO_BANK, event.getPlayer(), String.valueOf(amount));
                        } else {
                            if (bank.getAmount() < amount) {
                                Messaging.send(JSBMessage.NOT_ENOUGH_TO_WITHDRAW, event.getPlayer());
                            } else {
                                JSBanks.getEconomy().bankDeposit(event.getPlayer().getName(), amount);
                                bank.setAmount(bank.getAmount() - amount);
                                Messaging.send(JSBMessage.WITHDREW_FROM_BANK, event.getPlayer(), String.valueOf(amount));
                            }
                        }
                        inventory.formatInventory(bank.getAmount());
                        JSBanksConfigurationHandler.setBank(inventory.getID(), bank);
                        JSBanksConfigurationHandler.saveBanks();
                    }
                }
            }
            (depositing ? depositingPlayers : withdrawingPlayers).remove(event.getPlayer().getName());
            return;
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Remove player from registering queue
        if (registeringPlayers.containsKey(event.getPlayer().getName()))
            registeringPlayers.remove(event.getPlayer().getName());
        if (unregisteringPlayers.contains(event.getPlayer().getName()))
            unregisteringPlayers.remove(event.getPlayer().getName());
    }
    
}
