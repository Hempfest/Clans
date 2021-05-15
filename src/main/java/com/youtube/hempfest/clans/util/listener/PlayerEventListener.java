package com.youtube.hempfest.clans.util.listener;

import com.github.sanctum.labyrinth.task.Schedule;
import com.youtube.hempfest.clans.HempfestClans;
import com.youtube.hempfest.clans.Update;
import com.youtube.hempfest.clans.util.construct.ClaimManager;
import com.youtube.hempfest.clans.util.construct.ClaimUtil;
import com.youtube.hempfest.clans.util.construct.Clan;
import com.youtube.hempfest.clans.util.construct.ClanUtil;
import com.youtube.hempfest.clans.util.data.Config;
import com.youtube.hempfest.clans.util.data.ConfigType;
import com.youtube.hempfest.clans.util.data.DataManager;
import com.youtube.hempfest.clans.util.events.ClaimInteractEvent;
import com.youtube.hempfest.clans.util.events.ClaimResidentEvent;
import com.youtube.hempfest.clans.util.events.PlayerKillPlayerEvent;
import com.youtube.hempfest.clans.util.events.WildernessInhabitantEvent;
import com.youtube.hempfest.clans.util.misc.Member;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerEventListener implements Listener {

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		Schedule.sync(() -> {
			if (HempfestClans.getInstance().dataManager.get(p).getConfig().getString("Clan") != null) {
				Config cl = Config.get(HempfestClans.getInstance().dataManager.get(p).getConfig().getString("Clan"), "Clans");
				if (cl.getConfig().getString("name") == null) {
					HempfestClans.getInstance().playerClan.remove(p.getUniqueId());
					DataManager dm = new DataManager(p.getUniqueId().toString(), null);
					Config user = dm.getFile(ConfigType.USER_FILE);
					user.getConfig().set("Clan", null);
					user.saveConfig();
					if (HempfestClans.getInstance().dataManager.prefixedTagsAllowed()) {
						Member.removePrefix(p);
					}
					Config region = ClaimUtil.regions;
					region.getConfig().set(cl.getName(), null);
					region.saveConfig();
					HempfestClans.getInstance().claimManager.refresh();
					cl.delete();
					if (p.isOp()) {
						Clan.clanUtil.sendMessage(p, "&c&oERROR &8> &4Clan tag cannot be null.");
						Clan.clanUtil.sendMessage(p, "&c&oSomething went wrong while retrieving data for the clan " + '"' + cl.getName() + '"');
						Clan.clanUtil.sendMessage(p, "&e&oConsult a developer for this issue could be a bug or mis-use of API through third party use.");
					} else {
						p.sendMessage(Clan.clanUtil.color(Clan.clanUtil.getPrefix() + " Your clan was disbanded due to owner dismissal.."));
					}
					HempfestClans.getInstance().getLogger().info("- Clearing clan data for un-known clan " + '"' + cl.getName() + '"');
					return;
				}
				for (String ally : Clan.clanUtil.getAllies(Clan.clanUtil.getClan(p))) {
					if (!Clan.clanUtil.getAllClanIDs().contains(ally)) {
						Clan.clanUtil.removeAlly(Clan.clanUtil.getClan(p), ally);
						break;
					}
				}
				for (String enemy : Clan.clanUtil.getEnemies(Clan.clanUtil.getClan(p))) {
					if (!Clan.clanUtil.getAllClanIDs().contains(enemy)) {
						Clan.clanUtil.removeEnemy(Clan.clanUtil.getClan(p), enemy);
						break;
					}
				}
				for (String allyRe : Clan.clanUtil.getAllyRequests(Clan.clanUtil.getClan(p))) {
					if (!Clan.clanUtil.getAllClanIDs().contains(allyRe)) {
						List<String> allies = Clan.clanUtil.getAllies(Clan.clanUtil.getClan(p));
						allies.remove(allyRe);
						cl.getConfig().set("ally-requests", allies);
						cl.saveConfig();
						break;
					}
				}
			}
		}).debug().cancelAfter(p).repeat(10, 10);
		Schedule.sync(() -> {
			if (!ClaimManager.getInstance().isInClaim(p.getLocation())) {
				WildernessInhabitantEvent event = new WildernessInhabitantEvent(p);
				Bukkit.getPluginManager().callEvent(event);
				if (!event.isCancelled()) {
					event.handleUpdate();
				}
			} else {
				ClaimResidentEvent event = new ClaimResidentEvent(p);
				Bukkit.getPluginManager().callEvent(event);
				if (!event.isCancelled()) {
					event.handleUpdate();
				}
			}
		}).cancelAfter(p).repeat(2, 20);
		if (HempfestClans.getInstance().dataManager.get(p).getConfig().getString("Clan") != null) {
			DataManager dm = new DataManager(HempfestClans.getInstance().dataManager.get(p).getConfig().getString("Clan"));
			Config clan = dm.getFile(ConfigType.CLAN_FILE);
			if (clan.getConfig().getString("name") != null) {
				HempfestClans.getInstance().playerClan.put(p.getUniqueId(), HempfestClans.getInstance().dataManager.get(p).getConfig().getString("Clan"));
				HempfestClans.clanEnemies.put(Clan.clanUtil.getClan(p), new ArrayList<>(clan.getConfig().getStringList("enemies")));
				HempfestClans.clanAllies.put(Clan.clanUtil.getClan(p), new ArrayList<>(clan.getConfig().getStringList("allies")));
				if (HempfestClans.getInstance().dataManager.prefixedTagsAllowed()) {
					Clan c = HempfestClans.clanManager(p);
					Member.setPrefix(p, "&7[" + Clan.clanUtil.getColor(c.getChatColor()) + c.getClanTag() + "&7] ");
				}
			}
		}
		ClanUtil.updateUsername(p);
		HempfestClans.chatMode.put(p, "GLOBAL");
		if (p.isOp()) {
			Update check = new Update(HempfestClans.getInstance());
			try {
				if (check.hasUpdate()) {
					Clan.clanUtil.sendMessage(p, "&a&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬oO[&fUpdate&a&l&m]Oo▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
					Clan.clanUtil.sendMessage(p, "&eNew version: &6Clans [Free] &f" + check.getLatestVersion());
					Clan.clanUtil.sendMessage(p, "&e&oDownload: &f&n" + check.getResourceURL());
					Clan.clanUtil.sendMessage(p, "&a&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
				}
			} catch (Exception ignored) {
			}
		}
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		if (Clan.clanUtil.getClan(p) != null) {
			Clan c = HempfestClans.clanManager(p);
			for (String player : c.getMembers()) {
				int clanSize = c.getMembers().length;
				int offlineSize = 0;
				if (!Bukkit.getOfflinePlayer(Clan.clanUtil.getUserID(player)).isOnline()) {
					offlineSize++;
				}
				if (offlineSize == clanSize) {
					HempfestClans.clanEnemies.clear();
					HempfestClans.clanAllies.clear();
				}
			}
		}
		HempfestClans.wildernessInhabitants.remove(p);
		HempfestClans.clanManager.remove(p.getUniqueId());
		HempfestClans.residents.removeIf(resident -> resident.getPlayer().getName().equals(p.getName()));
		HempfestClans.getInstance().playerClan.remove(p.getUniqueId());
		if (HempfestClans.getInstance().dataManager.prefixedTagsAllowed()) {
			Member.removePrefix(p);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (event.getEntity().getKiller() != null) {
			Player p = event.getEntity().getKiller();
			Player target = event.getEntity();
			PlayerKillPlayerEvent e = new PlayerKillPlayerEvent(p, target);
			Bukkit.getPluginManager().callEvent(e);
			e.perform();
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBucketRelease(PlayerBucketEmptyEvent event) {
		ClaimInteractEvent e = new ClaimInteractEvent(event.getPlayer(), event.getBlock().getLocation());
		Bukkit.getPluginManager().callEvent(e);
		e.handleCheck();
		if (e.isCancelled()) event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBucketFill(PlayerBucketFillEvent event) {
		ClaimInteractEvent e = new ClaimInteractEvent(event.getPlayer(), event.getBlock().getLocation());
		Bukkit.getPluginManager().callEvent(e);
		e.handleCheck();
		if (e.isCancelled()) event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockInteract(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			ClaimInteractEvent e = new ClaimInteractEvent(event.getPlayer(), event.getClickedBlock().getLocation());
			Bukkit.getPluginManager().callEvent(e);
			e.handleCheck();
			if (e.isCancelled()) {
				event.setCancelled(e.isCancelled());
			}
		}
	}

}
