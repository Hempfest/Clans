package com.youtube.hempfest.clans.util.listener;

import com.youtube.hempfest.clans.HempfestClans;
import com.youtube.hempfest.clans.util.StringLibrary;
import com.youtube.hempfest.clans.util.construct.Clan;
import com.youtube.hempfest.clans.util.construct.ClanUtil;
import com.youtube.hempfest.clans.util.data.Config;
import com.youtube.hempfest.clans.util.events.CustomChatEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatEventListener implements Listener {

	private String chatMode(Player p) {
		return HempfestClans.chatMode.get(p);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPrefixApply(AsyncPlayerChatEvent event) {
		Player p = event.getPlayer();
		ClanUtil clanUtil = Clan.clanUtil;

		if (chatMode(p).equalsIgnoreCase("GLOBAL")) {
			Config main = HempfestClans.getMain();
			if (main.getConfig().getBoolean("Formatting.allow")) {
				if (clanUtil.getClan(p) != null) {
					Clan clan = new Clan(clanUtil.getClan(p));
					String clanName = clan.getClanTag();
					StringLibrary lib = new StringLibrary();
					String rank;
					clanName = clanUtil.getColor(clan.getChatColor()) + clanName;
					switch (lib.getRankStyle()) {
						case "WORDLESS":
							rank = lib.getWordlessStyle(clanUtil.getRank(p));
							event.setFormat(lib.color(String.format(lib.getChatFormat(), rank, clanName)) + " " + event.getFormat());
							break;
						case "FULL":
							rank = lib.getFullStyle(clanUtil.getRank(p));
							event.setFormat(lib.color(String.format(lib.getChatFormat(), rank, clanName)) + " " + event.getFormat());
							break;
					}
				}
			}
			return;
		}
		if (chatMode(p).equalsIgnoreCase("CLAN")) {
			if (!event.isCancelled()) {
				HempfestClans.getInstance().dataManager.formatClanChat(p, event.getRecipients(), event.getMessage());
				event.setCancelled(true);
				return;
			}
		}
		if (chatMode(p).equalsIgnoreCase("ALLY")) {
			if (!event.isCancelled()) {
				HempfestClans.getInstance().dataManager.formatAllyChat(p, event.getRecipients(), event.getMessage());
				event.setCancelled(true);
				return;
			}
		}

		List<String> defaults = new ArrayList<>(Arrays.asList("GLOBAL", "CLAN", "ALLY"));
		if (!defaults.contains(chatMode(p))) {
			// new event
			CustomChatEvent e = new CustomChatEvent(p, event.getRecipients(), event.getMessage(), true);
			Bukkit.getPluginManager().callEvent(e);
			if (!e.isCancelled()) {
				e.sendMessage();
			}
			event.setCancelled(true);
		}
	}

}
