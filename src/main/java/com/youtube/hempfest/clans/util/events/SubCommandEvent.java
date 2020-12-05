package com.youtube.hempfest.clans.util.events;

import com.youtube.hempfest.clans.util.StringLibrary;
import com.youtube.hempfest.clans.util.construct.Clan;
import com.youtube.hempfest.clans.util.construct.ClanUtil;
import com.youtube.hempfest.clans.util.listener.ClanEventBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class SubCommandEvent extends ClanEventBuilder {

	private static final HandlerList handlers = new HandlerList();

	private final Player sender;

	private final String[] args;

	private boolean isCommand;

	public SubCommandEvent(Player sender, String[] args) {
		this.sender = sender;
		this.args = args;
	}

	@Override
	public HandlerList getHandlers() {
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

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public void setReturn(boolean b) {
		this.isCommand = b;
	}

	public boolean isCommand() {
		return isCommand;
	}

	public String[] getArgs() {
		return args;
	}

	public Player getSender() {
		return sender;
	}


}
