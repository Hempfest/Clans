package com.youtube.hempfest.clans;

import com.youtube.hempfest.clans.commands.Command;
import com.youtube.hempfest.clans.util.construct.ClanUtil;
import com.youtube.hempfest.clans.util.data.DataManager;
import com.youtube.hempfest.clans.util.events.ClaimResidentEvent;
import com.youtube.hempfest.clans.util.listener.EventListener;
import com.youtube.hempfest.clans.util.timers.SyncRaidShield;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.logging.Logger;



public class HempfestClans extends JavaPlugin {

	//Instance
	private static HempfestClans instance;
	private final Logger log = Logger.getLogger("Minecraft");
	private final PluginManager pm = getServer().getPluginManager();
	public ClanUtil shield = new ClanUtil();
	public static HashMap<Player, String> idMode = new HashMap<>();
	//Start server
	public void onEnable() {
		log.info(String.format("[%s] - Clans [Free] *NEW* remodel.", getDescription().getName()));
		setInstance(this);
		DataManager dm = new DataManager();
		Command commands = new Command();
		commands.registerAll();
		pm.registerEvents(new EventListener(), this);
		dm.copyDefaults();
		dm.runCleaner();
		shield.setRaidShield(true);
		refreshChat();
		runShieldTimer();
		log.info(String.format("[%s] - Beginning claim resident event", getDescription().getName()));
		dm.performResidentEvent();
	}
	
	public void onDisable() {
		log.info(String.format("[%s] - Goodbye friends...", getDescription().getName()));
			ClaimResidentEvent.claimID.clear();
			ClaimResidentEvent.invisibleResident.clear();
			ClaimResidentEvent.residents.clear();
			ClaimResidentEvent.tempStorage.clear();
	}

	private void refreshChat() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			ClanUtil.chatMode.put(p, "GLOBAL");
		}
	}

	private void runShieldTimer() {
		SyncRaidShield shield = new SyncRaidShield();
		shield.runTaskTimer(this, 10L, 10L);
	}

	public static HempfestClans getInstance() {
		return instance;
	}

	private void setInstance(HempfestClans instance) {
		HempfestClans.instance = instance;
	}


	

}
