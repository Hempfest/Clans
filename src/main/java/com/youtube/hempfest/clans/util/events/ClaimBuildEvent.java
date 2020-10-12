package com.youtube.hempfest.clans.util.events;

import com.youtube.hempfest.clans.util.StringLibrary;
import com.youtube.hempfest.clans.util.construct.Claim;
import com.youtube.hempfest.clans.util.construct.ClaimUtil;
import com.youtube.hempfest.clans.util.construct.ClanUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ClaimBuildEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player p;
    private final Location location;

    private boolean cancelled;

    public ClaimBuildEvent(Player p, Location location) {
        this.p = p;
        this.location = location;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public HandlerList getHandlerList() {
        return handlers;
    }

    public void handleCheck() {
        ClaimUtil claimUtil = new ClaimUtil();
        if (claimUtil.isInClaim(location)) {
            Claim claim = new Claim(claimUtil.getClaimID(location), p);
            ClanUtil clanUtil = new ClanUtil();
            if (clanUtil.getClan(p) != null) {
                if (!claim.getOwner().equals(clanUtil.getClan(p))) {
                    if (!clanUtil.getAllies(claim.getOwner()).contains(clanUtil.getClan(p))) {
                        setCancelled(true);
                        StringLibrary stringLibrary = new StringLibrary();
                        stringLibrary.sendMessage(p, "&c&oYou cannot do this here, land owned by: " + clanUtil.clanRelationColor(clanUtil.getClan(p), claim.getOwner()) + clanUtil.getClanTag(claim.getOwner()));
                    }
                }
            } else {
                setCancelled(true);
                StringLibrary stringLibrary = new StringLibrary();
                stringLibrary.sendMessage(p, "&c&oYou cannot do this here, land owned by: &e&o&n" + clanUtil.getClanTag(claim.getOwner()));
            }
        }
    }

}
