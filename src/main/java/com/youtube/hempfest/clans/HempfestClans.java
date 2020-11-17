package com.youtube.hempfest.clans;

import com.google.gson.JsonObject;
import com.youtube.hempfest.clans.commands.Command;
import com.youtube.hempfest.clans.util.JSONUrlParser;
import com.youtube.hempfest.clans.util.Metrics;
import com.youtube.hempfest.clans.util.Placeholders;
import com.youtube.hempfest.clans.util.construct.Claim;
import com.youtube.hempfest.clans.util.construct.Clan;
import com.youtube.hempfest.clans.util.construct.ClanUtil;
import com.youtube.hempfest.clans.util.data.Config;
import com.youtube.hempfest.clans.util.data.ConfigType;
import com.youtube.hempfest.clans.util.data.DataManager;
import com.youtube.hempfest.clans.util.dynmap.HempfestDynmapIntegration;
import com.youtube.hempfest.clans.util.events.ClaimResidentEvent;
import com.youtube.hempfest.clans.util.listener.EventListener;
import com.youtube.hempfest.clans.util.timers.SyncRaidShield;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


public class HempfestClans extends JavaPlugin {

	private static HempfestClans instance;

	private final Logger log = Logger.getLogger("Minecraft");

	private final PluginManager pm = getServer().getPluginManager();

	public HempfestDynmapIntegration integration = new HempfestDynmapIntegration();

	public DataManager dataManager = new DataManager();

	public static HashMap<UUID, String> playerClan = new HashMap<>();

	public static HashMap<UUID, Clan> clanManager = new HashMap<>();

	public static HashMap<Player, String> idMode = new HashMap<>();

	public HashMap<String[], int[]> claimMap = new HashMap<>();

	public static HashMap<Player, String> chatMode = new HashMap<>();

	public static HashMap<String, List<String>> clanEnemies = new HashMap<>();

	public static HashMap<String, List<String>> clanAllies = new HashMap<>();

