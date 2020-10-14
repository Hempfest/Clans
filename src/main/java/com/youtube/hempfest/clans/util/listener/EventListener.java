package com.youtube.hempfest.clans.util.listener;

import com.youtube.hempfest.clans.HempfestClans;
import com.youtube.hempfest.clans.util.StringLibrary;
import com.youtube.hempfest.clans.util.construct.ClanUtil;
import com.youtube.hempfest.clans.util.data.Config;
import com.youtube.hempfest.clans.util.data.ConfigType;
import com.youtube.hempfest.clans.util.data.DataManager;
import com.youtube.hempfest.clans.util.events.ClaimBuildEvent;
import com.youtube.hempfest.clans.util.timers.AsyncClaimResident;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.*;

public class EventListener implements Listener {

    private String c(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        ClanUtil.chatMode.put(p, "GLOBAL");
        AsyncClaimResident asyncClaimResident = new AsyncClaimResident(p);
        asyncClaimResident.runTaskTimerAsynchronously(HempfestClans.getInstance(), 2L, 20L);
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

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() instanceof DoubleChest
        || event.getClickedBlock() instanceof Chest) {
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





}
