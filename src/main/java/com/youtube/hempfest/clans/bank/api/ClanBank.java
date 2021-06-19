/*
 *  This file is part of Clans.
 *
 *  Copyright 2021 ms5984 (Matt) <https://github.com/ms5984>
 *  Copyright 2021 Hempfest <https://github.com/Hempfest>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.youtube.hempfest.clans.bank.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

/**
 * The public API for Clan banks.
 */
public interface ClanBank {
    /**
     * Take an amount from the player and deposit into the bank.
     *
     * @param player player to take amount from
     * @param amount amount to deposit
     * @return true if successful
     */
    boolean deposit(Player player, BigDecimal amount);

    /**
     * Withdraw an amount from the bank and give to the player.
     *
     * @param player player to give amount to
     * @param amount amount to withdraw
     * @return true if successful
     */
    boolean withdraw(Player player, BigDecimal amount);

    /**
     * Check if the bank has an amount.
     *
     * @param amount amount to test
     * @return true if the bank has at least amount
     */
    boolean has(BigDecimal amount);

    /**
     * Get the balance of the bank.
     *
     * @return balance as double
     */
    double getBalanceDouble();

    /**
     * Get the balance of the bank.
     *
     * @return balance as BigDecimal
     */
    @NotNull BigDecimal getBalance();

    /**
     * Set the balance of the bank.
     *
     * @param newBalance the desired balance as a double
     * @throws IllegalArgumentException if desired balance is negative
     */
    default void setBalanceDouble(double newBalance) {
        if (newBalance < 0d) throw new IllegalArgumentException();
    }

    /**
     * Set the balance of the bank.
     *
     * @param newBalance the desired balance as BigDecimal
     * @throws IllegalArgumentException if desired balance is negative
     */
    default void setBalance(BigDecimal newBalance) {
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException();
    }
}
