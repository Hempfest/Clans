package com.youtube.hempfest.clans.util.events;

import com.github.sanctum.labyrinth.library.TextLib;
import com.youtube.hempfest.clans.HempfestClans;
import com.youtube.hempfest.clans.util.StringLibrary;
import com.youtube.hempfest.clans.util.construct.Claim;
import com.youtube.hempfest.clans.util.construct.ClaimUtil;
import com.youtube.hempfest.clans.util.construct.Clan;
import com.youtube.hempfest.clans.util.construct.ClanUtil;
import com.youtube.hempfest.clans.util.construct.Resident;
import com.youtube.hempfest.clans.util.data.DataManager;
import com.youtube.hempfest.clans.util.listener.ClanEventBuilder;
import java.util.HashMap;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class ClaimResidentEvent extends ClanEventBuilder implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private final HashMap<String, String> titleContext = new HashMap<>();

	private final Player p;

	private final Resident r;

	private final Claim claim;

	private boolean titlesAllowed = DataManager.titlesAllowed();

	private boolean cancelled;

	public ClaimResidentEvent(Player p) {
		this.p = p;
		this.claim = Claim.from(p.getLocation());
		if (HempfestClans.residents.stream().noneMatch(r -> r.getPlayer().getName().equals(p.getName()))) {
			Resident res = new Resident(p);
			res.setClaim(this.claim);
			r = res;
			HempfestClans.residents.add(r);
		} else {
			r = HempfestClans.residents.stream().filter(r -> r.getPlayer().getName().equals(p.getName())).findFirst().orElse(null);
		}
	}

	{

		titleContext.put("TITLE", "&3&oClaimed land");
		titleContext.put("SUB-TITLE", "&7Owned by: &b%s");

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

	public void setClaimTitle(String title, String subtitle) {
		titleContext.put("TITLE", title);
		titleContext.put("SUB-TITLE", subtitle);
	}

	public String getClaimTitle() {
		return titleContext.get("TITLE");
	}

	public String getClaimSubTitle() {
		return titleContext.get("SUB-TITLE");
	}

	public Claim getClaim() {
		return claim;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public boolean isTitleAllowed() {
		return titlesAllowed;
	}

	public Resident getResident() {
		return r;
	}

	public ClanUtil getUtil() {
		return Clan.clanUtil;
	}

	@Override
	public StringLibrary stringLibrary() {
		return Clan.clanUtil;
	}

	public ClaimUtil getClaimUtil() {
		return Claim.claimUtil;
	}

	public void playTitle() {
		String clanName = getUtil().getClanTag(getClaim().getOwner());
		String color = "";
		if (getUtil().getClan(p) != null) {
			color = getUtil().clanRelationColor(getUtil().getClan(p), getClaim().getOwner());
		} else {
			color = "&f&o";
		}
		if (titlesAllowed) {
			p.sendTitle(getClaimUtil().color(String.format(titleContext.get("TITLE"), clanName)), getClaimUtil().color(String.format(titleContext.get("SUB-TITLE"), color + clanName)), 10, 25, 10);
		}
		getClaimUtil().sendComponent(p, TextLib.getInstance().textHoverable(getClaimUtil().getPrefix() + " Now entering &a" + color + clanName + "'s&7 land @ ", "&f(&eHover&f)", "&eX:" + color + getClaim().getLocation().getChunk().getX() + " &eZ:" + color + getClaim().getLocation().getChunk().getZ()));
	}

	public void handleUpdate() {
		HempfestClans.wildernessInhabitants.remove(p);
		if (!getClaim().getClaimID().equals(r.getClaim().getClaimID())) {
			if (r.isNotificationSent()) {
				if (!r.getClaim().getOwner().equals(r.getAccurateClaim().getOwner())) {
					this.r.setNotificationSent(false);
					if (HempfestClans.clanManager(p) != null) {
						if (r.getClaim().getOwner().equals(HempfestClans.clanManager(p).getClanID())) {
							this.r.setTraversedDifferent(true);
						}
					}
					this.r.setClaim(this.claim);
					HempfestClans.residents.remove(this.r);
					HempfestClans.residents.add(this.r);
				}
			}
		}
		if (!this.r.isNotificationSent()) {
			playTitle();
			this.r.setNotificationSent(true);
		} else {
			if (this.r.hasTraversedDifferent()) {
				if (HempfestClans.clanManager(p) == null) {
					this.r.setTraversedDifferent(false);
					this.r.setNotificationSent(false);
					HempfestClans.residents.remove(this.r);
					HempfestClans.residents.add(this.r);
				}
			}
		}
	}

}