	public void onEnable() {
		if(JSONUrlParser.jsonGetRequest("https://clans-startstop-messages.herokuapp.com/") != null) {
			JsonObject startMessageObject = JSONUrlParser.jsonGetRequest("https://clans-startstop-messages.herokuapp.com/");
			String startMessage = "";
			ArrayList<String> messages = new ArrayList<>();
			for (int i = 0; i < startMessageObject.getAsJsonArray("startMessages").size(); i++) {
				messages.add(startMessageObject.getAsJsonArray("startMessages").get(i).toString());
			}
			startMessage = messages.get(new Random().nextInt(messages.size())).replaceAll("\"", "");
			log.info(String.format("[%s] - " + startMessage, getDescription().getName()));
		}
		setInstance(this);
		dataManager.copyDefaults();
		Command commands = new Command();
		commands.registerAll();
		pm.registerEvents(new EventListener(), this);
		dataManager.runCleaner();
		Clan.clanUtil.setRaidShield(true);
		refreshChat();
		runShieldTimer();
		log.info(String.format("[%s] - Beginning claim resident event", getDescription().getName()));
		getLogger().info("- Loading claim data");
		Claim.claimUtil.loadClaims();
		getLogger().info("- Claim data cached.");
		getLogger().info("- Loading clan data");
		Clan.clanUtil.loadClans();
		getLogger().info("- Clan data cached.");
		dataManager.performResidentEvent();
		for (Player p : Bukkit.getOnlinePlayers()) {
			DataManager data = new DataManager(p.getUniqueId().toString(), null);
			Config user = data.getFile(ConfigType.USER_FILE);
			if (user.getConfig().getString("Clan") != null) {
				playerClan.put(p.getUniqueId(), user.getConfig().getString("Clan"));
				getLogger().info("- Refilled user data. *RELOAD NOT SAFE*");
			}
		}
		if (pm.isPluginEnabled("PlaceholderAPI")) {
			new Placeholders(this).register();
			getLogger().info("- PlaceholderAPI found! Loading clans placeholders %clans_(name, rank, raidshield)%.");
		} else {
			getLogger().info("- PlaceholderAPI not found, placeholders will not work!");
		}
		registerMetrics(9234);
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
			if (Bukkit.getPluginManager().isPluginEnabled("dynmap")) {
				getLogger().info("- Dynmap found initializing API...");
				integration.registerDynmap();
				getLogger().info("- API successfully initialized");
			}
		}, 2);
	}


	public void onDisable() {
		if(JSONUrlParser.jsonGetRequest("https://clans-startstop-messages.herokuapp.com/") != null) {
			JsonObject stopMessageObject = JSONUrlParser.jsonGetRequest("https://clans-startstop-messages.herokuapp.com/");
			String stopMessage = "";
			ArrayList<String> messages = new ArrayList<>();
			for (int i = 0; i < stopMessageObject.getAsJsonArray("stopMessages").size(); i++) {
				messages.add(stopMessageObject.getAsJsonArray("stopMessages").get(i).toString());
			}
			stopMessage = messages.get(new Random().nextInt(messages.size())).replaceAll("\"", "");
			log.info(String.format("[%s] - " + stopMessage, getDescription().getName()));
		}
		ClaimResidentEvent.claimID.clear();
		ClaimResidentEvent.invisibleResident.clear();
		ClaimResidentEvent.residents.clear();
		ClaimResidentEvent.tempStorage.clear();
		idMode.clear();
		playerClan.clear();
		claimMap.clear();
		chatMode.clear();
		clanAllies.clear();
		clanEnemies.clear();
		clanManager.clear();
		Clan.clanUtil.getClans.clear();
	}

	private void refreshChat() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			chatMode.put(p, "GLOBAL");
		}
	}

	public static Clan clanManager(Player p) {
		Clan clan;
		if (!clanManager.containsKey(p.getUniqueId())) {
			clan = new Clan(new ClanUtil().getClan(p));
			clanManager.put(p.getUniqueId(), clan);
			return clan;
		} else
			return clanManager.get(p.getUniqueId());
	}

	public static Config getMain() {
		DataManager dm = new DataManager("Config", "Configuration");
		return dm.getFile(ConfigType.MISC_FILE);
	}

	private void runShieldTimer() {
		boolean configAllow = getMain().getConfig().getBoolean("Clans.raid-shield.allow");
		if (configAllow) {
			SyncRaidShield shield = new SyncRaidShield();
			shield.runTaskTimer(this, 10L, 10L);
			log.info(String.format("[%s] - Running allowance for RaidShield event", getDescription().getName()));
		} else {
			log.info(String.format("[%s] - RaidShield disabled. Denying runnable.", getDescription().getName()));
		}
	}

	public static HempfestClans getInstance() {
		return instance;
	}

	private void setInstance(HempfestClans instance) {
		HempfestClans.instance = instance;
	}

	private void registerMetrics(int ID) {
		Metrics metrics = new Metrics(this, ID);
		metrics.addCustomChart(new Metrics.SimplePie("using_claiming", () -> {
			String result = "No";
			if (Claim.claimUtil.claimingAllowed()) {
				result = "Yes";
			}
			return result;
		}));
		boolean configAllow = getMain().getConfig().getBoolean("Clans.raid-shield.allow");
		metrics.addCustomChart(new Metrics.SimplePie("using_raidshield", () -> {
			String result = "No";
			if (configAllow) {
				result = "Yes";
			}
			return result;
		}));
		metrics.addCustomChart(new Metrics.SimplePie("used_prefix", () -> ChatColor.stripColor(getMain().getConfig().getString("Formatting.prefix"))));
		metrics.addCustomChart(new Metrics.SingleLineChart("total_logged_players", () -> Clan.clanUtil.getAllUsers().size()));
		metrics.addCustomChart(new Metrics.SingleLineChart("total_clans_made", () -> Clan.clanUtil.getAllClanIDs().size()));
		getLogger().info("- Converting bStats metrics tables.");
	}
}
