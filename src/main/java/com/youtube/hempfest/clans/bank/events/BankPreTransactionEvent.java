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

import com.youtube.hempfest.clans.bank.Message;
import com.youtube.hempfest.clans.bank.api.ClanBank;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public final class BankPreTransactionEvent extends BankTransactionEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean success;
    private boolean cancelled;

    public BankPreTransactionEvent(Player player, ClanBank clanBank, BigDecimal amount, String clanId, boolean success, Type type) {
        super(player, clanBank, amount, clanId, success, type);
        this.success = success;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
        if (cancel && success) { // don't flip cancel, this is meant to set success to false on cancel = true
            success = false;
        }
    }

    @Override
    public boolean isSuccess() {
        return this.success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        switch (type) {
            case DEPOSIT:
                return (cancelled ? Message.TRANSACTION_DEPOSIT_PRE_CANCELLED : Message.TRANSACTION_DEPOSIT_PRE).toString()
                        .replace("{0}", (success ? Message.PRETRANSACTION_PENDING.toString() : Message.PRETRANSACTION_FAILURE.toString()))
                        .replace("{1}", player.getName())
                        .replace("{2}", amount.toString())
                        .replace("{3}", getClan().getClanTag());
            case WITHDRAWAL:
                return (cancelled ? Message.TRANSACTION_WITHDRAW_PRE_CANCELLED : Message.TRANSACTION_WITHDRAW_PRE).toString()
                        .replace("{0}", (success ? Message.PRETRANSACTION_PENDING.toString() : Message.PRETRANSACTION_FAILURE.toString()))
                        .replace("{1}", player.getName())
                        .replace("{2}", amount.toString())
                        .replace("{3}", getClan().getClanTag());
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
