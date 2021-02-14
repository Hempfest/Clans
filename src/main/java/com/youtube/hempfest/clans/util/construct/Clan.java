package com.youtube.hempfest.clans.util.construct;

import com.github.ms5984.clans.clansbanks.ClansBanks;
import com.github.ms5984.clans.clansbanks.api.BanksAPI;
import com.github.ms5984.clans.clansbanks.api.ClanBank;
import com.github.sanctum.labyrinth.library.HUID;
import com.youtube.hempfest.clans.HempfestClans;
import com.youtube.hempfest.clans.util.data.Config;
import com.youtube.hempfest.clans.util.data.ConfigType;
import com.youtube.hempfest.clans.util.data.DataManager;
import com.youtube.hempfest.clans.util.events.OtherInformationAdaptEvent;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Clan implements Serializable {

	private final String clanID;

	private DataManager dm() {
		return new DataManager(clanID, null);
	}

	public static ClanUtil clanUtil = new ClanUtil();

	/**
	 * @param clanID Create a clan object using a clanID
	 *               See ClanUtil for getting id.
	 *               If using a player object it is recommended to
	 *               use the clanManager from main class HempfestClans.java
	 */
	public Clan(String clanID) {
		this.clanID = clanID;
	}

	/**
	 * @param p variable constructor access is deprecated and replaced by
	 *          {@link #Clan(String clanID)}
	 * @deprecated As of version 2.0.6
	 */
	@Deprecated
	public Clan(String clanID, Player p) {
		this.clanID = clanID;
	}

	/**
	 * @return A persistent data id.
	 */
	public HUID getId() {
		DataManager dm = new DataManager(clanID);
		Config clan = dm.getFile(ConfigType.CLAN_FILE);
		HUID result = null;
		try {
			 result = HUID.fromString(clan.getConfig().getString("NO-ID"));
		} catch (NullPointerException ignored) {
		}
		return result;
	}

	/**
	 * @return A persistent data id by set delimiter
	 */
	public HUID getId(int id) {
		DataManager dm = new DataManager(clanID);
		Config clan = dm.getFile(ConfigType.CLAN_FILE);
		HUID result = null;
		try {
			for (String d : clan.getConfig().getConfigurationSection("Data").getKeys(false)) {
				if (clan.getConfig().getInt("Data." + d) == id) {
					result = HUID.fromString(d);
					break;
				}
			}
		} catch (NullPointerException ignored) {
		}
		return result;
	}

	/**
	 * @return true if friendly fire
	 */
	public boolean isFriendlyFire() {
		DataManager dm = new DataManager(clanID);
		Config clan = dm.getFile(ConfigType.CLAN_FILE);
		return clan.getConfig().getBoolean("friendly-fire");
	}

	public void setFriendlyFire(boolean friendlyFire) {
		DataManager dm = new DataManager(clanID);
		Config clan = dm.getFile(ConfigType.CLAN_FILE);
		clan.getConfig().set("friendly-fire", friendlyFire);
		clan.saveConfig();
	}

	/**
	 * @param loc Update the clans base to a specified location.
	 */
	public void updateBase(Location loc) {
		Config clan = dm().getFile(ConfigType.CLAN_FILE);
		double x = loc.getX();
		double y = loc.getY();
		double z = loc.getZ();
		float yaw = loc.getYaw();
		float pitch = loc.getPitch();
		World w = loc.getWorld();
		clan.getConfig().set("base.x", x);
		clan.getConfig().set("base.y", y);
		clan.getConfig().set("base.z", z);
		List<Float> exact = new ArrayList<>();
		exact.add(yaw);
		exact.add(pitch);
		clan.getConfig().set("base.float", exact);
		clan.getConfig().set("base.world", w.getName());
		clan.saveConfig();
		String format = String.format(HempfestClans.getMain().getConfig().getString("Response.base"), loc.getWorld().getName());
		messageClan(format);
	}

	/**
	 * @return Gets the clanID stored within the clan object.
	 */
	public String getClanID() {
		return clanID;
	}

	/**
	 * @return Gets the location of the clans base.
	 */
	public Location getBase() {
		Config clan = dm().getFile(ConfigType.CLAN_FILE);
		try {
			double x = clan.getConfig().getDouble("base.x");
			double y = clan.getConfig().getDouble("base.y");
			double z = clan.getConfig().getDouble("base.z");
			float yaw = clan.getConfig().getFloatList("base.float").get(0);
			float pitch = clan.getConfig().getFloatList("base.float").get(1);
			World w = Bukkit.getWorld(clan.getConfig().getString("base.world"));
			if (w == null) {
				w = Bukkit.getWorld(Objects.requireNonNull(HempfestClans.getMain().getConfig().getString("Clans.raid-shield.main-world")));
			}
			Location toGet = new Location(w, x, y, z, yaw, pitch);
			return toGet;
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	/**
	 * @return Get the clans information list as an array.
	 */
	public String[] getClanInfo() {
		Config clan = dm().getFile(ConfigType.CLAN_FILE);
		List<String> array = new ArrayList<>();
		String password = clan.getConfig().getString("password");
		String owner = clan.getConfig().getString("owner");
		List<String> members = clan.getConfig().getStringList("members");
		List<String> mods = clan.getConfig().getStringList("moderators");
		List<String> admins = clan.getConfig().getStringList("admins");
		List<String> allies = clan.getConfig().getStringList("allies");
		List<String> enemies = clan.getConfig().getStringList("enemies");
		String status = "LOCKED";
		if (password == null)
			status = "OPEN";
		array.add(" ");
		array.add("&2&lClan&7: &f" + clanUtil.getColor(getChatColor()) + clanUtil.getClanTag(clanID));
		array.add("&f&m---------------------------");
		array.add("&2" + clanUtil.getRankTag("Owner") + ": &f" + owner);
		array.add("&2Status: &f" + status);
		array.add("&2&lPower [&e" + format(String.valueOf(getPower())) + "&2&l]");
		if (getBase() != null)
			array.add("&2Base: &aSet");
		if (getBase() == null)
			array.add("&2Base: &7Not set");
		array.add("&2" + clanUtil.getRankTag("Admin") + "s [&b" + admins.size() + "&2]");
		array.add("&2" + clanUtil.getRankTag("Moderator") + "s [&e" + mods.size() + "&2]");
		array.add("&2Claims [&e" + getOwnedClaims().length + "&2]");
		array.add("&f&m---------------------------");
		if (allies.isEmpty())
			array.add("&2Allies [&b" + "0" + "&2]");
		if (allies.size() > 0) {
			array.add("&2Allies [&b" + allies.size() + "&2]");
			for (String clanId : allies) {
				array.add("&f- &e&o" + clanUtil.getClanTag(clanId));
			}
		}
		for (String clanId : clanUtil.getAllClanIDs()) {
			if (clanUtil.getEnemies(clanId).contains(clanID)) {
				enemies.add(clanId);
			}
		}
		if (enemies.isEmpty())
			array.add("&2Enemies [&b" + "0" + "&2]");
		if (enemies.size() > 0) {
			array.add("&2Enemies [&b" + enemies.size() + "&2]");
			for (String clanId : enemies) {
				array.add("&f- &c&o" + clanUtil.getClanTag(clanId));
			}
		}
		array.add("&f&m---------------------------");
		array.add("&n" + clanUtil.getRankTag("Member") + "s&r [&7" + members.size() + "&r] - " + members.toString());
		array.add(" ");
		OtherInformationAdaptEvent event = new OtherInformationAdaptEvent(array, clanID);
		Bukkit.getPluginManager().callEvent(event);
		return event.getInsertions().toArray(new String[0]);
	}

	/**
	 * @return The specified clan's password.
	 */
	public String getPassword() {
		DataManager dm = new DataManager(clanID, null);
		Config clan = dm.getFile(ConfigType.CLAN_FILE);
		return clan.getConfig().getString("password");
	}

	/**
	 * @param newPassword Change the clans password to a new one of specification.
	 */
	public void changePassword(String newPassword) {
		Config c = dm().getFile(ConfigType.CLAN_FILE);
		if (newPassword.equals("empty")) {
			c.getConfig().set("password", null);
			c.saveConfig();
			messageClan("&b&o&nThe clan status was set to&r &a&oOPEN.");
			return;
		}
		c.getConfig().set("password", newPassword);
		c.saveConfig();
		messageClan(String.format(HempfestClans.getMain().getConfig().getString("Response.password"), newPassword));
	}

	/**
	 * @param newTag Change the clans name tag to a new one of specification.
	 */
	public void changeTag(String newTag) {
		Config c = dm().getFile(ConfigType.CLAN_FILE);
		c.getConfig().set("name", newTag);
		c.saveConfig();
		String format = String.format(HempfestClans.getMain().getConfig().getString("Response.tag"), newTag);
		messageClan(format);
	}


	/**
	 * @param newColor Change the clans color to a new one of specification using
	 *                 minecraft color codes by name.
	 */
	public void changeColor(String newColor) {
		Config c = dm().getFile(ConfigType.CLAN_FILE);
		c.getConfig().set("name-color", newColor);
		c.saveConfig();
		messageClan(clanUtil.getColor(newColor) + "The clan name color has been changed.");
	}

	/**
	 * @return Gets the clan objects clan tag
	 */
	public String getClanTag() {
		Config c = dm().getFile(ConfigType.CLAN_FILE);
		return c.getConfig().getString("name");
	}

	/**
	 * @return Gets the clan objects clan tag color
	 */
	public String getChatColor() {
		Config c = dm().getFile(ConfigType.CLAN_FILE);
		if (c.getConfig().getString("name-color") == null) {
			return "WHITE";
		}
		return c.getConfig().getString("name-color");
	}

	/**
	 * @return Gets the member list of the clan object.
	 */
	public String[] getMembers() {
		Config c = dm().getFile(ConfigType.CLAN_FILE);
		List<String> array = new ArrayList<>(c.getConfig().getStringList("members"));
		return array.toArray(new String[0]);
	}

	/**
	 * @param message Send a message of specification to the clan
	 */
	public void messageClan(String message) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (clanUtil.getClan(p) != null && clanUtil.getClan(p).equals(clanID)) {
				p.sendMessage(clanUtil.color("&7[&6&l" + clanUtil.getClanTag(clanID) + "&7] " + message));
			}
		}
	}

	public int maxClaims() {
		if (!HempfestClans.getInstance().dataManager.claimEffect()) {
			return 0;
		}
		return (int) (getMembers().length + Math.cbrt(getPower()));
	}

	/**
	 * @param amount Format a number to ##.## format
	 * @return Gets the formatted result as a string.
	 */
	public String format(String amount) {
		// Assigning value to BigDecimal object b1
		BigDecimal b1 = new BigDecimal(amount);
		MathContext m = new MathContext(3); // 4 precision
		// b1 is rounded using m
		BigDecimal b2 = b1.round(m);
		Locale loc = Locale.US;
		switch (HempfestClans.getMain().getConfig().getString("Formatting.locale")) {
			case "fr":
				loc = Locale.FRANCE;
				break;

			case "de":
				loc = Locale.GERMANY;
				break;
		}
		return NumberFormat.getNumberInstance(loc).format(b2.doubleValue());
	}

	/**
	 * @param amount Give the clan a specified amount of power.
	 */
	public void givePower(double amount) {
		Config c = dm().getFile(ConfigType.CLAN_FILE);
		if (!c.getConfig().isDouble("bonus")) {
			c.getConfig().set("bonus", 0.0);
			c.saveConfig();
		}
		double current = c.getConfig().getDouble("bonus");
		c.getConfig().set("bonus", (current + amount));
		c.saveConfig();
		messageClan("&a&oNew power was gained. The clan grows stronger..");
		System.out.println(String.format("[%s] - Gave " + '"' + amount + '"' + " power to clan " + '"' + clanID + '"', HempfestClans.getInstance().getDescription().getName()));
	}

	/**
	 * @param amount Take a specified amount of power away from the clan.
	 */
	public void takePower(double amount) {
		Config c = dm().getFile(ConfigType.CLAN_FILE);
		if (!c.getConfig().isDouble("bonus")) {
			c.getConfig().set("bonus", 0.0);
			c.saveConfig();
		}
		double current = c.getConfig().getDouble("bonus");
		c.getConfig().set("bonus", (current - amount));
		c.saveConfig();
		messageClan("&c&oPower was stolen from us.. we need to earn it back");
		System.out.println(String.format("[%s] - Took " + '"' + amount + '"' + " power from clan " + '"' + clanID + '"', HempfestClans.getInstance().getDescription().getName()));
	}

	/**
	 * @return Gets the clans power level in double format.
	 */
	public double getPower() {
		double bal = 0;
		if (Bukkit.getPluginManager().isPluginEnabled("ClansBanks")) {
			BanksAPI api = ClansBanks.getAPI();
			ClanBank bank = api.getBank(Clan.clanUtil.getClan(clanID));
			bal = bank.getBalance().doubleValue();
		}
		Config c = dm().getFile(ConfigType.CLAN_FILE);
		double result = 0.0;
		double multiplier = 1.4;
		double add = getMembers().length + 0.56;
		int claimAmount = getOwnedClaims().length;
		result = result + add + (claimAmount * multiplier);
		double bonus = c.getConfig().getDouble("bonus");
		if (bal != 0) {
			if (HempfestClans.getMain().getConfig().getBoolean("Clans.bank-influence")) {
				bonus = bonus + (bal / 48.94);
			}
		}
		return result + bonus;
	}

	/**
	 * @return Gets a list of all owned clan chunks by claimID.
	 */
	public String[] getOwnedClaims() {
		DataManager dm = new DataManager("Regions", "Configuration");
		Config regions = dm.getFile(ConfigType.MISC_FILE);
		List<String> array = new ArrayList<>();
		for (String clan : clanUtil.getAllClanIDs()) {
			if (regions.getConfig().getConfigurationSection(clan + ".Claims") != null) {
				for (String claim : regions.getConfig().getConfigurationSection(clan + ".Claims").getKeys(false)) {
					if (clan.equals(clanID))
						array.add(claim);
				}
			}
		}
		return array.toArray(new String[0]);
	}

	/**
	 * @return Gets the list of allies for the specified clan.
	 */
	public List<String> getAllies() {
		if (!HempfestClans.clanAllies.containsKey(clanID)) {
			DataManager dm = new DataManager(clanID, null);
			Config clan = dm.getFile(ConfigType.CLAN_FILE);
			return new ArrayList<>(clan.getConfig().getStringList("allies"));
		}
		return HempfestClans.clanAllies.get(clanID);
	}

	/**
	 * @return Gets the list of enemies for the specified clan.
	 */
	public List<String> getEnemies() {
		if (!HempfestClans.clanEnemies.containsKey(clanID)) {
			DataManager dm = new DataManager(clanID, null);
			Config clan = dm.getFile(ConfigType.CLAN_FILE);
			return new ArrayList<>(clan.getConfig().getStringList("enemies"));
		}
		return HempfestClans.clanEnemies.get(clanID);
	}


}
