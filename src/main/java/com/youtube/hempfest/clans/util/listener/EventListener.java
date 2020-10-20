package com.youtube.hempfest.clans.util.listener;

import com.youtube.hempfest.clans.util.StringLibrary;
import com.youtube.hempfest.clans.util.construct.ClanUtil;
import com.youtube.hempfest.clans.util.data.Config;
import com.youtube.hempfest.clans.util.data.ConfigType;
import com.youtube.hempfest.clans.util.data.DataManager;
import com.youtube.hempfest.clans.util.events.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import org.bukkit.event.player.*;

public class EventListener implements Listener {

    private String c(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        ClanUtil.updateUsername(p);
        ClanUtil.chatMode.put(p, "GLOBAL");
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        ClaimResidentEvent.claimID.remove(p.getName());
        ClaimResidentEvent.invisibleResident.remove(p.getUniqueId());
        ClaimResidentEvent.residents.remove(p.getName());
        ClaimResidentEvent.tempStorage.remove(p.getUniqueId());
    }

    private String chatMode(Player p) {
        return ClanUtil.chatMode.get(p);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPrefixApply(AsyncPlayerChatEvent event) {
        Player p = event.getPlayer();
        ClanUtil clanUtil = new ClanUtil();
        DataManager dm = new DataManager("Config", "Configuration");
        if (chatMode(p).equals("GLOBAL")) {
            Config main = dm.getFile(ConfigType.MISC_FILE);
            if (main.getConfig().getBoolean("Formatting.allow")) {
                if (clanUtil.getClan(p) != null) {
                    String clanName = clanUtil.getClanTag(clanUtil.getClan(p));
                    StringLibrary lib = new StringLibrary();
                    String rank = "";
                    switch (lib.getRankStyle()) {
                        case "WORDLESS":
                            rank = lib.getWordlessStyle(clanUtil.getRank(p));
                            event.setFormat(c(String.format(lib.getChatFormat(), rank, clanName)) + " " + event.getFormat());
                            break;
                        case "FULL":
                            rank = lib.getFullStyle(clanUtil.getRank(p));
                            event.setFormat(c(String.format(lib.getChatFormat(), rank, clanName)) + " " + event.getFormat());
                            break;
                    }
                }
            }
            return;
        }
        if (chatMode(p).equals("CLAN")) {
            dm.formatClanChat(p, event.getRecipients(), event.getMessage());
            event.setCancelled(true);
            return;
        }
        if (chatMode(p).equals("ALLY")) {
            dm.formatAllyChat(p, event.getRecipients(), event.getMessage());
            event.setCancelled(true);
            return;
        }
    }


    @EventHandler
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

    @EventHandler
    public void onBucketRelease(PlayerBucketEmptyEvent event) {
        ClaimBuildEvent e = new ClaimBuildEvent(event.getPlayer(), event.getBlock().getLocation());
        Bukkit.getPluginManager().callEvent(e);
        e.handleCheck();
        event.setCancelled(e.isCancelled());
    }

    @EventHandler
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

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        ClaimBuildEvent e = new ClaimBuildEvent(event.getPlayer(), event.getBlock().getLocation());
        Bukkit.getPluginManager().callEvent(e);
        e.handleCheck();
        event.setCancelled(e.isCancelled());
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ClaimBuildEvent e = new ClaimBuildEvent(event.getPlayer(), event.getBlock().getLocation());
        Bukkit.getPluginManager().callEvent(e);
        e.handleCheck();
        event.setCancelled(e.isCancelled());
    }


    @EventHandler
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
