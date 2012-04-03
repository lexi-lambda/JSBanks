package com.imjake9.server.banks.utils;

import com.imjake9.server.lib.MessageTemplate;
import com.imjake9.server.lib.Messaging;
import com.imjake9.server.lib.Messaging.MessageLevel;
import java.util.logging.Logger;

public class JSBMessaging {
    
    private static final Logger log = Logger.getLogger("Minecraft");
    
    /**
     * Logs a message to the console with the INFO and JSB prefixes.
     * 
     * @param message 
     */
    public static void info(String message) {
        log.info("[JSBanks] " + message);
    }
    
    /**
     * Logs a message to the console with the WARNING and JSB prefixes.
     * 
     * @param message 
     */
    public static void warning(String message) {
        log.warning("[JSBanks] " + message);
    }
    
    /**
     * Logs a message to the console with the SEVERE and JSB prefixes.
     * 
     * @param message 
     */
    public static void severe(String message) {
        log.severe("[JSBanks] " + message);
    }
    
    public static enum JSBMessage implements MessageTemplate {
        
        ADJACENT_TO_CHEST (MessageLevel.ERROR, "You cannot place a chest next to a bank."),
        CHEST_ALREADY_REGISTERED (MessageLevel.ERROR, "That chest is already registered as a bank."),
        CHEST_NOT_EMPTY (MessageLevel.ERROR, "A chest must be empty to modify it."),
        CHEST_NOT_REGISTERED (MessageLevel.ERROR, "That chest is not a bank."),
        DEPOSITING_TO_BANK (MessageLevel.INIT, "Left-click a bank to deposit."),
        DEPOSITED_TO_BANK (MessageLevel.COMPLETE, "Deposited %1 into bank."),
        NEW_BALANCE (MessageLevel.PLAIN, "<dark_red>Bank Balance</dark_red>: <aqua>%1</aqua>"),
        NOT_ENOUGH_TO_DEPOSIT (MessageLevel.ERROR, "You don't that much money to deposit."),
        NOT_ENOUGH_TO_WITHDRAW (MessageLevel.ERROR, "That bank doesn't contain that much money."),
        PRIVATE_BANK (MessageLevel.ERROR, "That private bank is owned by another player."),
        REGISTERING_BANK (MessageLevel.INIT, "Left-click a chest block to register a bank."),
        REGISTRATION_CANCELED (MessageLevel.COMPLETE, "Bank registration canceled."),
        REGISTRATION_COMPLETE (MessageLevel.COMPLETE, "Bank registered successfully."),
        TRANSACTION_CANCELED (MessageLevel.COMPLETE, "Transaction canceled."),
        UNREGISTERING_BANK (MessageLevel.INIT, "Left-click a bank to unregister it."),
        UNREGISTRATION_CANCELED (MessageLevel.COMPLETE, "You must select a bank chest."),
        UNREGISTRATION_COMPLETE (MessageLevel.COMPLETE, "Bank unregistered successfully."),
        WITHDRAWING_FROM_BANK (MessageLevel.INIT, "Left-click a bank to withdraw."),
        WITHDREW_FROM_BANK (MessageLevel.COMPLETE, "Withdrew %1 from bank.");
        
        private MessageLevel level;
        private String format;
        
        JSBMessage(MessageLevel level, String format) {
            this.level = level;
            this.format = Messaging.parseStyling(level.getOpeningTag() + format + level.getClosingTag());
        }
        
        JSBMessage(String format) {
            this(MessageLevel.NORMAL, format);
        }

        @Override
        public String getMessage() {
            return this.format;
        }

        @Override
        public MessageLevel getLevel() {
            return this.level;
        }
        
    }
    
}