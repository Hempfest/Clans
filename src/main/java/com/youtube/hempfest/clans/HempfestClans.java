package com.youtube.hempfest.clans;

import com.youtube.hempfest.clans.commands.Command;
import com.youtube.hempfest.clans.util.Metrics;
import com.youtube.hempfest.clans.util.Placeholders;
import com.youtube.hempfest.clans.util.construct.ClaimUtil;
import com.youtube.hempfest.clans.util.construct.ClanUtil;
import com.youtube.hempfest.clans.util.data.Config;
import com.youtube.hempfest.clans.util.data.ConfigType;
import com.youtube.hempfest.clans.util.data.DataManager;
import com.youtube.hempfest.clans.util.events.ClaimResidentEvent;
import com.youtube.hempfest.clans.util.listener.EventListener;
import com.youtube.hempfest.clans.util.timers.SyncRaidShield;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;



public class HempfestClans extends JavaPlugin {

	//Instance
	private static HempfestClans instance;
	private final Logger log = Logger.getLogger("Minecraft");
	private final PluginManager pm = getServer().getPluginManager();
	public ClanUtil clanUtil = new ClanUtil();
	public ClaimUtil claimUtil = new ClaimUtil();
	public static HashMap<UUID, String> playerClan = new HashMap<>();
	public static HashMap<Player, String> idMode = new HashMap<>();
	public boolean running;
	//Start server
	public void onEnable() {
		running = true;
		log.info(String.format("[%s] - More awesome, less chicken egg. {RE-VAMPED}", getDescription().getName()));
		setInstance(this);
		DataManager dm = new DataManager();
		dm.copyDefaults();
		Command commands = new Command();
		commands.registerAll();
		pm.registerEvents(new EventListener(), this);
		dm.runCleaner();
		clanUtil.setRaidShield(true);
		refreshChat();
		runShieldTimer();
		log.info(String.format("[%s] - Beginning claim resident event", getDescription().getName()));
		dm.performResidentEvent();
		for (Player p : Bukkit.getOnlinePlayers()) {
			DataManager data = new DataManager(p.getUniqueId().toString(), null);
			Config user = data.getFile(ConfigType.USER_FILE);
			if (user.getConfig().getString("Clan") != null) {
				playerClan.put(p.getUniqueId(), user.getConfig().getString("Clan"));
			}
		}
		if (pm.isPluginEnabled("PlaceholderAPI")) {
			new Placeholders(this).register();
			getLogger().info("- PlaceholderAPI found! Loading clans placeholders (3).");
		} else {
			getLogger().info("- PlaceholderAPI not found, placeholders will not work!");
		}
		registerMetrics(9234);
	}
	
	public void onDisable() {
		running = false;
		log.info(String.format("[%s] - Koala bears are nice :), until you get malled..", getDescription().getName()));
			ClaimResidentEvent.claimID.clear();
			ClaimResidentEvent.invisibleResident.clear();
			ClaimResidentEvent.residents.clear();
			ClaimResidentEvent.tempStorage.clear();
			idMode.clear();
			playerClan.clear();
	}

	private void refreshChat() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			ClanUtil.chatMode.put(p, "GLOBAL");
		}
	}

	public static Config getMain() {
		DataManager dm = new DataManager("Config", "Configuration");
		return dm.getFile(ConfigType.MISC_FILE);
	}

	private boolean configAllow() {
		return getMain().getConfig().getBoolean("Clans.raid-shield.allow");
	}

	private void runShieldTimer() {
		if (configAllow()) {
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
			if (ClaimUtil.claimingAllowed()) {
				result = "Yes";
			}
			return result;
		}));
		metrics.addCustomChart(new Metrics.SimplePie("using_raidshield", () -> {
			String result = "No";
			if (configAllow()) {
				result = "Yes";
			}
			return result;
		}));
		metrics.addCustomChart(new Metrics.SingleLineChart("total_logged_players", () -> clanUtil.getAllUsers().size()));
		metrics.addCustomChart(new Metrics.SingleLineChart("total_clans_made", () -> clanUtil.getAllClanIDs().size()));
	}
	

}
