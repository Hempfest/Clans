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

import java.util.concurrent.CompletableFuture;

public abstract class BankActionEvent extends BankEvent {

    private static final HandlerList HANDLERS = new HandlerList();
    protected final String clanId;

    protected BankActionEvent(ClanBank clanBank, String clanId) {
        super(clanBank, false);
        this.clanId = clanId;
    }

    /**
     * Get the direct clanId for this event.
     * @return clanId as String
     */
    public String getClanId() {
        return clanId;
    }

    /**
     * Get the clan associated with this bank event.
     * @return the Clan whose bank this is
     */
    public Clan getClan() {
        return CompletableFuture.supplyAsync(() -> Clan.clanUtil.getClan(clanId)).join();
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
