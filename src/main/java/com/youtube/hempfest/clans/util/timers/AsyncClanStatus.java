package com.youtube.hempfest.clans.util.timers;

import com.youtube.hempfest.clans.HempfestClans;
import com.youtube.hempfest.clans.util.construct.ClanUtil;
import com.youtube.hempfest.clans.util.data.Config;
import com.youtube.hempfest.clans.util.data.ConfigType;
import com.youtube.hempfest.clans.util.data.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class AsyncClanStatus extends BukkitRunnable {

    @Override
    public void run() {
        ClanUtil clanUtil = HempfestClans.getInstance().clanUtil;
        if (HempfestClans.getInstance().running) {
            if (Bukkit.getOnlinePlayers().size() > 0) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (clanUtil.getClan(p) != null) {
                        DataManager cm = new DataManager(clanUtil.getClan(p), null);
                        Config cl = cm.getFile(ConfigType.CLAN_FILE);
                        if (!cl.exists()) {
                            DataManager dm = new DataManager(p.getUniqueId().toString(), null);
                            Config user = dm.getFile(ConfigType.USER_FILE);
                            user.getConfig().set("Clan", null);
                            user.saveConfig();
                            HempfestClans.playerClan.remove(p.getUniqueId());
                            p.sendMessage(clanUtil.color(clanUtil.getPrefix() + " Your clan was disbanded due to owner dismissal.."));
                            return;
                        }
                        String name = cl.getConfig().getString("name");
                        if (name == null) {
                            if (p.isOp()) {
                                clanUtil.sendMessage(p, "&c&oERROR &8> &4Clan tag cannot be null.");
                                clanUtil.sendMessage(p, "&c&oSomething went wrong while retrieving data for the clan " + '"' + cl.getName() + '"');
                                clanUtil.sendMessage(p, "&e&oConsult a developer for this issue could be a bug or mis-use of API through third party use.");
                            }
                            HempfestClans.getInstance().getLogger().info("- Clearing clan data for un-known clan " + '"' + cl.getName() + '"');
                            cl.delete();
                            return;
                        }
                        for (String ally : clanUtil.getAllies(clanUtil.getClan(p))) {
                            if (!clanUtil.getAllClanIDs().contains(ally)) {
                                List<String> allies = clanUtil.getAllies(clanUtil.getClan(p));
                                allies.remove(ally);
                                cl.getConfig().set("allies", allies);
                                cl.saveConfig();
                            }
                        }
                        for (String enemy : clanUtil.getEnemies(clanUtil.getClan(p))) {
                            if (!clanUtil.getAllClanIDs().contains(enemy)) {
                                List<String> enemies = clanUtil.getEnemies(clanUtil.getClan(p));
                                enemies.remove(enemy);
                                cl.getConfig().set("enemies", enemies);
                                cl.saveConfig();
                            }
                        }
                        for (String allyRe : clanUtil.getAllyRequests(clanUtil.getClan(p))) {
                            if (!clanUtil.getAllClanIDs().contains(allyRe)) {
                                List<String> allies = clanUtil.getAllies(clanUtil.getClan(p));
                                allies.remove(allyRe);
                                cl.getConfig().set("ally-requests", allies);
                                cl.saveConfig();
                            }
                        }
                    }
                }
            }
        }
    }
}
