package com.youtube.hempfest.clans.util;

import com.youtube.hempfest.clans.util.construct.ClanUtil;
import com.youtube.hempfest.clans.util.data.Config;
import com.youtube.hempfest.clans.util.data.ConfigType;
import com.youtube.hempfest.clans.util.data.DataManager;
import com.youtube.hempfest.hempcore.formatting.component.Text;
import com.youtube.hempfest.hempcore.formatting.component.Text_R2;
import com.youtube.hempfest.hempcore.formatting.string.ColoredString;
import java.util.List;
import java.util.Random;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StringLibrary {


	public void sendMessage(Player p, String message) {
		p.sendMessage(color(getPrefix() + " " + message));
	}

	public String color(String text) {
		if (Bukkit.getServer().getVersion().contains("1.16")) {
			return new ColoredString(text, ColoredString.ColorType.HEX).toString();
		} else
			return new ColoredString(text, ColoredString.ColorType.MC).toString();
	}

	public void sendComponent(CommandSender s, TextComponent text) {
		s.spigot().sendMessage(text);
	}

	public String getPrefix() {
		DataManager dm = new DataManager("Config", "Configuration");
		Config main = dm.getFile(ConfigType.MISC_FILE);
		return main.getConfig().getString("Formatting.Prefix");
	}

	public String alreadyInClan() {
		return "You are already in a clan";
	}

	public String notInClan() {
		return "You are not in a clan";
	}

	public String alreadyOwnClaim() {
		return "Your clan already owns this land";
	}

	public String notClaimOwner(String actualOwner) {
		return "You do not own this land, it belongs to: " + actualOwner;
	}

	public String wrongPassword() {
		return "The password you entered was incorrect";
	}

	public String getRankStyle() {
		DataManager dm = new DataManager("Config", "Configuration");
		Config main = dm.getFile(ConfigType.MISC_FILE);
		String type = main.getConfig().getString("Formatting.Rank-Style");
		String result = "";
		switch (type) {
			case "WORDLESS":
				result = "WORDLESS";
				break;
			default:
				result = "FULL";
				break;
		}
		return result;
	}

	public String getWordlessStyle(String rank) {
		DataManager dm = new DataManager("Config", "Configuration");
		Config main = dm.getFile(ConfigType.MISC_FILE);
		return main.getConfig().getString("Formatting.Styles.Wordless." + rank);
	}

	public String getFullStyle(String rank) {
		DataManager dm = new DataManager("Config", "Configuration");
		Config main = dm.getFile(ConfigType.MISC_FILE);
		return main.getConfig().getString("Formatting.Styles.Full." + rank);
	}

	public String getChatFormat() {
		DataManager dm = new DataManager("Config", "Configuration");
		Config main = dm.getFile(ConfigType.MISC_FILE);
		return main.getConfig().getString("Formatting.Chat");
	}

	public void paginatedClanList(Player p, List<String> listToPaginate, String command, int page, int contentLinesPerPage) {
		int totalPageCount = 1;
		if ((listToPaginate.size() % contentLinesPerPage) == 0) {
			if (listToPaginate.size() > 0) {
				totalPageCount = listToPaginate.size() / contentLinesPerPage;
			}
		} else {
			totalPageCount = (listToPaginate.size() / contentLinesPerPage) + 1;
		}

		if (page <= totalPageCount) {

			if (listToPaginate.isEmpty()) {
				sendMessage(p, color("&fThe list is empty!"));
			} else {
				int i = 0, k = 0;
				page--;
				p.sendMessage(color("&7&o&m============================"));
				for (String entry : listToPaginate) {
					k++;
					if ((((page * contentLinesPerPage) + i + 1) == k) && (k != ((page * contentLinesPerPage) + contentLinesPerPage + 1))) {
						i++;
						String c = "";
						ClanUtil clanUtil = new ClanUtil();
						if (clanUtil.getClan(p) != null) {
							c = clanUtil.clanRelationColor(clanUtil.getClan(p), clanUtil.getClanID(entry)) + entry;
						}
						p.sendMessage(color(c));
					}
				}
				int point;
				point = page + 1;
				if (page >= 1) {
					int last;
					last = point - 1;
					point = point + 1;
					p.sendMessage(color("&7&o&m============================"));
					if (page < (totalPageCount - 1)) {
						if (Bukkit.getServer().getVersion().contains("1.16")) {
							sendComponent(p, new Text().textRunnable("&7Navigate &b&o&m--&b> &7[", "&c&oBACK&7]", "&7 : [", "&b&oNEXT&7]", "&b&oClick to go &d&oback a page", "&b&oClick to goto the &5&onext page", command + " " + last, command + " " + point));
						} else {
							sendComponent(p, Text_R2.textRunnable("&7Navigate &b&o&m--&b> &7[", "&c&oBACK&7]", "&7 : [", "&b&oNEXT&7]", "&b&oClick to go &d&oback a page", "&b&oClick to goto the &5&onext page", command + " " + last, command + " " + point));
						}
					}
					if (page == (totalPageCount - 1)) {
						if (Bukkit.getServer().getVersion().contains("1.16")) {
							sendComponent(p, new Text().textRunnable("&7Navigate &b&o&m--&b> &7[", "&c&oBACK", "&7]", "&b&oClick to go &d&oback a page", command + " " + last));
						} else {
							sendComponent(p, Text_R2.textRunnable("&7Navigate &b&o&m--&b> &7[", "&c&oBACK", "&7]", "&b&oClick to go &d&oback a page", command + " " + last));
						}
					}
				}
				if (page == 0) {
					point = page + 1 + 1;
					p.sendMessage(color("&7&o&m============================"));
					if (Bukkit.getServer().getVersion().contains("1.16")) {
						sendComponent(p, new Text().textRunnable("&7Navigate &b&o&m--&b> &7[", "&b&oNEXT", "&7]", "&b&oClick to goto the &5&onext page", command + " " + point));
					} else {
						sendComponent(p, Text_R2.textRunnable("&7Navigate &b&o&m--&b> &7[", "&b&oNEXT", "&7]", "&b&oClick to goto the &5&onext page", command + " " + point));
					}
				}
			}
		} else {
			sendMessage(p, color("&eThere are only &f" + totalPageCount + " &epages!"));
		}
	}

	public void paginatedMemberList(Player p, List<String> listToPaginate, int page) {
		ClanUtil clanUtil = new ClanUtil();
		int totalPageCount = 1;
		if ((listToPaginate.size() % 6) == 0) {
			if (listToPaginate.size() > 0) {
				totalPageCount = listToPaginate.size() / 6;
			}
		} else {
			totalPageCount = (listToPaginate.size() / 6) + 1;
		}

		if (page <= totalPageCount) {

			if (listToPaginate.isEmpty()) {
				sendMessage(p, color("&fThe list is empty!"));
			} else {
				int i = 0, k = 0;
				page--;
				for (String entry : listToPaginate) {
					k++;
					if ((((page * 6) + i + 1) == k) && (k != ((page * 6) + 6 + 1))) {
						i++;
						if (Bukkit.getServer().getVersion().contains("1.16")) {
							sendComponent(p, new Text().textHoverable("&f- ", "&#2eab92&l" + entry, "&rRank: " + '"' + "&b" + clanUtil.getRankTag(clanUtil.getMemberRank(clanUtil.getClan(p), entry)) + "&r" + '"' + "\nK/D: &b&o" + clanUtil.getKD(clanUtil.getUserID(entry)) + "\n&rOnline: &b" + Bukkit.getOfflinePlayer(clanUtil.getUserID(entry)).isOnline()));
						} else {
							sendComponent(p, Text_R2.textHoverable("&f- ", "&b&l" + entry, "&rRank: " + '"' + "&b" + clanUtil.getRankTag(clanUtil.getMemberRank(clanUtil.getClan(p), entry)) + "&r" + '"' + "\nK/D: &b&o" + clanUtil.getKD(clanUtil.getUserID(entry)) + "\n&rOnline: &b" + Bukkit.getOfflinePlayer(clanUtil.getUserID(entry)).isOnline()));
						}
					}
				}
				int point;
				point = page + 1;
				if (page >= 1) {
					int last;
					last = point - 1;
					point = point + 1;
					if (Bukkit.getServer().getVersion().contains("1.16")) {
						sendComponent(p, new Text().textRunnable("&7Navigate &b&o&m--&b> &7[", "&c&oBACK&7]", "&7 : [", "&b&oNEXT&7]", "&b&oClick to go &d&oback a page", "&b&oClick to goto the &5&onext page", "c members" + " " + last, "c members" + " " + point));
					} else {
						sendComponent(p, Text_R2.textRunnable("&7Navigate &b&o&m--&b> &7[", "&c&oBACK&7]", "&7 : [", "&b&oNEXT&7]", "&b&oClick to go &d&oback a page", "&b&oClick to goto the &5&onext page", "c members" + " " + last, "c members" + " " + point));
					}
				}
				if (listToPaginate.size() > 6 && page == 0) {
					point = page + 1 + 1;
					if (Bukkit.getServer().getVersion().contains("1.16")) {
						sendComponent(p, new Text().textRunnable("&7Navigate &b&o&m--&b> &7[", "&b&oNEXT", "&7]", "&b&oClick to goto the &5&onext page", "c members" + " " + point));
					} else {
						sendComponent(p, Text_R2.textRunnable("&7Navigate &b&o&m--&b> &7[", "&b&oNEXT", "&7]", "&b&oClick to goto the &5&onext page", "c members" + " " + point));
					}
				}
			}
		} else {
			sendMessage(p, color("&eThere are only &f" + totalPageCount + " &epages!"));
		}
	}

	public void chunkBorderHint(Player p) {
		Random r = new Random();
		int send = r.nextInt(3);
		if (send == 2) {
			sendMessage(p, "&f[&7HINT&f] &7&oView chunk border's in game by pressing and releasing keys F3+G.");
		}
	}


}
