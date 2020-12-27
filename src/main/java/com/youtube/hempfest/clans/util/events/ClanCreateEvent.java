package com.youtube.hempfest.clans.util.events;

import com.youtube.hempfest.clans.util.StringLibrary;
import com.youtube.hempfest.clans.util.construct.Clan;
import com.youtube.hempfest.clans.util.construct.ClanUtil;
import com.youtube.hempfest.clans.util.listener.ClanEventBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class ClanCreateEvent extends ClanEventBuilder implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private boolean cancelled;

	private final Player maker;

	private final String name;

	private final String password;

	public ClanCreateEvent(Player maker, String name, String password) {
		this.maker = maker;
		this.name = name;
		this.password = password;
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

	public Player getMaker() {
		return maker;
	}

	public String getName() {
		return name;
	}

	public String getPassword() {
		return password;
	}

}
