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
import com.youtube.hempfest.clans.util.construct.Clan;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public final class AsyncNewBankEvent extends BankEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Clan clan;
    private final BigDecimal startingBalance;

    public AsyncNewBankEvent(Clan clan, ClanBank clanBank) {
        super(clanBank, true);
        this.clan = clan;
        this.startingBalance = clanBank.getBalance();
    }

    /**
     * Get the clan whose bank was just created
     * @return the Clan
     */
    public Clan getClan() {
        return clan;
    }

    /**
     * Get the starting balance of the bank (usually 0 per default configuration).
     * @return the initial balance
     */
    public BigDecimal getStartingBalance() {
        return startingBalance;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
