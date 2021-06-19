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

import com.youtube.hempfest.clans.util.construct.Clan;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

public interface BankAPI {
    /**
     * Describes a level of output logged to the console
     */
    enum LogLevel {
        SILENT, QUIET, VERBOSE
    }

    /**
     * Gets the bank associated with a clan
     * @param clan the desired clan
     * @return a Bank; if it does not exist it is created
     */
    ClanBank getBank(Clan clan);

    /**
     * Whether banks are enabled.
     *
     * @return true if banks are enabled
     */
    boolean isBankingEnabled();

    /**
     * Set the default balance of newly-created Banks
     * @return the starting balance of new banks
     */
    default BigDecimal startingBalance() {
        return BigDecimal.ZERO;
    }

    /**
     * This value reflects the maximum balance of Banks if configured.
     * Returns null if no set maximum
     * @return the maximum balance or null
     */
    default @Nullable BigDecimal maxBalance() {
        return null;
    }

    /**
     * Set the transaction logging level
     * @return a {@link LogLevel} representing desired verbosity
     */
    default LogLevel logToConsole() {
        return LogLevel.QUIET;
    }

    /**
     * Retrieve the API instance via Bukkit's ServicesManager.
     * @return BanksAPI provider
     */
    static BankAPI getInstance() {
        final RegisteredServiceProvider<BankAPI> rsp = Bukkit.getServicesManager().getRegistration(BankAPI.class);
        if (rsp == null) throw new IllegalStateException("Clans[Banks] is not loaded!");
        return rsp.getProvider();
    }
}
