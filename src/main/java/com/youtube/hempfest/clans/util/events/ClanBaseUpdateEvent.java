package com.youtube.hempfest.clans.util.events;

import com.youtube.hempfest.clans.util.StringLibrary;
import com.youtube.hempfest.clans.util.construct.Clan;
import com.youtube.hempfest.clans.util.construct.ClanUtil;
import com.youtube.hempfest.clans.util.listener.ClanEventBuilder;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class ClanBaseUpdateEvent extends ClanEventBuilder implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private boolean cancelled;

	private final Player player;

	private final Location location;

	public ClanBaseUpdateEvent(Player player, Location location) {
		this.player = player;
		this.location = location;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public ClanUtil getUtil() {
		return Clan.clanUtil;
	}

	@Override
	public StringLibrary stringLibrary() {
		return Clan.clanUtil;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}

	public Player getPlayer() {
		return player;
	}

	public Location getLocation() {
		return location;
	}

}
