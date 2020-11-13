package com.youtube.hempfest.clans.util.listener;

import com.youtube.hempfest.clans.HempfestClans;
import com.youtube.hempfest.clans.util.StringLibrary;
import com.youtube.hempfest.clans.util.construct.Clan;
import com.youtube.hempfest.clans.util.construct.ClanUtil;
import com.youtube.hempfest.clans.util.data.Config;
import com.youtube.hempfest.clans.util.data.ConfigType;
import com.youtube.hempfest.clans.util.data.DataManager;
import com.youtube.hempfest.clans.util.events.ClaimBuildEvent;
import com.youtube.hempfest.clans.util.events.ClaimResidentEvent;
import com.youtube.hempfest.clans.util.events.PlayerKillPlayerEvent;
import com.youtube.hempfest.clans.util.events.PlayerPunchPlayerEvent;
import com.youtube.hempfest.clans.util.events.PlayerShootPlayerEvent;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventListener implements Listener {


    public Config playerData(Player p) {
        DataManager dm = new DataManager(p.getUniqueId().toString(), null);
        return dm.getFile(ConfigType.USER_FILE);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (playerData(p).getConfig().getString("Clan") != null) {
            HempfestClans.playerClan.put(p.getUniqueId(), playerData(p).getConfig().getString("Clan"));
            DataManager dm = new DataManager(Clan.clanUtil.getClan(p), null);
            Config clan = dm.getFile(ConfigType.CLAN_FILE);
            HempfestClans.clanEnemies.put(Clan.clanUtil.getClan(p), new ArrayList<>(clan.getConfig().getStringList("enemies")));
            HempfestClans.clanAllies.put(Clan.clanUtil.getClan(p), new ArrayList<>(clan.getConfig().getStringList("allies")));
        }
        ClanUtil.updateUsername(p);
        HempfestClans.chatMode.put(p, "GLOBAL");

    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (Clan.clanUtil.getClan(p) != null) {
            Clan c = new Clan(Clan.clanUtil.getClan(p), p);
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
        ClaimResidentEvent.claimID.remove(p.getName());
        ClaimResidentEvent.invisibleResident.remove(p.getUniqueId());
        ClaimResidentEvent.residents.remove(p.getName());
        ClaimResidentEvent.tempStorage.remove(p.getUniqueId());
        HempfestClans.playerClan.remove(p.getUniqueId());
    }

    private String chatMode(Player p) {
        return HempfestClans.chatMode.get(p);
    }

    private static final DataManager dm = new DataManager();

    @EventHandler(priority = EventPriority.HIGH)
    public void onPrefixApply(AsyncPlayerChatEvent event) {
        Player p = event.getPlayer();
        ClanUtil clanUtil = Clan.clanUtil;

        if (chatMode(p).equals("GLOBAL")) {
            Config main = HempfestClans.getMain();
            if (main.getConfig().getBoolean("Formatting.allow")) {
                if (clanUtil.getClan(p) != null) {
                    Clan clan = new Clan(clanUtil.getClan(p));
                    String clanName = clan.getClanTag();
                    StringLibrary lib = new StringLibrary();
                    String rank;
                    clanName = clanUtil.getColor(clan.getChatColor()) + clanName;
                    switch (lib.getRankStyle()) {
                        case "WORDLESS":
                            rank = lib.getWordlessStyle(clanUtil.getRank(p));
                            event.setFormat(lib.color(String.format(lib.getChatFormat(), rank, clanName)) + " " + event.getFormat());
                            break;
                        case "FULL":
                            rank = lib.getFullStyle(clanUtil.getRank(p));
                            event.setFormat(lib.color(String.format(lib.getChatFormat(), rank, clanName)) + " " + event.getFormat());
                            break;
                    }
                }
            }
            return;
        }
        if (chatMode(p).equals("CLAN")) {
            if (!event.isCancelled()) {
                dm.formatClanChat(p, event.getRecipients(), event.getMessage());
                event.setCancelled(true);
                return;
            }
        }
        if (chatMode(p).equals("ALLY")) {
            if (!event.isCancelled()) {
                dm.formatAllyChat(p, event.getRecipients(), event.getMessage());
                event.setCancelled(true);
                return;
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
    if (event.getEntity().getShooter() instanceof Player) {
        Player p = (Player) event.getEntity().getShooter();
        ClaimBuildEvent e = new ClaimBuildEvent(p, event.getEntity().getLocation());
        Bukkit.getPluginManager().callEvent(e);
        e.handleCheck();
        if (e.isCancelled()) {
            event.getEntity().remove();
        }
    }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketRelease(PlayerBucketEmptyEvent event) {
        ClaimBuildEvent e = new ClaimBuildEvent(event.getPlayer(), event.getBlock().getLocation());
        Bukkit.getPluginManager().callEvent(e);
        e.handleCheck();
        event.setCancelled(e.isCancelled());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        ClaimBuildEvent e = new ClaimBuildEvent(event.getPlayer(), event.getBlock().getLocation());
        Bukkit.getPluginManager().callEvent(e);
        e.handleCheck();
        event.setCancelled(e.isCancelled());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                ClaimBuildEvent e = new ClaimBuildEvent(event.getPlayer(), event.getClickedBlock().getLocation());
                Bukkit.getPluginManager().callEvent(e);
                e.handleCheck();
                event.setCancelled(e.isCancelled());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {

        ClaimBuildEvent e = new ClaimBuildEvent(event.getPlayer(), event.getBlock().getLocation());
        Bukkit.getPluginManager().callEvent(e);
        e.handleCheck();
        event.setCancelled(e.isCancelled());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        ClaimBuildEvent e = new ClaimBuildEvent(event.getPlayer(), event.getBlock().getLocation());
        Bukkit.getPluginManager().callEvent(e);
        e.handleCheck();
        event.setCancelled(e.isCancelled());
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player target = (Player)event.getEntity();
            Player p = (Player)event.getDamager();
            PlayerPunchPlayerEvent e = new PlayerPunchPlayerEvent(p, target);
            Bukkit.getPluginManager().callEvent(e);
            e.perform();
            event.setCancelled(e.canHurt());
        }

        if (event.getEntity() instanceof Player && event.getDamager() instanceof Projectile && (
                (Projectile)event.getDamager()).getShooter() instanceof Player) {
            Projectile pr = (Projectile)event.getDamager();
            Player p = (Player)pr.getShooter();
            Player target = (Player)event.getEntity();
            PlayerShootPlayerEvent e = new PlayerShootPlayerEvent(p, target);
            Bukkit.getPluginManager().callEvent(e);
            e.perform();
            event.setCancelled(e.canHurt());
        }

    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() instanceof Player) {
            Player p = event.getEntity().getKiller();
            Player target = event.getEntity();
                PlayerKillPlayerEvent e = new PlayerKillPlayerEvent(p, target);
                Bukkit.getPluginManager().callEvent(e);
                e.perform();
        }
    }


}
