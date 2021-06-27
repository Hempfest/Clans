/*
 *  Copyright 2021 ms5984 (Matt) <https://github.com/ms5984>
 *  Copyright 2021 Hempfest <https://github.com/Hempfest>
 *
 *  This file is part of Clans.
 */
package com.youtube.hempfest.clans.util.listener;

import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.youtube.hempfest.clans.bank.Message;
import com.youtube.hempfest.clans.bank.api.BankAPI;
import com.youtube.hempfest.clans.bank.events.BankPreTransactionEvent;
import com.youtube.hempfest.clans.bank.events.BankSetBalanceEvent;
import com.youtube.hempfest.clans.bank.events.BankTransactionEvent;
import com.youtube.hempfest.clans.bank.model.Bank;
import com.youtube.hempfest.clans.bank.model.BankLog;
import com.youtube.hempfest.clans.bank.model.BankMeta;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;
import java.util.Optional;

public class BankListener implements Listener {
    private final JavaPlugin plugin = JavaPlugin.getProvidingPlugin(getClass());
    private final PluginManager pm = plugin.getServer().getPluginManager();
    private final EconomyProvision eco = EconomyProvision.getInstance();
    private final BankAPI api = BankAPI.getInstance();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPreTransactionLog(BankPreTransactionEvent e) {
        // Log to console if needed
        switch (api.logToConsole()) {
            case SILENT:
                return;
            case QUIET:
                if (e.isCancelled()) plugin.getLogger().info(e::toString);
                return;
            case VERBOSE:
                plugin.getLogger().info(() -> e.toString() + " " +
                        Message.TRANSACTION_VERBOSE_CLAN_ID
                                .replace(e.getClanId())
                );
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTransactionLogAndSave(BankTransactionEvent e) {
        // Log to console if needed
        switch (api.logToConsole()) {
            case SILENT:
                break;
            case QUIET:
                plugin.getLogger().info(e::toString);
                break;
            case VERBOSE:
                plugin.getLogger().info(() -> e.toString() + " " +
                        Message.TRANSACTION_VERBOSE_CLAN_ID
                                .replace(e.getClanId()));
        }
        // Add to in-game log
        BankLog.getForClan(e.getClan()).addTransaction(e);
        // Save our object
        if (!(e.getClanBank() instanceof Bank)) return;
        BankMeta.get(e.getClan()).storeBank((Bank) e.getClanBank());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDeposit(BankPreTransactionEvent e) {
        if (e.getType() != BankTransactionEvent.Type.DEPOSIT) return;
        if (!(e.getClanBank() instanceof Bank)) return;
        if (!e.isSuccess()) {
            e.setCancelled(true);
            return; // The player didn't have enough money or is not allowed, no transaction
        }
        final Bank bank = (Bank) e.getClanBank();
        final BigDecimal maxBalance = api.maxBalance();
        final BigDecimal amount = e.getAmount();
        if (maxBalance != null) {
            // If the bank balance plus the deposit amount is greater than max balance,
            // cancel the transaction
            if (bank.getBalance().add(amount).compareTo(maxBalance) > 0) {
                e.setCancelled(true);
                return;
            }
        }
        final Player player = e.getPlayer();
        final boolean success = eco.withdraw(amount, player).orElse(false);
        if (success) bank._setBalanceInternal(bank.getBalance().add(amount));
        if (!success) e.setSuccess(false);
        pm.callEvent(
                new BankTransactionEvent(
                        player,
                        bank,
                        amount,
                        bank._getClanIdInternal(),
                        success,
                        BankTransactionEvent.Type.DEPOSIT
                )
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onWithdrawal(BankPreTransactionEvent e) {
        if (e.getType() != BankTransactionEvent.Type.WITHDRAWAL) return;
        if (!(e.getClanBank() instanceof Bank)) return;
        if (!e.isSuccess()) {
            e.setCancelled(true);
            return; // The player didn't have enough money or is not allowed, no transaction
        }
        final Bank bank = (Bank) e.getClanBank();
        final Player player = e.getPlayer();
        final BigDecimal amount = e.getAmount();
        final boolean success = eco.deposit(amount, player).orElse(false);
        if (success) bank._setBalanceInternal(bank.getBalance().subtract(amount));
        if (!success) e.setSuccess(false);
        pm.callEvent(
                new BankTransactionEvent(
                        player,
                        bank,
                        amount,
                        bank._getClanIdInternal(),
                        success,
                        BankTransactionEvent.Type.WITHDRAWAL
                )
        );
    }

    @EventHandler(ignoreCancelled = true)
    public void onSetBalance(BankSetBalanceEvent e) {
        // Cancel event if new balance exceeds max balance
        Optional.ofNullable(api.maxBalance()).ifPresent(maxBalance -> {
            if (e.getNewBalance().compareTo(maxBalance) > 0) {
                e.setCancelled(true);
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSetBalanceSave(BankSetBalanceEvent e) {
        if (!(e.getClanBank() instanceof Bank)) return;
        // Update our stored object
        final Bank bank = (Bank) e.getClanBank();
        bank._setBalanceInternal(e.getNewBalance());
        BankMeta.get(e.getClan()).storeBank(bank);
    }
}
