package com.youtube.hempfest.clans.util.events;

import com.youtube.hempfest.clans.util.StringLibrary;
import com.youtube.hempfest.clans.util.construct.Clan;
import com.youtube.hempfest.clans.util.construct.ClanUtil;
import com.youtube.hempfest.clans.util.listener.ClanEventBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class ClanJoinEvent extends ClanEventBuilder implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private boolean cancelled;

	private final Player joining;

	private final Clan clan;

	public ClanJoinEvent(Player joining, Clan clan) {
		this.joining = joining;
		this.clan = clan;
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

	public Player getJoining() {
		return joining;
	}

	public Clan getClan() {
		return clan;
	}

}
