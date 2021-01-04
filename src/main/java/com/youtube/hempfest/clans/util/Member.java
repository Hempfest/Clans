package com.youtube.hempfest.clans.util;

import com.youtube.hempfest.clans.HempfestClans;
import com.youtube.hempfest.clans.util.construct.Clan;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
public class Member {

	private static Team team;
	private static Scoreboard scoreboard;

	public static Team getTeam(Player player) {
		Clan c = HempfestClans.clanManager(player);
		scoreboard = player.getScoreboard();
		org.bukkit.scoreboard.Team result = null;
		if (scoreboard.getTeam(c.getClanID()) != null) {
			result = scoreboard.getTeam(c.getClanID());
		}
		return result;
	}

	public static void setPrefix(Player player, String prefix) {
		Clan c = HempfestClans.clanManager(player);
		scoreboard = player.getScoreboard();

		if (scoreboard.getTeam(c.getClanID()) == null) {
			scoreboard.registerNewTeam(c.getClanID());
			setPrefix(player, prefix);
		} else {
			team = getTeam(player);
			team.setPrefix(Clan.clanUtil.color(prefix));
			team.setDisplayName(c.getClanTag());
			team.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
			team.addEntry(player.getName());
		}
	}

	public static void updatePrefix(Player player, String prefix) {
		Clan c = HempfestClans.clanManager(player);
		scoreboard = player.getScoreboard();
		if (getTeam(player) != null) {
			team = getTeam(player);
			team.setPrefix(Clan.clanUtil.color(prefix));
			team.setDisplayName(c.getClanTag());
			team.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
		}

	}

	public static void removePrefix(Player player) {
		scoreboard = player.getScoreboard();
		try {
			if (getTeam(player) != null) {
				Clan c = HempfestClans.clanManager(player);
				team = scoreboard.getTeam(c.getClanID());
				if (!team.getEntries().isEmpty()) {
					if (team.getEntries().contains(player.getName())) {
						team.removeEntry(player.getName());
					}
				} else {
					team.unregister();
				}
			}
		} catch (IllegalArgumentException e) {
			if (scoreboard.getTeam(player.getName()) != null) {
				team = scoreboard.getTeam(player.getName());
			} else {
				team = scoreboard.registerNewTeam(player.getName());
			}
			team.unregister();
		}
	}

}