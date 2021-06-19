/*
 *  Copyright 2021 ms5984 (Matt) <https://github.com/ms5984>
 *  Copyright 2021 Hempfest <https://github.com/Hempfest>
 *
 *  This file is part of Clans.
 */
package com.youtube.hempfest.clans.bank.model;

import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.youtube.hempfest.clans.HempfestClans;
import com.youtube.hempfest.clans.bank.api.ClanBank;
import com.youtube.hempfest.clans.bank.events.BankPreTransactionEvent;
import com.youtube.hempfest.clans.bank.events.BankSetBalanceEvent;
import com.youtube.hempfest.clans.bank.events.BankTransactionEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;

public final class Bank implements ClanBank, Serializable {
    private static final long serialVersionUID = -5283828168295980464L;
    protected static final PluginManager PM = Bukkit.getServer().getPluginManager();
    protected final EconomyProvision eco = EconomyProvision.getInstance();
    protected BigDecimal balance;
    protected boolean enabled;
    protected final String clanId;

    public Bank(@NotNull String clanId) throws IllegalStateException {
        try {
            //noinspection ConstantConditions
            this.balance = HempfestClans.getBankAPI().startingBalance();
        } catch (NullPointerException e) {
            throw new IllegalStateException("Banking is not enabled!", e);
        }
        this.enabled = true;
        this.clanId = clanId;
    }

    @Override
    public boolean deposit(Player player, BigDecimal amount) {
        if (!enabled) return false;
        if (amount.signum() != 1) return false;
        final boolean has = eco.has(amount, player, player.getWorld().getName())
                .orElseThrow(IllegalStateException::new);
        final BankPreTransactionEvent event = new BankPreTransactionEvent(player, this, amount, clanId,
                has, BankTransactionEvent.Type.DEPOSIT);
        PM.callEvent(event);
        return event.isSuccess();
    }

    @Override
    public boolean withdraw(Player player, BigDecimal amount) {
        if (!enabled) return false;
        if (amount.signum() != 1) return false;
        final BankPreTransactionEvent event = new BankPreTransactionEvent(player, this, amount, clanId,
                has(amount), BankTransactionEvent.Type.WITHDRAWAL);
        PM.callEvent(event);
        return event.isSuccess();
    }

    @Override
    public boolean has(BigDecimal amount) {
        return balance.compareTo(amount) >= 0;
    }

    @Override
    public double getBalanceDouble() {
        return balance.doubleValue();
    }

    @Override
    public @NotNull BigDecimal getBalance() {
        return balance;
    }

    @Override
    public void setBalanceDouble(double newBalance) {
        ClanBank.super.setBalanceDouble(newBalance);
        PM.callEvent(new BankSetBalanceEvent(this, clanId, BigDecimal.valueOf(newBalance)));
    }

    @Override
    public void setBalance(BigDecimal newBalance) {
        ClanBank.super.setBalance(newBalance);
        PM.callEvent(new BankSetBalanceEvent(this, clanId, newBalance));
    }
}
