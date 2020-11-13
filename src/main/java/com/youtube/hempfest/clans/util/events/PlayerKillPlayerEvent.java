package com.youtube.hempfest.clans.util.events;

import com.youtube.hempfest.clans.util.StringLibrary;
import com.youtube.hempfest.clans.util.construct.Clan;
import com.youtube.hempfest.clans.util.construct.ClanUtil;
import com.youtube.hempfest.clans.util.listener.ClanEventBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class PlayerKillPlayerEvent extends ClanEventBuilder {

    private static final HandlerList handlers = new HandlerList();

    private final Player killer;

    private final Player victim;

    public PlayerKillPlayerEvent(Player p, Player target) {
        this.killer = p;
        this.victim = target;
    }

    public Player getKiller() {
        return killer;
    }

    public Player getVictim() {
        return victim;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public ClanUtil getUtil() {
        return Clan.clanUtil;
    }

    @Override
    public StringLibrary stringLibrary() {
        return new StringLibrary();
    }

    public Clan getClan(String clanID, Player clanPlayer) {
        return new Clan(clanID, clanPlayer);
    }

    public void perform() {
        ClanUtil clanUtil = getUtil();
        if (clanUtil.getClan(killer) != null) {
            Clan kill = getClan(clanUtil.getClan(killer), killer);
            if (clanUtil.getClan(victim) != null) {
                if (!clanUtil.getClan(killer).equals(clanUtil.getClan(victim))) {
                    Clan dead = getClan(clanUtil.getClan(victim), victim);
                    kill.givePower(0.11);
                    dead.takePower(0.11);
                }
            } else {
                // victim not in a clan
                kill.givePower(0.11);
            }
        }
        if (clanUtil.getClan(killer) == null) {
            if (clanUtil.getClan(victim) != null) {
                    Clan dead = getClan(clanUtil.getClan(victim), victim);
                    dead.takePower(0.11);
            }
        }
    }
}
