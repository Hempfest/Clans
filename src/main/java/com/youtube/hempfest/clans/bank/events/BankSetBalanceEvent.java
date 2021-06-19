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
package com.youtube.hempfest.clans.bank.events;

import com.youtube.hempfest.clans.bank.api.ClanBank;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public final class BankSetBalanceEvent extends BankActionEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private final BigDecimal newBalance;
    private boolean cancelled = false;

    public BankSetBalanceEvent(ClanBank clanBank, String clanId, @NotNull BigDecimal newBalance) {
        super(clanBank, clanId);
        this.newBalance = newBalance;
    }

    /**
     * Get the potential new balance.
     *
     * @return the desired balance as a BigDecimal
     */
    public @NotNull BigDecimal getNewBalance() {
        return newBalance;
    }

    /**
     * Get the potential new balance.
     *
     * @return the desired balance as a double
     */
    public double getNewBalanceAsDouble() {
        return newBalance.doubleValue();
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
