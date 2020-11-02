package com.youtube.hempfest.clans.util.events;

import com.youtube.hempfest.clans.HempfestClans;
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

    public ClaimUtil getClaimUtil() {
        return HempfestClans.getInstance().claimUtil;
    }

    public ClanUtil getUtil() {
        return HempfestClans.getInstance().clanUtil;
    }

    public void handleCheck() {
        if (getClaimUtil().isInClaim(location)) {
            Claim claim = new Claim(getClaimUtil().getClaimID(location), p);
            if (getUtil().getClan(p) != null) {
                if (!claim.getOwner().equals(getUtil().getClan(p))) {
                    if (!getUtil().getAllies(claim.getOwner()).contains(getUtil().getClan(p))) {
                        setCancelled(true);
                        StringLibrary stringLibrary = new StringLibrary();
                        stringLibrary.sendMessage(p, "&c&oYou cannot do this here, land owned by: " + getUtil().clanRelationColor(getUtil().getClan(p), claim.getOwner()) + getUtil().getClanTag(claim.getOwner()));
                    }
                }
            } else {
                setCancelled(true);
                StringLibrary stringLibrary = new StringLibrary();
                stringLibrary.sendMessage(p, "&c&oYou cannot do this here, land owned by: &e&o&n" + getUtil().getClanTag(claim.getOwner()));
            }
        }
    }

}
