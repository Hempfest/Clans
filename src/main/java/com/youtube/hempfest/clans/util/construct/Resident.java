package com.youtube.hempfest.clans.util.construct;

import org.bukkit.entity.Player;

public class Resident {

	private final Player inhabitant;

	private final Claim claim;

	private final long joinTime;

	private boolean notificationSent;

	public Resident(Player inhabitant, Claim claim) {
		this.inhabitant = inhabitant;
		this.claim = claim;
		this.joinTime = System.currentTimeMillis();
	}

	public Player getPlayer() {
		return inhabitant;
	}

	public Claim getClaim() {
		return claim;
	}

	public boolean isNotificationSent() {
		return notificationSent;
	}

	public void setNotificationSent(boolean sent) {
		this.notificationSent = sent;
	}

	public long timeActiveInMillis() {
		return (System.currentTimeMillis() - joinTime);
	}

}
