/*
 *  Copyright 2021 ms5984 (Matt) <https://github.com/ms5984>
 *  Copyright 2021 Hempfest <https://github.com/Hempfest>
 *
 *  This file is part of Clans.
 */
package com.youtube.hempfest.clans.bank.model;

import com.youtube.hempfest.clans.util.construct.Clan;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public enum BankAction {
    BALANCE("balance"),
    DEPOSIT("deposit"),
    WITHDRAW("withdraw"),
    VIEW_LOG("view-log"),
    SET_PERM("set-perm");

    private final String label;

    BankAction(String descriptor) {
        this.label = descriptor;
    }

    public static final class AccessMap implements Serializable {
        private static final long serialVersionUID = -265409254564104601L;
        private final Map<BankAction, Integer> acl = new EnumMap<>(BankAction.class);

        public AccessMap() {
            for (BankAction value : BankAction.values()) {
                acl.computeIfAbsent(value, BankAction::getConfigDefault);
            }
        }

        private void setForClan(Clan clan) {
            BankMeta.get(clan).storeAccessMap(this);
        }

        private static AccessMap getForClan(Clan clan) {
            return BankMeta.get(clan).getAccessMap().orElseGet(() -> CompletableFuture.supplyAsync(AccessMap::new).join());
        }
    }

    public int getConfigDefault() {
        return JavaPlugin.getProvidingPlugin(getClass()).getConfig().getInt("default-access." + label, 2);
    }

    public int getValueInClan(Clan clan) {
        return AccessMap.getForClan(clan).acl.get(this);
    }

    public boolean testForPlayer(Clan clan, Player player) {
        return Clan.clanUtil.getRankPower(player) >= AccessMap.getForClan(clan).acl.get(this);
    }

    public void setRankForActionInClan(Clan clan, int rank) {
        if (rank < 0 || rank > 3) throw new IllegalArgumentException("Rank must be between 0-3");
        final AccessMap accessMap = AccessMap.getForClan(clan);
        accessMap.acl.put(this, rank);
        accessMap.setForClan(clan);
    }
}
