package com.youtube.hempfest.clans.util;

import com.youtube.hempfest.clans.util.construct.Clan;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
public class Member {

	private static Team team;
	private static Scoreboard scoreboard;

	public static void setPrefix(Player player, String prefix) {

		scoreboard = player.getScoreboard();

		if (scoreboard.getTeam(player.getName()) == null) {
			scoreboard.registerNewTeam(player.getName());
		}

		team = scoreboard.getTeam(player.getName());
		team.setPrefix(Clan.clanUtil.color(prefix));
		team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
		team.addEntry(player.getName());
	}

	public static void updatePrefix(Player player, String prefix) {
		scoreboard = player.getScoreboard();
		if (scoreboard.getTeam(player.getName()) == null) {
			scoreboard.registerNewTeam(player.getName());
		}
		team = scoreboard.getTeam(player.getName());
		team.unregister();
		scoreboard.registerNewTeam(player.getName());
		team = scoreboard.getTeam(player.getName());
		team.setPrefix(Clan.clanUtil.color(prefix));
		team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
		team.addEntry(player.getName());
	}

	public static void removePrefix(Player player) {

		scoreboard = player.getScoreboard();

		if (scoreboard.getTeam(player.getName()) == null) {
			scoreboard.registerNewTeam(player.getName());
		}

		team = scoreboard.getTeam(player.getName());
		team.unregister();
	}

}
