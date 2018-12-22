package io.github.lazoyoung;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class EconomyCommand extends CommandData implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "select":
                selectCurrency(sender, args);
                break;
            case "balance":
                balance(sender, args);
                break;
            case "deposit":
            case "withdraw":
            case "set":
                return setBalance(sender, args);
            default:
                return false;
        }
        return true;
    }
    
    private void selectCurrency(CommandSender sender, String[] args) {
        UUID id;
        String economyArg, currency = null;
        if (sender instanceof Player) {
            id = ((Player) sender).getUniqueId();
        }
        else if (sender instanceof ConsoleCommandSender) {
            id = consoleId;
        }
        else {
            sender.sendMessage("Unsupported CommandSender.");
            return;
        }
        
        try {
            economyArg = args[1].toUpperCase();
        } catch (Exception e) {
            sender.sendMessage("Missing parameter.");
            return;
        }
        try {
            currency = args[2].toLowerCase();
        } catch (Exception ignored) {
        
        }
        
        try {
            Economy economy = Economy.valueOf(economyArg);
            EconomyHandler handler = economy.getHandler();
            
            if(handler == null) {
                sender.sendMessage("That economy is not available.");
                return;
            }
            if (currency == null) {
                currencySel.put(id, new Currency(economy, handler));
            } else {
                currencySel.put(id, new Currency(economy, handler, currency));
            }
        } catch (IllegalArgumentException e) {
            sender.sendMessage(e.getMessage());
            return;
        }
        
        sender.sendMessage("Selected: " + economyArg + "/" + currency);
    }
    
    @SuppressWarnings("deprecation")
    private void balance(CommandSender sender, String[] args) {
        OfflinePlayer player = null;
        Currency c = getCurrency(sender);
        
        if (c != null) {
            if (sender instanceof Player) {
                player = ((OfflinePlayer) sender);
            } else if (args.length < 2) {
                sender.sendMessage("Please define the player.");
                return;
            }
            
            EconomyHandler eco = c.getEconomyHandler();
            if (args.length > 1) {
                player = Bukkit.getOfflinePlayer(args[1].toLowerCase());
            }
            if (player == null || !eco.hasAccount(player)) {
                sender.sendMessage("That account does not exist.");
                return;
            }
            double bal = eco.getBalance(player, c.getCurrency());
            sender.sendMessage(player.getName() + "'s balance: " + bal);
            return;
        }
        sender.sendMessage("Please select currency: /eco select <economy> [currency]");
    }
    
    @SuppressWarnings("deprecation")
    private boolean setBalance(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Missing parameter.");
            return false;
        }
        
        Currency c = getCurrency(sender);
        if (c != null) {
            double amount;
            OfflinePlayer player;
            EconomyHandler eco = c.getEconomyHandler();
            EconomyResponse result;
            
            try {
                amount = Double.parseDouble(args[2]);
                player = Bukkit.getOfflinePlayer(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("Please define the amount of balance.");
                return false;
            }
            
            switch(args[0].toLowerCase()) {
                case "deposit":
                    result = eco.deposit(player, c.getCurrency(), amount);
                    break;
                case "withdraw":
                    result = eco.withdraw(player, c.getCurrency(), amount);
                    break;
                case "set":
                    result = eco.setBalance(player, c.getCurrency(), amount);
                    break;
                default:
                    sender.sendMessage("Invalid parameter.");
                    return false;
            }
            if (result.transactionSuccess()) {
                sender.sendMessage("Transaction succeed. New balance: " + result.balance);
                return true;
            }
            sender.sendMessage("Transaction failed: " + result.errorMessage);
            return true;
        }
        sender.sendMessage("Please select currency: /eco select <economy> [currency]");
        return true;
    }
    
}
