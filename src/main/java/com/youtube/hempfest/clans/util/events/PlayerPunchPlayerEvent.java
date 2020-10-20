package com.youtube.hempfest.clans.util.events;

import com.youtube.hempfest.clans.util.construct.ClanUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerPunchPlayerEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player attacker;

    private final Player victim;

    private boolean cancelled;

    public PlayerPunchPlayerEvent(Player p, Player target) {
        this.attacker = p;
        this.victim = target;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public HandlerList getHandlerList() {
        return handlers;
    }

    public ClanUtil getUtil() {
        return new ClanUtil();
    }

    public void perform() {
        ClanUtil clanUtil = getUtil();
        if (clanUtil.getClan(attacker) != null) {
            if (clanUtil.getClan(victim) != null) {
                if (clanUtil.getClan(attacker).equals(clanUtil.getClan(victim))) {
                    setCanHurt(false);
                    clanUtil.sendMessage(attacker, "&c&oYou cannot hurt allies!");
                    return;
                }
                if (clanUtil.getAllies(clanUtil.getClan(attacker)).contains(clanUtil.getClan(victim))) {
                    setCanHurt(false);
                    clanUtil.sendMessage(attacker, "&c&oYou cannot hurt allies!");
                    return;
                }
            }
        }
    }

    public boolean canHurt() {
        return cancelled;
    }

    public void setCanHurt(boolean b) {
        if (b == true)
            this.cancelled = false;
        if (b == false)
            this.cancelled = true;
    }


}
