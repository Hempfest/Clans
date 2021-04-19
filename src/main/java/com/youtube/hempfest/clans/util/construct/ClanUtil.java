package com.youtube.hempfest.clans.util.construct;

import com.github.sanctum.labyrinth.formatting.string.PaginatedAssortment;
import com.github.sanctum.labyrinth.formatting.string.RandomID;
import com.github.sanctum.labyrinth.library.HUID;
import com.youtube.hempfest.clans.HempfestClans;
import com.youtube.hempfest.clans.metadata.PersistentClan;
import com.youtube.hempfest.clans.util.RankPriority;
import com.youtube.hempfest.clans.util.StringLibrary;
import com.youtube.hempfest.clans.util.data.Config;
import com.youtube.hempfest.clans.util.data.ConfigType;
import com.youtube.hempfest.clans.util.data.DataManager;
import com.youtube.hempfest.clans.util.events.ClanInformationAdaptEvent;
import com.youtube.hempfest.clans.util.events.ClanJoinEvent;
import com.youtube.hempfest.clans.util.events.ClanLeaveEvent;
import com.youtube.hempfest.clans.util.misc.Color;
import com.youtube.hempfest.clans.util.misc.Member;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class ClanUtil extends StringLibrary {

	public boolean raidShield;

	public void create(Player p, String clanName, String password) {
		DataManager dm = new DataManager(p.getUniqueId().toString(), null);
		Config user = dm.getFile(ConfigType.USER_FILE);
		if (getClan(p) == null) {
			if (clanName.length() > HempfestClans.getMain().getConfig().getInt("Formatting.tag-size")) {
				sendMessage(p, "&c&oThe clan name you have chosen is too long! Max tag length reached.");
				return;
			}
			if (getAllClanIDs().size() >= HempfestClans.getMain().getConfig().getInt("Clans.max-clans")) {
				if (!p.hasPermission("clans.create.bypass")) {
					sendMessage(p, "&c&oYour server has already reached its max clan limit.");
					return;
				}
			}
			FileConfiguration local = user.getConfig();
			String newID = clanCode();
			local.set("Clan", newID);
			HempfestClans.getInstance().playerClan.put(p.getUniqueId(), newID);
			user.saveConfig();
			String status = "OPEN";
			if (password == null) {
				createClanFile(newID, clanName);
				sendMessage(p, "Clan " + '"' + clanName + '"' + " created with no password.");
				String format = String.format(HempfestClans.getMain().getConfig().getString("Response.creation"), p.getName(), status, clanName);
				Bukkit.broadcastMessage(color(getPrefix() + " " + format));
			} else {
				status = "LOCKED";
				createClanFile(newID, clanName, password);
				sendMessage(p, "Clan " + '"' + clanName + '"' + " created with password: &e&o" + password);
				String format = String.format(HempfestClans.getMain().getConfig().getString("Response.creation"), p.getName(), status, clanName);
				Bukkit.broadcastMessage(color(getPrefix() + " " + format));
			}
			DataManager data = new DataManager(newID, null);
			Config clanFile = data.getFile(ConfigType.CLAN_FILE);
			FileConfiguration fc = clanFile.getConfig();
			List<String> members = fc.getStringList("members");
			members.add(p.getName());
			fc.set("members", members);
			fc.set("owner", p.getName());
			clanFile.saveConfig();
			getClans.add(new Clan(newID));
			Bukkit.getScheduler().scheduleSyncDelayedTask(HempfestClans.getInstance(), () -> {
				if (HempfestClans.getInstance().dataManager.prefixedTagsAllowed()) {
					Clan c = HempfestClans.clanManager(p);
					Member.setPrefix(p, "&7[" + Clan.clanUtil.getColor(c.getChatColor()) + c.getClanTag() + "&7] ");
				}
			}, 2);
		} else {
			sendMessage(p, alreadyInClan());
		}
	}

	public void leave(Player p) {
		if (getClan(p) != null) {
			Clan clanIndex = HempfestClans.clanManager(p);
			DataManager dm = new DataManager(getClan(p), null);
			Config clan = dm.getFile(ConfigType.CLAN_FILE);
			DataManager data = new DataManager(p.getUniqueId().toString(), null);
			Config user = data.getFile(ConfigType.USER_FILE);
			ClanLeaveEvent event = new ClanLeaveEvent(p, clanIndex);
			Bukkit.getPluginManager().callEvent(event);
			if (!event.isCancelled()) {
				switch (getRank(p)) {
					case "Owner":
						if (HempfestClans.getInstance().dataManager.prefixedTagsAllowed()) {
							Member.removePrefix(p);
						}
						Clan c = HempfestClans.clanManager(p);
						for (String ally : c.getAllies()) {
							removeAlly(ally, c.getClanID());
						}
						for (String enemy : c.getEnemies()) {
							removeEnemy(enemy, c.getClanID());
						}

						DataManager dataManager = new DataManager("Regions", "Configuration");
						Config regions = dataManager.getFile(ConfigType.MISC_FILE);
						regions.getConfig().set(getClan(p), null);
						regions.saveConfig();
						String clanName = clan.getConfig().getString("name");
						try {
							if (Arrays.asList(PersistentClan.getClanContainer(Clan.clanUtil.getClan(p))).size() > 0) {
								for (HUID md : PersistentClan.getClanContainer(Clan.clanUtil.getClan(p))) {
									PersistentClan.deleteInstance(md);
								}
							}
						} catch (NullPointerException e) {
							Bukkit.getLogger().severe("- Unable to delete meta container, no/invalid HUID link(s) found.");
						}
						clan.delete();
						HempfestClans.clanEnemies.remove(Clan.clanUtil.getClan(p));
						HempfestClans.clanAllies.remove(Clan.clanUtil.getClan(p));
						getClans.remove(getClan(Clan.clanUtil.getClan(p)));
						user.getConfig().set("Clan", null);
						user.saveConfig();
						String format = String.format(HempfestClans.getMain().getConfig().getString("Response.deletion"), clanName);
						Bukkit.broadcastMessage(color(getPrefix() + " " + format));
						HempfestClans.getInstance().playerClan.remove(p.getUniqueId());
						ClaimManager.getInstance().refresh();
						break;
					case "Admin":
						if (HempfestClans.getInstance().dataManager.prefixedTagsAllowed()) {
							Member.removePrefix(p);
						}
						clanIndex.messageClan("&e&oPlayer " + '"' + p.getName() + '"' + " left the clan..");
						List<String> admins = clan.getConfig().getStringList("admins");
						admins.remove(p.getName());
						clan.getConfig().set("admins", admins);
						user.getConfig().set("Clan", null);
						clan.saveConfig();
						user.saveConfig();
						HempfestClans.getInstance().playerClan.remove(p.getUniqueId());
						break;
					case "Moderator":
						if (HempfestClans.getInstance().dataManager.prefixedTagsAllowed()) {
							Member.removePrefix(p);
						}
						clanIndex.messageClan("&e&oPlayer " + '"' + p.getName() + '"' + " left the clan..");
						List<String> moderators = clan.getConfig().getStringList("moderators");
						moderators.remove(p.getName());
						clan.getConfig().set("moderators", moderators);
						user.getConfig().set("Clan", null);
						clan.saveConfig();
						user.saveConfig();
						HempfestClans.getInstance().playerClan.remove(p.getUniqueId());
						break;
					case "Member":
						if (HempfestClans.getInstance().dataManager.prefixedTagsAllowed()) {
							Member.removePrefix(p);
						}
						clanIndex.messageClan("&e&oPlayer " + '"' + p.getName() + '"' + " left the clan..");
						List<String> members = clan.getConfig().getStringList("members");
						members.remove(p.getName());
						clan.getConfig().set("members", members);
						user.getConfig().set("Clan", null);
						clan.saveConfig();
						user.saveConfig();
						HempfestClans.getInstance().playerClan.remove(p.getUniqueId());
						break;
				}
			}
		} else {
			sendMessage(p, notInClan());
		}
	}

	public void joinClan(Player p, String clanName, String password) {
		if (getClan(p) == null) {
			if (!getAllClanNames().contains(clanName)) {
				sendMessage(p, "&c&oThis clan does not exist!");
				return;
			}
			if (getClanPassword(getClanID(clanName)) == null) {
				ClanJoinEvent event = new ClanJoinEvent(p, getClan(getClanID(clanName)));
				Bukkit.getPluginManager().callEvent(event);
				if (!event.isCancelled()) {
					DataManager data = new DataManager(p.getUniqueId().toString(), null);
					Config user = data.getFile(ConfigType.USER_FILE);
					user.getConfig().set("Clan", getClanID(clanName));
					user.saveConfig();
					HempfestClans.getInstance().playerClan.put(p.getUniqueId(), getClanID(clanName));
					DataManager dm = new DataManager(getClan(p), null);
					Config clan = dm.getFile(ConfigType.CLAN_FILE);
					FileConfiguration fc = clan.getConfig();
					List<String> members = fc.getStringList("members");
					members.add(p.getName());
					fc.set("members", members);
					clan.saveConfig();
					Clan clanIndex = HempfestClans.clanManager(p);
					clanIndex.messageClan("&a&oPlayer " + '"' + p.getName() + '"' + " joined the clan.");
					Member.setPrefix(p, "&7[" + getColor(clanIndex.getChatColor()) + clanIndex.getClanTag() + "&7] ");
					return;
				}
			}
			if (getClanPassword(getClanID(clanName)) != null && password.equals("none")) {
				sendMessage(p, "&c&oThis clan requires a password to join.");
				return;
			}
			if (getClanPassword(getClanID(clanName)).equals(password)) {
				ClanJoinEvent event = new ClanJoinEvent(p, getClan(getClanID(clanName)));
				Bukkit.getPluginManager().callEvent(event);
				if (!event.isCancelled()) {
					DataManager data = new DataManager(p.getUniqueId().toString(), null);
					Config user = data.getFile(ConfigType.USER_FILE);
					user.getConfig().set("Clan", getClanID(clanName));
					user.saveConfig();
					HempfestClans.getInstance().playerClan.put(p.getUniqueId(), getClanID(clanName));
					DataManager dm = new DataManager(getClan(p), null);
					Config clan = dm.getFile(ConfigType.CLAN_FILE);
					FileConfiguration fc = clan.getConfig();
					List<String> members = fc.getStringList("members");
					members.add(p.getName());
					fc.set("members", members);
					clan.saveConfig();
					Clan clanIndex = HempfestClans.clanManager(p);
					clanIndex.messageClan("&a&oPlayer " + '"' + p.getName() + '"' + " joined the clan.");
					if (HempfestClans.getInstance().dataManager.prefixedTagsAllowed()) {
						Member.setPrefix(p, "&7[" + Clan.clanUtil.getColor(clanIndex.getChatColor()) + clanIndex.getClanTag() + "&7] ");
					}
				}
			} else {
				sendMessage(p, wrongPassword());
			}
		} else
			sendMessage(p, alreadyInClan());
	}

	public List<String> getAllyRequests(String clanID) {
		DataManager dm = new DataManager(clanID, null);
		Config clan = dm.getFile(ConfigType.CLAN_FILE);
		return new ArrayList<>(clan.getConfig().getStringList("ally-requests"));
	}

	public void sendAllyRequest(Player p, String clanID, String targetClanID) {
		DataManager dm = new DataManager(targetClanID, null);
		Config clan = dm.getFile(ConfigType.CLAN_FILE);
		if (getAllyRequests(clanID).contains(targetClanID)) {
			addAlly(clanID, targetClanID);
			return;
		}
		if (getAllyRequests(targetClanID).contains(clanID)) {
			sendMessage(p, "&e&oWaiting on a response from &f" + getClanTag(targetClanID));
			return;
		}
		List<String> allies = getAllyRequests(targetClanID);
		allies.add(clanID);
		clan.getConfig().set("ally-requests", allies);
		clan.saveConfig();
		Clan clanIndex = new Clan(targetClanID);
		sendMessage(p, "&a&oAlly invitation sent.");
		clanIndex.messageClan("&a&oClan " + '"' + "&e" + getClanTag(clanID) + "&a&o" + '"' + " wishes to ally, to accept\n&7Type &f/clan ally &6" + getClanTag(clanID));
	}

	public void addAlly(String clanID, String targetClanID) {
		DataManager dm = new DataManager(clanID, null);
		Config clan = dm.getFile(ConfigType.CLAN_FILE);
		DataManager dm2 = new DataManager(targetClanID, null);
		Config clan2 = dm2.getFile(ConfigType.CLAN_FILE);
		if (getAllyRequests(clanID).contains(targetClanID)) {
			List<String> allyRequests = getAllyRequests(clanID);
			allyRequests.remove(targetClanID);
			clan.getConfig().set("ally-requests", allyRequests);
			clan.saveConfig();
		}
		List<String> allies = getAllies(clanID);
		List<String> allies2 = getAllies(targetClanID);
		allies.add(targetClanID);
		allies2.add(clanID);
		clan.getConfig().set("allies", allies);
		clan.saveConfig();
		clan2.getConfig().set("allies", allies2);
		clan2.saveConfig();
		Clan clanIndex = new Clan(clanID);
		Clan clanIndex2 = new Clan(targetClanID);
		clanIndex.messageClan("&a&oNow allies with clan " + '"' + "&e" + getClanTag(targetClanID) + "&a&o" + '"');
		clanIndex2.messageClan("&a&oNow allies with clan " + '"' + "&e" + getClanTag(clanID) + "&a&o" + '"');
		HempfestClans.clanAllies.put(clanID, allies);
	}

	public void removeAlly(String clanID, String targetClanID) {
		DataManager dm = new DataManager(clanID, null);
		Config clan = dm.getFile(ConfigType.CLAN_FILE);
		List<String> allies = getAllies(clanID);
		allies.remove(targetClanID);
		clan.getConfig().set("allies", allies);
		clan.saveConfig();
		HempfestClans.clanAllies.put(clanID, allies);
	}

	public void addEnemy(String clanID, String targetClanID) {
		DataManager dm = new DataManager(clanID, null);
		Config clan = dm.getFile(ConfigType.CLAN_FILE);
		if (getAllies(clanID).contains(targetClanID)) {
			removeAlly(clanID, targetClanID);
			removeAlly(targetClanID, clanID);
		}
		List<String> enemies = getEnemies(clanID);
		enemies.add(targetClanID);
		clan.getConfig().set("enemies", enemies);
		clan.saveConfig();
		Clan clanIndex = new Clan(clanID);
		Clan clanIndex2 = new Clan(targetClanID);
		clanIndex.messageClan("&4&oNow enemies with clan " + '"' + "&e" + getClanTag(targetClanID) + "&4&o" + '"');
		clanIndex2.messageClan("&4&oNow enemies with clan " + '"' + "&e" + getClanTag(clanID) + "&4&o" + '"');
		HempfestClans.clanEnemies.put(clanID, enemies);
	}

	public void removeEnemy(String clanID, String targetClanID) {
		DataManager dm = new DataManager(clanID, null);
		Config clan = dm.getFile(ConfigType.CLAN_FILE);
		List<String> enemies = getEnemies(clanID);
		enemies.remove(targetClanID);
		clan.getConfig().set("enemies", enemies);
		clan.saveConfig();
		Clan clanIndex = new Clan(clanID);
		Clan clanIndex2 = new Clan(targetClanID);
		clanIndex.messageClan("&f&oNow neutral with clan " + '"' + "&e" + getClanTag(targetClanID) + "&f&o" + '"');
		clanIndex2.messageClan("&f&oNow neutral with clan " + '"' + "&e" + getClanTag(clanID) + "&f&o" + '"');
		HempfestClans.clanEnemies.put(clanID, enemies);
	}

	public double getKD(UUID playerID) {
		OfflinePlayer player = Bukkit.getOfflinePlayer(playerID);
		if (Bukkit.getVersion().contains("1.12") || Bukkit.getVersion().contains("1.13")
				|| Bukkit.getVersion().contains("1.14")) {
			return 0.0;
		}
		int kills = player.getStatistic(Statistic.PLAYER_KILLS);
		int deaths = player.getStatistic(Statistic.DEATHS);
		double result;
		if (deaths == 0) {
			result = kills;
		} else {
			double value = (double) kills / deaths;
			result = Math.round(value);
		}
		return result;
	}

	public UUID getUserID(String playerName) {
		UUID result = null;
		for (UUID player : getAllUsers()) {
			DataManager dm = new DataManager(player.toString(), null);
			Config user = dm.getFile(ConfigType.USER_FILE);
			if (user.getConfig().getString("username") == null) {
				user.getConfig().set("username", Bukkit.getOfflinePlayer(player).getName());
				user.saveConfig();
			}
			if (Objects.equals(user.getConfig().getString("username"), playerName)) {
				result = player;
			}
		}
		return result;
	}

	public List<UUID> getAllUsers() {
		DataManager dm = new DataManager();
		List<UUID> result = new ArrayList<>();
		for (File file : Objects.requireNonNull(dm.getUserFolder().listFiles())) {
			result.add(UUID.fromString(file.getName().replace(".yml", "")));
		}
		return result;
	}

	public static void updateUsername(Player p) {
		DataManager data = new DataManager(p.getUniqueId().toString(), null);
		Config user = data.getFile(ConfigType.USER_FILE);
		if (user.getConfig().getString("username") == null) {
			user.getConfig().set("username", p.getName());
			user.saveConfig();
		}
	}

	public String getCurrentRank(int rankPower) {
		String result = "";
		switch (rankPower) {
			case 0:
				result = "members";
				break;
			case 1:
				result = "moderators";
				break;
			case 2:
				result = "admins";
				break;
			case 3:
				result = "owner";
				break;
		}
		return result;
	}

	public String getRankUpgrade(int rankPower) {
		String result = null;
		switch (rankPower) {
			case 0:
				result = "moderators";
				break;
			case 1:
				result = "admins";
				break;
		}
		return result;
	}

	public String getRankDowngrade(int rankPower) {
		String result = null;
		switch (rankPower) {
			case 1:
				result = "members";
				break;
			case 2:
				result = "moderators";
				break;
		}
		return result;
	}

	private void createClanFile(String clanID, String name) {
		DataManager dm = new DataManager(clanID, null);
		Config clan = dm.getFile(ConfigType.CLAN_FILE);
		FileConfiguration local = clan.getConfig();
		List<String> members = new ArrayList<>();
		List<String> mods = new ArrayList<>();
		List<String> admins = new ArrayList<>();
		List<String> allies = new ArrayList<>();
		List<String> enemies = new ArrayList<>();
		local.set("name", name);
		local.set("members", members);
		local.set("moderators", mods);
		local.set("admins", admins);
		local.set("allies", allies);
		local.set("enemies", enemies);
		clan.saveConfig();
		System.out.printf("[%s] - Clan " + '"' + clanID + '"' + " created.%n", HempfestClans.getInstance().getDescription().getName());
	}

	private void createClanFile(String clanID, String name, String password) {
		DataManager dm = new DataManager(clanID, null);
		Config clan = dm.getFile(ConfigType.CLAN_FILE);
		FileConfiguration local = clan.getConfig();
		List<String> members = new ArrayList<>();
		List<String> mods = new ArrayList<>();
		List<String> admins = new ArrayList<>();
		List<String> allies = new ArrayList<>();
		List<String> enemies = new ArrayList<>();
		local.set("name", name);
		local.set("password", password);
		local.set("members", members);
		local.set("moderators", mods);
		local.set("admins", admins);
		local.set("allies", allies);
		local.set("enemies", enemies);
		clan.saveConfig();
		System.out.printf("[%s] - Clan " + '"' + clanID + '"' + " created.%n", HempfestClans.getInstance().getDescription().getName());
	}

	public void demotePlayer(Player target) {
		ClanUtil clanUtil = new ClanUtil();
		DataManager data = new DataManager(getClan(target), null);
		Config clan = data.getFile(ConfigType.CLAN_FILE);
		if (clan.getConfig().getStringList("members").contains(target.getName())) {
			if (clanUtil.getRankPower(target) != 3 || clanUtil.getRankPower(target) != 0) {
				String currentRank = clanUtil.getCurrentRank(clanUtil.getRankPower(target));
				List<String> array = clan.getConfig().getStringList(clanUtil.getRankDowngrade(clanUtil.getRankPower(target)));
				if (!clanUtil.getRankDowngrade(clanUtil.getRankPower(target)).equals("members"))
					array.add(target.getName());
				clan.getConfig().set(clanUtil.getRankDowngrade(clanUtil.getRankPower(target)), array);
				List<String> array2 = clan.getConfig().getStringList(currentRank);
				array2.remove(target.getName());
				clan.getConfig().set(currentRank, array2);
				clan.saveConfig();
				Clan clanIndex = HempfestClans.clanManager(target);
				String format = String.format(HempfestClans.getMain().getConfig().getString("Response.demotion"), target.getName(), getRankTag(getRank(target)));
				clanIndex.messageClan(format);
			}
		}
	}

	public void promotePlayer(Player target) {
		ClanUtil clanUtil = new ClanUtil();
		DataManager data = new DataManager(getClan(target), null);
		Config clan = data.getFile(ConfigType.CLAN_FILE);
		if (clan.getConfig().getStringList("members").contains(target.getName())) {
			if (clanUtil.getRankPower(target) < clanUtil.maxRankPower()) {
				String currentRank = clanUtil.getCurrentRank(clanUtil.getRankPower(target));
				List<String> array = clan.getConfig().getStringList(clanUtil.getRankUpgrade(clanUtil.getRankPower(target)));
				List<String> array2 = clan.getConfig().getStringList(currentRank);
				if (!currentRank.equals("members")) {
					array2.remove(target.getName());
				}
				array.add(target.getName());
				clan.getConfig().set(clanUtil.getRankUpgrade(clanUtil.getRankPower(target)), array);
				clan.getConfig().set(currentRank, array2);
				clan.saveConfig();
				Clan clanIndex = HempfestClans.clanManager(target);
				String format = String.format(HempfestClans.getMain().getConfig().getString("Response.promotion"), target.getName(), getRankTag(getRank(target)));
				clanIndex.messageClan(format);
			}
		}
	}

	public void kickPlayer(String target) {
		UUID tid = getUserID(target);
		DataManager dm = new DataManager(tid.toString(), null);
		Config user = dm.getFile(ConfigType.USER_FILE);
		DataManager data = new DataManager(user.getConfig().getString("Clan"), null);
		Config clan = data.getFile(ConfigType.CLAN_FILE);
		FileConfiguration fc = clan.getConfig();
		List<String> members = fc.getStringList("members");
		List<String> admins = fc.getStringList("admins");
		List<String> moderators = fc.getStringList("moderators");
		if (HempfestClans.getInstance().dataManager.prefixedTagsAllowed()) {
			OfflinePlayer player = Bukkit.getOfflinePlayer(tid);
			if (player.isOnline()) {
				Member.removePrefix(player.getPlayer());
			}
		}
		if (fc.getStringList("members").contains(target)) {
			members.remove(target);
			fc.set("members", members);
		}
		if (fc.getStringList("moderators").contains(target)) {
			moderators.remove(target);
			fc.set("moderators", moderators);
		}
		if (fc.getStringList("admins").contains(target)) {
			admins.remove(target);
			fc.set("admins", admins);
		}
		clan.saveConfig();
		user.getConfig().set("Clan", null);
		user.saveConfig();
		HempfestClans.getInstance().playerClan.remove(tid);
	}

	public void teleportBase(Player p) {
		Clan clan = HempfestClans.clanManager(p);
		if (clan.getBase() != null) {
			p.teleport(clan.getBase());
		}
	}

	public void transferOwner(Player p, String target) {
		DataManager dm = new DataManager(getClan(p), null);
		Config clan = dm.getFile(ConfigType.CLAN_FILE);
		Clan clanIndex = HempfestClans.clanManager(p);
		if (Arrays.asList(clanIndex.getMembers()).contains(target)) {
			if (getRankPower(p) == 3) {
				clan.getConfig().set("owner", target);
				clan.saveConfig();
				sendMessage(p, "&d&oOwnership transferred.. It was a nice run..");
			} else {
				sendMessage(p, "&cYou don't have clan clearance.");
			}
		} else {
			sendMessage(p, "&c&oMember not found.");
		}
	}

	/**
	 * @param p Target player to retrieve id from
	 * @return Gets the specified players clanID
	 */
	public String getClan(Player p) {
		if (!HempfestClans.getInstance().playerClan.containsKey(p.getUniqueId())) {
			DataManager dm = new DataManager(p.getUniqueId().toString());
			Config user = dm.getFile(ConfigType.USER_FILE);
			if (user.exists()) {
				if (user.getConfig().getString("Clan") != null) {
					return user.getConfig().getString("Clan");
				}
			}
			return null;
		}
		return HempfestClans.getInstance().playerClan.get(p.getUniqueId());
	}

	public void changeNickname(Player p, String newName) {
		DataManager dm = new DataManager(p.getUniqueId().toString(), null);
		Config user = dm.getFile(ConfigType.USER_FILE);
		if (newName.equals("empty")) {
			user.getConfig().set("Nickname", p.getName());
			newName = p.getName();
		} else {
			user.getConfig().set("Nickname", newName);
		}
		user.saveConfig();
		sendMessage(p, "&3&oChat nickname updated to: &7" + newName);
	}

	/**
	 * @param p Target player to check
	 * @return Gets the clan nickname for the specified player.
	 */
	public String getClanNickname(Player p) {
		DataManager dm = new DataManager(p.getUniqueId().toString(), null);
		Config user = dm.getFile(ConfigType.USER_FILE);
		return user.getConfig().getString("Nickname") != null ? user.getConfig().getString("Nickname") : p.getName();
	}

	/**
	 * @param p Target to check
	 * @return Gets the rank of the specified player in default format.
	 */
	public String getRank(Player p) {
		DataManager dm = new DataManager(getClan(p));
		Config clan = dm.getFile(ConfigType.CLAN_FILE);
		String rank = "";
		FileConfiguration fc = clan.getConfig();
		if (fc.getStringList("members").contains(p.getName())) {
			rank = "Member";
		}
		if (fc.getStringList("moderators").contains(p.getName())) {
			rank = "Moderator";
		}
		if (fc.getStringList("admins").contains(p.getName())) {
			rank = "Admin";
		}
		if (Objects.equals(fc.getString("owner"), p.getName())) {
			rank = "Owner";
		}
		return rank;
	}

	/**
	 * @param p Target to check
	 * @return Gets the rank of the specified player in default format.
	 */
	public String getRank(OfflinePlayer p) {
		DataManager dm = new DataManager(p.getUniqueId().toString(), null);
		Config user = dm.getFile(ConfigType.USER_FILE);
		DataManager dm2 = new DataManager(user.getConfig().getString("Clan"));
		Config clan = dm2.getFile(ConfigType.CLAN_FILE);
		String rank = "";
		FileConfiguration fc = clan.getConfig();
		if (fc.getStringList("members").contains(p.getName())) {
			rank = "Member";
		}
		if (fc.getStringList("moderators").contains(p.getName())) {
			rank = "Moderator";
		}
		if (fc.getStringList("admins").contains(p.getName())) {
			rank = "Admin";
		}
		if (Objects.equals(fc.getString("owner"), p.getName())) {
			rank = "Owner";
		}
		return rank;
	}

	/**
	 * @param rank The default rank to check
	 * @return Gets the configured rank tag for the specifed default rank
	 * See getRank(Player p) for online usage.
	 */
	public String getRankTag(String rank) {
		String result = "";
		DataManager dm = new DataManager("Config", "Configuration");
		Config main = dm.getFile(ConfigType.MISC_FILE);
		String member = main.getConfig().getString("Formatting.Styles.Full.Member");
		String mod = main.getConfig().getString("Formatting.Styles.Full.Moderator");
		String admin = main.getConfig().getString("Formatting.Styles.Full.Admin");
		String owner = main.getConfig().getString("Formatting.Styles.Full.Owner");
		switch (rank) {
			case "Member":
				result = member;
				break;
			case "Moderator":
				result = mod;
				break;
			case "Admin":
				result = admin;
				break;
			case "Owner":
				result = owner;
				break;
		}
		return result;
	}

	public String getMemberRank(String clanID, String member) {
		DataManager dm = new DataManager(clanID, null);
		Config clan = dm.getFile(ConfigType.CLAN_FILE);
		String rank = "";
		FileConfiguration fc = clan.getConfig();
		if (fc.getStringList("members").contains(member)) {
			rank = "Member";
		}
		if (fc.getStringList("moderators").contains(member)) {
			rank = "Moderator";
		}
		if (fc.getStringList("admins").contains(member)) {
			rank = "Admin";
		}
		if (Objects.equals(fc.getString("owner"), member)) {
			rank = "Owner";
		}
		return rank;
	}

	public int getRankPower(Player p) {
		return getRankPriority(getRank(p)).toInt();
	}

	public int getRankPower(OfflinePlayer p) {
		return getRankPriority(getRank(p)).toInt();
	}

	public int maxRankPower() {
		return 2;
	}

	public RankPriority getRankPriority(String rank) {
		RankPriority priority = null;
		switch (rank) {
			case "Owner":
				priority = RankPriority.HIGHEST;
				break;
			case "Admin":
				priority = RankPriority.HIGHER;
				break;
			case "Moderator":
				priority = RankPriority.HIGH;
				break;
			case "Member":
				priority = RankPriority.NORMAL;
				break;
		}
		return priority;
	}

	/**
	 * @param clanID Target clan to check
	 * @return Gets the clan tag from the specified clan.
	 */
	public String getClanTag(String clanID) {
		DataManager dm = new DataManager(clanID, null);
		Config clan = dm.getFile(ConfigType.CLAN_FILE);
		return clan.getConfig().getString("name");
	}

	/**
	 * @param color Target color in COLOR_NAME format
	 * @return Gets the specified color in color code format.
	 */
	public String getColor(String color) {
		StringBuilder result = new StringBuilder();
		for (String c : color.split(",")) {
			for (Color a : Color.values()) {
				if (c.replace(",", "").matches(a.name().toLowerCase().replace(",", "").replace("_", ""))) {
					result.append(a.toCode());
				}
				if (c.replace(",", "").matches(a.name().replace(",", "").replace("_", ""))) {
					result.append(a.toCode());
				}
			}
		}
		return result.toString();
	}

	public List<Clan> getClans = new ArrayList<>();

	public void loadClans() {
		for (String clanID : getAllClanIDs()) {
			Clan instance = new Clan(clanID);
			if (!getClans.contains(instance)) {
				getClans.add(instance);
			}
		}
	}

	public Clan getClan(String clanID) {
		Clan clan = null;
		for (Clan c : getClans) {
			if (c.getClanID().equals(clanID)) {
				clan = c;
			}
		}
		return clan;
	}

	/**
	 * @param clanID Target clan to check
	 * @return Gets the list of allies for the specified clan.
	 */
	public List<String> getAllies(String clanID) {
		if (!HempfestClans.clanAllies.containsKey(clanID)) {
			DataManager dm = new DataManager(clanID, null);
			Config clan = dm.getFile(ConfigType.CLAN_FILE);
			return new ArrayList<>(clan.getConfig().getStringList("allies"));
		}
		return HempfestClans.clanAllies.get(clanID);
	}

	/**
	 * @param clanID Target clan to check
	 * @return Gets the list of enemies for the specified clan.
	 */
	public List<String> getEnemies(String clanID) {
		if (!HempfestClans.clanEnemies.containsKey(clanID)) {
			DataManager dm = new DataManager(clanID, null);
			Config clan = dm.getFile(ConfigType.CLAN_FILE);
			return new ArrayList<>(clan.getConfig().getStringList("enemies"));
		}
		return HempfestClans.clanEnemies.get(clanID);
	}

	/**
	 * @param clanID       Primary clan
	 * @param targetClanID Target clan
	 * @return Checks if the two clans are neutral in relation.
	 */
	public boolean isNeutral(String clanID, String targetClanID) {
		return !getAllies(clanID).contains(targetClanID) && !getEnemies(clanID).contains(targetClanID);
	}

	/**
	 * @param clanID       Primary clan
	 * @param targetClanID Target clan
	 * @return Gets the relation color in color code format for the two clans.
	 */
	public String clanRelationColor(String clanID, String targetClanID) {
		String result = "&f&o";
		ClanUtil clanUtil = new ClanUtil();
		try {
			if (clanUtil.getAllClanIDs().contains(targetClanID)) {
				if (isNeutral(clanID, targetClanID)) {
					result = "&f";
				}
				if (clanID.equals(targetClanID)) {
					result = "&6&l";
				}
				if (getAllies(clanID).contains(targetClanID)) {
					result = "&a";
				}
				if (getEnemies(clanID).contains(targetClanID)) {
					result = "&c";
				}
			}
		} catch (NullPointerException ignored) {
		}
		return result;
	}

	public String getClanPassword(String clanID) {
		DataManager dm = new DataManager(clanID, null);
		Config clan = dm.getFile(ConfigType.CLAN_FILE);
		return clan.getConfig().getString("password");
	}

	public void getMyClanInfo(Player p, int page) {
		String clanID = getClan(p);
		Clan clanIndex = HempfestClans.clanManager(p);
		DataManager dm = new DataManager(clanID, null);
		Config clan = dm.getFile(ConfigType.CLAN_FILE);
		List<String> array = new ArrayList<>();
		String owner = clan.getConfig().getString("owner");
		String password = clan.getConfig().getString("password");
		List<String> members = clan.getConfig().getStringList("members");
		List<String> mods = clan.getConfig().getStringList("moderators");
		List<String> admins = clan.getConfig().getStringList("admins");
		List<String> allies = clan.getConfig().getStringList("allies");
		List<String> allyRequests = clan.getConfig().getStringList("ally-requests");
		List<String> enemies = clan.getConfig().getStringList("enemies");
		array.add(" ");
		array.add("&6&lClan&7: &f" + getColor(clanIndex.getChatColor()) + getClanTag(clanID));
		array.add("&f&m---------------------------");
		array.add("&6" + getRankTag("Owner") + ": &f" + owner);
		if (password == null)
			password = "NO PASS";
		if (clanIndex.getBase() != null)
			array.add("&6Base: &aSet");
		if (clanIndex.getBase() == null)
			array.add("&6Base: &7Not set");
		array.add("&6Color: " + getColor(clanIndex.getChatColor()) + clanIndex.getChatColor());
		if (getRankPower(p) >= viewPassClearance()) {
			array.add("&6Password: &f" + password);
		}
		array.add("&6&lPower [&e" + clanIndex.format(String.valueOf(clanIndex.getPower())) + "&6&l]");
		array.add("&6" + getRankTag("Admin") + "s [&b" + admins.size() + "&6]");
		array.add("&6" + getRankTag("Moderator") + "s [&e" + mods.size() + "&6]");
		if (HempfestClans.getInstance().dataManager.claimEffect()) {
			array.add("&6Claims [&e" + clanIndex.getOwnedClaims().length + "&f / &e" + clanIndex.maxClaims() + "&6]");
		} else {
			array.add("&6Claims [&e" + clanIndex.getOwnedClaims().length + "&6]");
		}
		array.add("&f&m---------------------------");
		if (allyRequests.size() > 0) {
			array.add("&6Ally Requests [&b" + allyRequests.size() + "&6]");
			for (String clanId : allyRequests) {
				array.add("&f- &e&o" + getClanTag(clanId));
			}
		}
		if (allyRequests.isEmpty())
			array.add("&6Ally Requests [&b" + 0 + "&6]");
		if (allies.size() > 0) {
			array.add("&6Allies [&b" + allies.size() + "&6]");
			for (String clanId : allies) {
				array.add("&f- &e&o" + getClanTag(clanId));
			}
		}
		for (String clanId : getAllClanIDs()) {
			if (getEnemies(clanId).contains(getClan(p))) {
				enemies.add(clanId);
			}
		}
		if (allies.isEmpty())
			array.add("&6Allies [&b" + 0 + "&6]");
		if (enemies.size() > 0) {
			array.add("&6Enemies [&b" + enemies.size() + "&6]");
			for (String clanId : enemies) {
				array.add("&f- &c&o" + getClanTag(clanId));
			}
		}
		if (enemies.isEmpty())
			array.add("&6Enemies [&b" + 0 + "&6]");
		array.add("&f&m---------------------------");
		array.add("&n" + getRankTag("Member") + "s&r [&7" + members.size() + "&r]");
		ClanInformationAdaptEvent event = new ClanInformationAdaptEvent(array, clanID);
		Bukkit.getPluginManager().callEvent(event);
		printArray(p, event.getInsertions());
		paginatedMemberList(p, members, page);
		p.sendMessage(" ");
	}

	/**
	 * @param clanName Target clan to check
	 * @return Gets the ID of a specified clan by name.
	 */
	public String getClanID(String clanName) {
		String result = null;
		for (String ID : getAllClanIDs()) {
			DataManager dm = new DataManager(ID, null);
			Config clan = dm.getFile(ConfigType.CLAN_FILE);
			if (Objects.equals(clan.getConfig().getString("name"), clanName)) {
				result = ID;
				break;
			}
		}
		return result;
	}

	/**
	 * @return Gets a list of all saved clans by name
	 */
	public List<String> getAllClanNames() {
		List<String> array = new ArrayList<>();
		for (String clan : getAllClanIDs()) {
			DataManager dm = new DataManager(clan, null);
			Config c = dm.getFile(ConfigType.CLAN_FILE);
			array.add(c.getConfig().getString("name"));
		}
		return array;
	}

	/**
	 * @return Gets a list of all saved clans by clanID
	 */
	public List<String> getAllClanIDs() {
		DataManager dm = new DataManager();
		List<String> array = new ArrayList<>();
		for (File file : Objects.requireNonNull(dm.getClanFolder().listFiles())) {
			array.add(file.getName().replace(".yml", ""));
		}
		return array;
	}


	private void printArray(Player p, List<String> list) {
		for (String l : list) {
			p.sendMessage(color(l));
		}
	}

	public boolean overPowerBypass() {
		DataManager dm = new DataManager("Config", "Configuration");
		Config main = dm.getFile(ConfigType.MISC_FILE);
		return main.getConfig().getBoolean("Clans.raid-shield.claiming");
	}

	public int invitationClearance() {
		DataManager dm = new DataManager("Config", "Configuration");
		Config main = dm.getFile(ConfigType.MISC_FILE);
		return main.getConfig().getInt("Clans.invite-clearance");
	}

	public int tagChangeClearance() {
		DataManager dm = new DataManager("Config", "Configuration");
		Config main = dm.getFile(ConfigType.MISC_FILE);
		return main.getConfig().getInt("Clans.rename-clearance");
	}

	public int colorChangeClearance() {
		DataManager dm = new DataManager("Config", "Configuration");
		Config main = dm.getFile(ConfigType.MISC_FILE);
		return main.getConfig().getInt("Clans.recolor-clearance");
	}

	public int positionClearance() {
		DataManager dm = new DataManager("Config", "Configuration");
		Config main = dm.getFile(ConfigType.MISC_FILE);
		return main.getConfig().getInt("Clans.position-clearance");
	}

	public int unclaimAllClearance() {
		DataManager dm = new DataManager("Config", "Configuration");
		Config main = dm.getFile(ConfigType.MISC_FILE);
		return main.getConfig().getInt("Clans.land-claiming.unclaim-all-clearance");
	}

	public int claimingClearance() {
		DataManager dm = new DataManager("Config", "Configuration");
		Config main = dm.getFile(ConfigType.MISC_FILE);
		return main.getConfig().getInt("Clans.land-claiming.clearance");
	}

	public int viewPassClearance() {
		DataManager dm = new DataManager("Config", "Configuration");
		Config main = dm.getFile(ConfigType.MISC_FILE);
		return main.getConfig().getInt("Clans.password-visible-clearance");
	}

	public int baseClearance() {
		DataManager dm = new DataManager("Config", "Configuration");
		Config main = dm.getFile(ConfigType.MISC_FILE);
		return main.getConfig().getInt("Clans.base-clearance");
	}

	public int friendfireClearance() {
		DataManager dm = new DataManager("Config", "Configuration");
		Config main = dm.getFile(ConfigType.MISC_FILE);
		return main.getConfig().getInt("Clans.friendlyfire-clearance");
	}

	public int kickClearance() {
		DataManager dm = new DataManager("Config", "Configuration");
		Config main = dm.getFile(ConfigType.MISC_FILE);
		return main.getConfig().getInt("Clans.kick-clearance");

	}

	public int passwordClearance() {
		DataManager dm = new DataManager("Config", "Configuration");
		Config main = dm.getFile(ConfigType.MISC_FILE);
		return main.getConfig().getInt("Clans.password-clearance");

	}

	private String clanCode() {
		return serial(4) + "-" + serial(4) + "-" + serial(4);
	}

	private String serial(int count) {
		return new RandomID(count, "ABGHJ2567").generate();
	}

	public void setRaidShield(boolean value) {
		this.raidShield = value;
	}

	public boolean shieldStatus() {
		return raidShield;
	}

	public boolean isNight(String world, int on, int off) {
		Server server = Bukkit.getServer();
		long time = 0;
		try {
			time = Objects.requireNonNull(server.getWorld(world)).getTime();
		} catch (NullPointerException e) {
			HempfestClans.getInstance().getLogger().severe("- World not found in configuration. Raid-shield will not work properly.");
		}

		return time <= on || time >= off;
	}

	public void getLeaderboard(Player p, int page) {
		HashMap<String, Double> clans = new HashMap<>();
		Clan clan = null;
		for (String clanID : getAllClanIDs()) {
			for (Clan c : getClans) {
				if (c.getClanID().equals(clanID)) {
					clan = c;
				}
			}
			clans.put(getClanTag(clanID), clan.getPower());
		}
		PaginatedAssortment topClans = new PaginatedAssortment(p, null, clans);
		if (Bukkit.getVersion().contains("1.16")) {
			topClans.setListTitle("&7&m------------&7&l[&6&oTop Clans&7&l]&7&m------------");
			topClans.setNormalText("");
			topClans.setHoverText(" &#787674# &#0eaccc&l" + "%s" + " &#00fffb&o" + "%s" + " &#787674: &#ff7700&l" + "%s");
			topClans.setHoverTextMessage("&6" + "%s" + " &a&oplaces &7#&6" + "%s" + "&a&o on page " + "%s" + ".");
			topClans.setListBorder("&7&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
		} else {
			topClans.setListTitle("&7&m------------&7&l[&6&oTop Clans&7&l]&7&m------------");
			topClans.setNormalText("");
			topClans.setHoverText(" &7# &3&l" + "%s" + " &b&o" + "%s" + " &7: &6&l" + "%s");
			topClans.setHoverTextMessage("&6" + "%s" + " &a&oplaces &7#&6" + "%s" + "&a&o on page " + "%s" + ".");
			topClans.setListBorder("&7&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
		}
		topClans.setLinesPerPage(10);
		topClans.setNavigateCommand("c top");
		topClans.setCommandToRun("c info %s");
		topClans.exportSorted(PaginatedAssortment.MapType.DOUBLE, page);
	}

}