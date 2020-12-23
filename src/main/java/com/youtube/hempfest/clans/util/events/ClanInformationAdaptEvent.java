package com.youtube.hempfest.clans.util.events;

import com.youtube.hempfest.clans.util.StringLibrary;
import com.youtube.hempfest.clans.util.construct.Clan;
import com.youtube.hempfest.clans.util.construct.ClanUtil;
import com.youtube.hempfest.clans.util.listener.ClanEventBuilder;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.event.HandlerList;

public class ClanInformationAdaptEvent extends ClanEventBuilder {

	private static final HandlerList handlers = new HandlerList();

	private final List<String> info;

	public ClanInformationAdaptEvent(List<String> commandArgs) {
		this.info = commandArgs;
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

	public List<String> getInsertions() {
		return info;
	}

	public void insert(String line) {
		info.add(stringLibrary().color(line));
	}

	public void insert(String... lines) {
		List<String> array = new ArrayList<>();
		for (String s : lines) {
			array.add(stringLibrary().color(s));
		}
		info.addAll(array);
	}


}
