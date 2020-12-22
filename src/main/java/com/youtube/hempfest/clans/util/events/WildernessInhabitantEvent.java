package com.youtube.hempfest.clans.util.events;

import com.youtube.hempfest.clans.HempfestClans;
import com.youtube.hempfest.clans.util.StringLibrary;
import com.youtube.hempfest.clans.util.construct.Claim;
import com.youtube.hempfest.clans.util.construct.ClaimUtil;
import com.youtube.hempfest.clans.util.construct.Clan;
import com.youtube.hempfest.clans.util.construct.ClanUtil;
import com.youtube.hempfest.clans.util.construct.Resident;
import com.youtube.hempfest.clans.util.data.DataManager;
import com.youtube.hempfest.clans.util.listener.AsyncClanEventBuilder;
import java.util.HashMap;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class WildernessInhabitantEvent extends AsyncClanEventBuilder implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private final HashMap<String, String> titleContext = new HashMap<>();

	private final Player p;

	private boolean titlesAllowed = DataManager.titlesAllowed();

	private boolean cancelled;

	public WildernessInhabitantEvent(Player p, boolean isAsync) {
		super(isAsync);
		this.p = p;
		if (HempfestClans.residents.stream().anyMatch(r -> r.getPlayer().getName().equals(p.getName()))) {
			for (Resident res : HempfestClans.residents) {
				if (res.getPlayer().getName().equals(p.getName())) {
					HempfestClans.residents.remove(res);
					break;
				}
			}
		}
	}

	{
		titleContext.put("W-TITLE", "&4&nWilderness");
		titleContext.put("W-SUB-TITLE", "&7&oOwned by no-one.");
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean b) {
		this.cancelled = b;
	}

	public void setTitlesAllowed(boolean b) {
		this.titlesAllowed = b;
	}

	public void setWildernessTitle(String title, String subtitle) {
		titleContext.put("W-TITLE", title);
		titleContext.put("W-SUB-TITLE", subtitle);
	}

	public String getWildernessTitle() {
		return titleContext.get("W-TITLE");
	}

	public String getWildernessSubTitle() {
		return titleContext.get("W-SUB-TITLE");
	}

	public Claim getClaim() {
		return null;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public Player getPlayer() {
		return p;
	}

	public boolean isTitlesAllowed() {
		return titlesAllowed;
	}

	public ClanUtil getUtil() {
		return Clan.clanUtil;
	}

	@Override
	public StringLibrary stringLibrary() {
		return new StringLibrary();
	}

	public ClaimUtil getClaimUtil() {
		return Claim.claimUtil;
	}

	public void handleUpdate() {
			if (!HempfestClans.wildernessInhabitants.contains(p)) {
				if (titlesAllowed) {
					p.sendTitle(getClaimUtil().color(titleContext.get("W-TITLE")), getClaimUtil().color(titleContext.get("W-SUB-TITLE")), 10, 25, 10);
				}
				getClaimUtil().sendMessage(p, "Now entering &4&nWilderness");
                HempfestClans.wildernessInhabitants.add(p);
			}
	}

}
