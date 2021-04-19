package com.youtube.hempfest.clans.util.events;

import com.youtube.hempfest.clans.HempfestClans;
import com.youtube.hempfest.clans.util.StringLibrary;
import com.youtube.hempfest.clans.util.construct.Claim;
import com.youtube.hempfest.clans.util.construct.ClaimUtil;
import com.youtube.hempfest.clans.util.construct.Clan;
import com.youtube.hempfest.clans.util.construct.ClanUtil;
import com.youtube.hempfest.clans.util.construct.Resident;
import com.youtube.hempfest.clans.util.data.DataManager;
import com.youtube.hempfest.clans.util.listener.ClanEventBuilder;
import java.util.Arrays;
import java.util.HashMap;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class ClaimResidentEvent extends ClanEventBuilder implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private final HashMap<String, String> titleContext = new HashMap<>();

	private final Player p;

	private Resident r;

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
		getClaimUtil().sendMessage(p, "Now entering &a" + color + clanName + "'s&7 land @ &f(&eX:" + color + getClaim().getLocation().getChunk().getX() + " &eZ:" + color + getClaim().getLocation().getChunk().getZ() + "&f)");
	}

	public void handleUpdate() {
		HempfestClans.wildernessInhabitants.remove(p);
		if (!getClaim().getClaimID().equals(r.getClaim().getClaimID())) {
			if (Clan.clanUtil.getClan(p) != null) {
				if (!HempfestClans.clanManager(p).getClanID().equals(claim.getOwner())) {
					for (Resident r : HempfestClans.residents) {
						if (r.getPlayer().getName().equals(p.getName())) {
							Resident add = new Resident(p);
							add.setNotificationSent(true);
							add.setTraversedDifferent(true);
							add.setComingBack(true);
							add.setClaim(this.claim);
							this.r = add;
							HempfestClans.residents.remove(r);
							HempfestClans.residents.add(this.r);
							break;
						}
					}
				}
			} else {
				for (Resident r : HempfestClans.residents) {
					if (r.getPlayer().getName().equals(p.getName())) {
						Resident add = new Resident(p);
						add.setClaim(this.claim);
						this.r = add;
						HempfestClans.residents.remove(r);
						HempfestClans.residents.add(this.r);
						break;
					}
				}
			}
			if (Arrays.asList(getClaim().getClan().getMembers()).contains(p.getName())) {
				if (r.isComingBack()) {
					for (Resident r : HempfestClans.residents) {
						if (r.getPlayer().getName().equals(p.getName())) {
							Resident add = new Resident(p);
							add.setNotificationSent(false);
							add.setTraversedDifferent(true);
							add.setComingBack(false);
							add.setClaim(this.claim);
							this.r = add;
							HempfestClans.residents.remove(r);
							HempfestClans.residents.add(this.r);
							break;
						}
					}
				}
				if (r.isComingBack() && r.isNotificationSent()) {
					HempfestClans.residents.removeIf(r -> r.getPlayer().getName().equals(p.getName()));
					Resident add = new Resident(p);
					add.setNotificationSent(false);
					add.setTraversedDifferent(false);
					add.setComingBack(false);
					add.setClaim(this.claim);
					this.r = add;
					HempfestClans.residents.add(this.r);
				}
			}
		}
		if (!r.isNotificationSent()) {
			playTitle();
			if (Clan.clanUtil.getClan(p) != null) {
				if (!HempfestClans.clanManager(p).getClanID().equals(claim.getOwner())) {
					r.setComingBack(true);
				}
			}
			r.setNotificationSent(true);
		} else {
			if (r.hasTraversedDifferent()) {
				if (Clan.clanUtil.getClan(p) != null) {
					if (!HempfestClans.clanManager(p).getClanID().equals(claim.getOwner())) {
						r.setNotificationSent(false);
						r.setTraversedDifferent(false);
					}
				} else {
					r.setTraversedDifferent(false);
					r.setNotificationSent(false);
				}
			}
		}
	}

}
