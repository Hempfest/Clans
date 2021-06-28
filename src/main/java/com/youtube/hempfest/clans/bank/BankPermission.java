/*
 *  Copyright 2021 ms5984 (Matt) <https://github.com/ms5984>
 *  Copyright 2021 Hempfest <https://github.com/Hempfest>
 *
 *  This file is part of Clans.
 */
package com.youtube.hempfest.clans.bank;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;

public enum BankPermission {
    STAR("clans.banks.*"),
    USE("clans.banks.use"),
    USE_STAR("clans.banks.use.*"),
    USE_BALANCE("clans.banks.use.balance"),
    USE_DEPOSIT("clans.banks.use.deposit"),
    USE_WITHDRAW("clans.banks.use.withdraw");

    private static boolean init;
    public final String node;
    private Permission permission;

    BankPermission(String s) {
        this.node = s;
    }

    public boolean check(CommandSender commandSender) {
        return commandSender.hasPermission(node);
    }

    /**
     * Setup permission nodes.
     *
     * @param pm plugin manager object
     * @throws IllegalStateException if permissions already added
     */
    public static void setup(PluginManager pm) throws IllegalStateException {
        if (init) throw new IllegalStateException("Permissions already added!");
        // setup nodes
        USE_BALANCE.permission = new Permission(USE_BALANCE.node);
        USE_DEPOSIT.permission = new Permission(USE_DEPOSIT.node);
        USE_WITHDRAW.permission = new Permission(USE_WITHDRAW.node);
        USE.permission = new Permission(USE.node);
        USE_BALANCE.permission.addParent(USE.permission, true);
        USE_STAR.permission = new Permission(USE_STAR.node);
        USE.permission.addParent(USE_STAR.permission, true);
        USE_DEPOSIT.permission.addParent(USE_STAR.permission, true);
        USE_WITHDRAW.permission.addParent(USE_STAR.permission, true);
        STAR.permission = new Permission(STAR.node);
        USE_STAR.permission.addParent(STAR.permission, true);
        // add nodes
        pm.addPermission(STAR.permission);
        pm.addPermission(USE_STAR.permission);
        pm.addPermission(USE.permission);
        pm.addPermission(USE_DEPOSIT.permission);
        pm.addPermission(USE_WITHDRAW.permission);
        pm.addPermission(USE_BALANCE.permission);
        init = true;
    }
}
