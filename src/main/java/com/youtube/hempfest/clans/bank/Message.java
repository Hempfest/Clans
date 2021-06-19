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
 *  distributed under the License is distributed on an "AS IS" BASIS(""),
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.youtube.hempfest.clans.bank;

import com.youtube.hempfest.clans.util.ProvidedMessage;
import org.jetbrains.annotations.Nullable;

public enum Message implements ProvidedMessage {
    // Clans-related messages
    CLANS_HELP_PREFIX("&7|&e)"),
    NOT_ON_CLAN_LAND("You are not on clan territory!"),
    PLAYER_NO_CLAN("&cYou do not belong to a clan."),
    // Banks-related messages
    BANKS_HEADER("&fBanks"),
    HELP_PREFIX("&6/clan &fbank"),
    HELP_AMOUNT_COMMANDS("&f<&adeposit&7,&cwithdraw&f> <&7{amount}&f>"),
    CURRENT_BALANCE("&bCurrent bank balance"),
    COMMAND_LISTING("&6Commands:"),
    USAGE("&6Usage:"),
    GREETING("&bWelcome, {0}&b."), // {0} = Player name
    GREETING_HOVER("&6In clan: &f{0}\\n&aClick to get balance"), // {0} = clan tag
    INVALID_SUBCOMMAND("&cInvalid subcommand!"),
    INVALID_AMOUNT("&cInvalid number!"),
    HOVER_BALANCE("Get the current bank balance"),
    HOVER_DEPOSIT("Deposit money into the clan bank"),
    HOVER_WITHDRAW("Withdraw money from the clan bank"),
    HOVER_VIEW_LOG("View recent transaction history"),
    HOVER_SET_PERM("Set access to bank functions"),
    HOVER_NO_AMOUNT("No amount given!"),
    VALID_OPTIONS("Valid options:"),
    AMOUNT("amount"),
    PERM("perm"),
    LEVEL("level"),
    VALID_LEVELS("Valid levels: [0-3]"),
    INVALID_LEVEL("&7Invalid level!"),
    SETTING_LEVEL("&7Setting &6{0} &7level to &a{1}"), // {0} = action, {1} = level
    DEPOSIT_MESSAGE_PLAYER("&7You deposited &a{0} &7into the clan bank."), // {0} = amount
    DEPOSIT_MESSAGE_ANNOUNCE("{0} &6added &a{1} &6to the clan bank!"), // {0} = player name, {1} = amount
    DEPOSIT_ERROR_PLAYER("&cUnable to deposit {0}"), // {0} = amount
    WITHDRAW_MESSAGE_PLAYER("&7You withdrew &c{0} &7from the clan bank."), // {0} = amount
    WITHDRAW_MESSAGE_ANNOUNCE("{0} &4took &c{1} &4from the clan bank!"), // {0} = player name, {1} = amount
    WITHDRAW_ERROR_PLAYER("&cUnable to withdraw {0}"), // {0} = amount
    // Banks event-related messages
    //for the following: {0} = success, {1} = name, {2} = amount, {3} = clan tag
    TRANSACTION_DEPOSIT_PRE(">>PRE [{0}]: {1} to deposit {2} with clan {3}"),
    TRANSACTION_DEPOSIT_PRE_CANCELLED(">>PRE-Cancelled [{0}]: {1} to deposit {2} with clan {3}"),
    TRANSACTION_WITHDRAW_PRE(">>PRE [{0}]: {1} to withdraw {2} from clan {3}"),
    TRANSACTION_WITHDRAW_PRE_CANCELLED(">>PRE-Cancelled [{0}]: {1} to withdraw {2} from clan {3}"),
    TRANSACTION_DEPOSIT("Transaction [{0}]: {1} deposited {2} with clan {3}"),
    TRANSACTION_WITHDRAW("Transaction [{0}]: {1} withdrew {2} from clan {3}"),
    TRANSACTION_VERBOSE_CLAN_ID("clanId={0}"), // {0} = clan id
    TRANSACTION_SUCCESS("SUCCESS"),
    TRANSACTION_FAILED("FAILED"),
    PRETRANSACTION_PENDING("PENDING"),
    PRETRANSACTION_FAILURE("DENIED"),
    // No permission messages
    NO_PERM_PLAYER_COMMAND("&cYou do not have permission to run this command."),
    NO_PERM_PLAYER_ACTION("&cYou do not have permission to perform this action.");

    private final String text;

    Message(String text) {
        this.text = text;
    }

    @Override
    public @Nullable String get() {
        return text;
    }
}
