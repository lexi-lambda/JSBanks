package com.imjake9.server.banks;

import com.imjake9.server.banks.utils.JSBMessage;
import com.imjake9.server.lib.Messaging;
import com.imjake9.server.lib.Messaging.JSMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

public class JSBanksCommandHandler implements CommandExecutor {
    
    public JSBanksCommandHandler() {
        
        JSBanks plugin = JSBanks.getPlugin();
        
        for (JSBanksCommands command : JSBanksCommands.values()) {
            
            PluginCommand cmd = plugin.getCommand(command.name().toLowerCase());
            
            if (cmd == null)
                continue;
            
            cmd.setAliases(Arrays.asList(command.getAliases()));
            cmd.setPermission(command.getPermission());
            cmd.setPermissionMessage(command.getPermissionMessage());
            cmd.setExecutor(this);
            
        }
        
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String... args) {
        
        return JSBanksCommands.handleCommand(command, sender, args);
        
    }
    
    public static enum JSBanksCommands {
        
        BCREATE("cbank") {
            
            @Override
            public boolean handle(CommandSender sender, String... args) {
                JSBanksChestListener.clearFlags(sender.getName());
                List<String> owners = new ArrayList<String>();
                owners.add(sender.getName().toLowerCase());
                for (String owner : args) {
                    owners.add(owner.toLowerCase());
                }
                JSBanksChestListener.registeringPlayers.put(sender.getName(), owners);
                Messaging.send(JSBMessage.REGISTERING_BANK, sender);
                return true;
            }
            
        },
        BDEPOSIT("bdep", "bplus", "badd") {
            
            @Override
            public boolean handle(CommandSender sender, String... args) {
                JSBanksChestListener.clearFlags(sender.getName());
                if (args.length < 1) {
                    Messaging.send(JSMessage.MISSING_PARAMETER, sender, "amount");
                    return false;
                }
                double amount;
                try {
                    amount = Double.parseDouble(args[0]);
                } catch (NumberFormatException ex) {
                    Messaging.send(JSMessage.INVALID_PARAMTER, sender, "amount", args[0]);
                    return false;
                }
                if (!JSBanks.getEconomy().has(sender.getName(), amount)) {
                    Messaging.send(JSBMessage.NOT_ENOUGH_TO_DEPOSIT, sender);
                    return true;
                }
                JSBanksChestListener.depositingPlayers.put(sender.getName(), amount);
                Messaging.send(JSBMessage.DEPOSITING_TO_BANK, sender);
                return true;
            }
            
        },
        BREMOVE("rbank") {
            
            @Override
            public boolean handle(CommandSender sender, String... args) {
                JSBanksChestListener.clearFlags(sender.getName());
                JSBanksChestListener.unregisteringPlayers.add(sender.getName());
                Messaging.send(JSBMessage.UNREGISTERING_BANK, sender);
                return true;
            }
            
        },
        BWITHDRAW("bwith", "bminus", "bsubtract", "bsub") {
            
            @Override
            public boolean handle(CommandSender sender, String... args) {
                JSBanksChestListener.clearFlags(sender.getName());
                if (args.length < 1) {
                    Messaging.send(JSMessage.MISSING_PARAMETER, sender, "amount");
                    return false;
                }
                double amount;
                try {
                    amount = Double.parseDouble(args[0]);
                } catch (NumberFormatException ex) {
                    Messaging.send(JSMessage.INVALID_PARAMTER, sender, "amount", args[0]);
                    return false;
                }
                JSBanksChestListener.withdrawingPlayers.put(sender.getName(), amount);
                Messaging.send(JSBMessage.WITHDRAWING_FROM_BANK, sender);
                return true;
            }
            
        },
        JSBANKS("jsb") {
            
            @Override
            public boolean handle(CommandSender sender, String... args) {
                if (args.length == 0)
                    return false;
                String subcommand = args[0];
                if (subcommand.equalsIgnoreCase("reload"))
                    JSBanksConfigurationHandler.loadBanks();
                return true;
            }
            
        };
        
        private String[] aliases;
        
        public static boolean handleCommand(Command command, CommandSender sender, String... args) {
            JSBanksCommands handler = valueOf(command.getName().toUpperCase());
            if (!handler.hasPermission(sender)) {
                Messaging.send(JSMessage.LACKS_PERMISSION, sender, handler.getPermission());
                return true;
            }
            return valueOf(command.getName().toUpperCase()).handle(sender, args);
        }
        
        JSBanksCommands(String... aliases) {
            this.aliases = aliases;
        }
        
        public String[] getAliases() {
            return this.aliases;
        }
        
        public String getPermission() {
            return JSBanks.getPlugin().getPermissionsManager().getPermission(name().toLowerCase());
        }
        
        public boolean hasPermission(CommandSender sender) {
            return JSBanks.getPlugin().getPermissionsManager().hasPermission(sender, name().toLowerCase());
        }
        
        public String getPermissionMessage() {
            return Messaging.fillArgs(JSMessage.LACKS_PERMISSION, getPermission());
        }
        
        public String getSubPermission(String node) {
            return JSBanks.getPlugin().getPermissionsManager().getPermission(name().toLowerCase() + "." + node);
        }
        
        public boolean hasSubPermission(CommandSender sender, String node) {
            return JSBanks.getPlugin().getPermissionsManager().hasPermission(sender, name().toLowerCase() + "." + node);
        }
        
        public abstract boolean handle(CommandSender sender, String... args);
        
    }
    
}
