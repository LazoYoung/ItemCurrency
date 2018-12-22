package io.github.lazoyoung;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import javax.annotation.Nullable;

public interface EconomyHandler {
    
    double getBalance(OfflinePlayer player, @Nullable String currency);
    boolean hasAccount(OfflinePlayer player);
    EconomyResponse deposit(OfflinePlayer player, @Nullable String currency, double amount);
    EconomyResponse withdraw(OfflinePlayer player, @Nullable String currency, double amount);

}
