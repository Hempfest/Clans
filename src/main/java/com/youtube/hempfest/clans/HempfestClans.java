package com.youtube.hempfest.clans;

import com.google.gson.JsonObject;
import com.youtube.hempfest.clans.metadata.PersistentClan;
import com.youtube.hempfest.clans.util.JSONUrlParser;
import com.youtube.hempfest.clans.util.Metrics;
import com.youtube.hempfest.clans.util.Placeholders;
import com.youtube.hempfest.clans.util.construct.Claim;
import com.youtube.hempfest.clans.util.construct.Clan;
import com.youtube.hempfest.clans.util.construct.ClanUtil;
import com.youtube.hempfest.clans.util.data.Config;
import com.youtube.hempfest.clans.util.data.ConfigType;
import com.youtube.hempfest.clans.util.data.DataManager;
import com.youtube.hempfest.clans.util.events.ClaimResidentEvent;
import com.youtube.hempfest.clans.util.timers.SyncRaidShield;
import com.youtube.hempfest.hempcore.command.CommandBuilder;
import com.youtube.hempfest.hempcore.event.EventBuilder;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


public class HempfestClans extends JavaPlugin {

	private static HempfestClans instance;

	private final Logger log = Logger.getLogger("Minecraft");

	public DataManager dataManager = new DataManager();

	public List<String> tabList = new ArrayList<>();

	public static HashMap<UUID, String> playerClan = new HashMap<>();

	public static HashMap<UUID, Clan> clanManager = new HashMap<>();

	public static HashMap<Player, String> idMode = new HashMap<>();

	public HashMap<String[], int[]> claimMap = new HashMap<>();

	public static HashMap<Player, String> chatMode = new HashMap<>();

	public static HashMap<String, List<String>> clanEnemies = new HashMap<>();

	public static HashMap<String, List<String>> clanAllies = new HashMap<>();

	public void onEnable() {
		getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
		getLogger().info("- Clans [Free]. Loading plugin information...");
		getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
		for (String ch : logo()) {
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			getLogger().info("- " + ch);
		}
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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
		EventBuilder builder = new EventBuilder(this);
		builder.compileFields("com.youtube.hempfest.clans.util.listener");
		CommandBuilder commandBuilder = new CommandBuilder(this);
		commandBuilder.compileFields("com.youtube.hempfest.clans.commands");
		dataManager.runCleaner();
		Clan.clanUtil.setRaidShield(true);
		refreshChat();
		runShieldTimer();
		log.info(String.format("[%s] - Beginning claim resident event", getDescription().getName()));
		Claim.claimUtil.loadClaims();
		Clan.clanUtil.loadClans();
		dataManager.performResidentEvent();
		for (Player p : Bukkit.getOnlinePlayers()) {
			DataManager data = new DataManager(p.getUniqueId().toString(), null);
			Config user = data.getFile(ConfigType.USER_FILE);
			if (user.getConfig().getString("Clan") != null) {
				playerClan.put(p.getUniqueId(), user.getConfig().getString("Clan"));
				getLogger().info("- Refilled user data. *RELOAD NOT SAFE*");
			}
		}
		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			new Placeholders(this).register();
			getLogger().info("- PlaceholderAPI found! Loading clans placeholders %clans_(name, rank, raidshield)%.");
		} else {
			getLogger().info("- PlaceholderAPI not found, placeholders will not work!");
		}
		registerMetrics(9234);
		getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
		boolean success;
		try {
			getLogger().info("- Attempting automatic clan meta query process..");
			PersistentClan.querySaved();
			success = true;
		} catch (NullPointerException e) {
			getLogger().info("- Process failed. No directory found to process.");
			getLogger().info("- Store a new instance of data for query to take effect on enable.");
			success = false;
		}
		if (success) {
			getLogger().info("- Query success! All found meta cached. (" + PersistentClan.getMetaDataContainer().length + ")");
		} else {
			getLogger().info("- Query failed! (SEE ABOVE FOR INFO)");
		}
	}

	private List<String> logo() {
		return new ArrayList<>(Arrays.asList("   ▄▄· ▄▄▌   ▄▄▄·  ▐ ▄ .▄▄ · ", "  ▐█ ▌▪██•  ▐█ ▀█ •█▌▐█▐█ ▀. ", "  ██ ▄▄██▪  ▄█▀▀█ ▐█▐▐▌▄▀▀▀█▄", "  ▐███▌▐█▌▐▌▐█ ▪▐▌██▐█▌▐█▄▪▐█", "  ·▀▀▀ .▀▀▀  ▀  ▀ ▀▀ █▪ ▀▀▀▀ "));
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
		} else {
			if (!clanManager.get(p.getUniqueId()).getClanID().equals(playerClan.get(p.getUniqueId()))) {
				clan = new Clan(new ClanUtil().getClan(p));
				clanManager.put(p.getUniqueId(), clan);
				return clan;
			}
			return clanManager.get(p.getUniqueId());
		}
	}

	public static Config getMain() {
		DataManager dm = new DataManager("Config", "Configuration");
		Config main = dm.getFile(ConfigType.MISC_FILE);
		if (!main.exists()) {
			InputStream is = getInstance().getResource("Config.yml");
			Config.copy(is, main.getFile());
		}
		return main;
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
