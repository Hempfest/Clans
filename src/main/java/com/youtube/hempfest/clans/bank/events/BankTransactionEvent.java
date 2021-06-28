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
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public class BankTransactionEvent extends BankActionEvent {

    private static final HandlerList HANDLERS = new HandlerList();
    public enum Type {
        DEPOSIT, WITHDRAWAL
    }

    protected final Player player;
    protected final BigDecimal amount;
    protected final boolean success;
    protected final Type type;

    public BankTransactionEvent(Player player, ClanBank clanBank, BigDecimal amount, String clanId, boolean success, Type type) {
        super(clanBank, clanId);
        this.player = player;
        this.amount = amount;
        this.success = success;
        this.type = type;
    }
    public BankTransactionEvent(BankTransactionEvent event) {
        super(event.clanBank, event.clanId);
        this.player = event.player;
        this.amount = event.amount;
        this.success = event.success;
        this.type = event.type;
    }

    /**
     * Get the player associated with this transaction.
     * @return Player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the ClanBank associated with this transaction.
     * @return the ClanBank
     */
    @Override
    public ClanBank getClanBank() {
        return super.getClanBank();
    }

    /**
     * Get the amount involved with this transaction.
     * @return a BigDecimal amount
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Denotes whether or not the transaction was successful.
     * @return true if successful
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * A transaction may constitute a deposit or withdrawal.
     * @return {@link Type} representing this transaction
     */
    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        switch (type) {
            case DEPOSIT:
                return Message.TRANSACTION_DEPOSIT.toString()
                        .replace("{0}", this.success ? Message.TRANSACTION_SUCCESS.toString() : Message.TRANSACTION_FAILED.toString())
                        .replace("{1}", player.getName())
                        .replace("{2}", amount.toString())
                        .replace("{3}", getClan().getClanTag());
            case WITHDRAWAL:
                return Message.TRANSACTION_WITHDRAW.toString()
                        .replace("{0}", this.success ? Message.TRANSACTION_SUCCESS.toString() : Message.TRANSACTION_FAILED.toString())
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
