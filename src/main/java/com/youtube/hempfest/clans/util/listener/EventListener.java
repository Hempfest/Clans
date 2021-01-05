package com.youtube.hempfest.clans.util.listener;

import com.youtube.hempfest.clans.HempfestClans;
import com.youtube.hempfest.clans.Update;
import com.youtube.hempfest.clans.util.Member;
import com.youtube.hempfest.clans.util.StringLibrary;
import com.youtube.hempfest.clans.util.construct.Claim;
import com.youtube.hempfest.clans.util.construct.Clan;
import com.youtube.hempfest.clans.util.construct.ClanUtil;
import com.youtube.hempfest.clans.util.construct.Resident;
import com.youtube.hempfest.clans.util.data.Config;
import com.youtube.hempfest.clans.util.data.ConfigType;
import com.youtube.hempfest.clans.util.data.DataManager;
import com.youtube.hempfest.clans.util.events.ClaimInteractEvent;
import com.youtube.hempfest.clans.util.events.ClanCreateEvent;
import com.youtube.hempfest.clans.util.events.CustomChatEvent;
import com.youtube.hempfest.clans.util.events.PlayerKillPlayerEvent;
import com.youtube.hempfest.clans.util.events.PlayerPunchPlayerEvent;
import com.youtube.hempfest.clans.util.events.PlayerShootPlayerEvent;
import com.youtube.hempfest.hempcore.data.VaultHook;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
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
            DataManager dm = new DataManager(Clan.clanUtil.getClan(p));
            Config clan = dm.getFile(ConfigType.CLAN_FILE);
            if (!clan.exists()) {
                playerData(p).getConfig().set("Clan", null);
                playerData(p).saveConfig();
                p.sendMessage(Clan.clanUtil.color(Clan.clanUtil.getPrefix() + " Your clan was disbanded due to owner dismissal.."));
                return;
            }
            HempfestClans.getInstance().playerClan.put(p.getUniqueId(), playerData(p).getConfig().getString("Clan"));
            HempfestClans.clanEnemies.put(Clan.clanUtil.getClan(p), new ArrayList<>(clan.getConfig().getStringList("enemies")));
            HempfestClans.clanAllies.put(Clan.clanUtil.getClan(p), new ArrayList<>(clan.getConfig().getStringList("allies")));
            if (HempfestClans.getInstance().dataManager.prefixedTagsAllowed()) {
                Clan c = HempfestClans.clanManager(p);
                Member.setPrefix(p, "&7[" + Clan.clanUtil.getColor(c.getChatColor()) + c.getClanTag() + "&7] ");
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

    private String chatMode(Player p) {
        return HempfestClans.chatMode.get(p);
    }

    @EventHandler
    public void onClanBuy(ClanCreateEvent event) {
        Player p = event.getMaker();
        if (HempfestClans.getMain().getConfig().getBoolean("Clans.creation.charge")) {
            double amount = HempfestClans.getMain().getConfig().getDouble("Clans.creation.amount");
            EconomyResponse takeMoney = VaultHook.getEconomy().withdrawPlayer(p, amount);
            if (!takeMoney.transactionSuccess()) {
                event.setCancelled(true);
                event.stringLibrary().sendMessage(p, "&c&oYou don't have enough money. Amount needed: &6" + amount);
            }
        }
    }

    private static final DataManager dm = new DataManager();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrefixApply(AsyncPlayerChatEvent event) {
        Player p = event.getPlayer();
        ClanUtil clanUtil = Clan.clanUtil;

        if (chatMode(p).equalsIgnoreCase("GLOBAL")) {
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
        if (chatMode(p).equalsIgnoreCase("CLAN")) {
            if (!event.isCancelled()) {
                dm.formatClanChat(p, event.getRecipients(), event.getMessage());
                event.setCancelled(true);
                return;
            }
        }
        if (chatMode(p).equalsIgnoreCase("ALLY")) {
            if (!event.isCancelled()) {
                dm.formatAllyChat(p, event.getRecipients(), event.getMessage());
                event.setCancelled(true);
                return;
            }
        }

        List<String> defaults = new ArrayList<>(Arrays.asList("GLOBAL", "CLAN", "ALLY"));
        if (!defaults.contains(chatMode(p))) {
            // new event
            CustomChatEvent e = new CustomChatEvent(p, event.getRecipients(), event.getMessage(), true);
            Bukkit.getPluginManager().callEvent(e);
            if (!e.isCancelled()) {
                e.sendMessage();
            }
            event.setCancelled(true);
        }
    }


    @EventHandler(priority = EventPriority.NORMAL)
    public void onProjectileHit(ProjectileHitEvent event) {
    if (event.getEntity().getShooter() instanceof Player) {
        Player p = (Player) event.getEntity().getShooter();
        ClaimInteractEvent e = new ClaimInteractEvent(p, event.getEntity().getLocation());
        Bukkit.getPluginManager().callEvent(e);
        e.handleCheck();
        if (e.isCancelled()) {
            event.getEntity().remove();
        }
    }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onTNTExplode(EntityExplodeEvent e) {
        if (e.getEntity() instanceof Creeper) {
            for (Block exploded : e.blockList()) {
                if (Claim.claimUtil.isInClaim(exploded.getLocation())) {
                    e.setCancelled(true);
                    break;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        Block piston = e.getBlock();
        if (!Claim.claimUtil.isInClaim(piston.getLocation())) {
            for (Block pushed : e.getBlocks()) {
                if (Claim.claimUtil.isInClaim(pushed.getLocation())) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        Block piston = e.getBlock();
        if (!Claim.claimUtil.isInClaim(piston.getLocation())) {
            for (Block pushed : e.getBlocks()) {
                if (Claim.claimUtil.isInClaim(pushed.getLocation())) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBucketRelease(PlayerBucketEmptyEvent event) {
        ClaimInteractEvent e = new ClaimInteractEvent(event.getPlayer(), event.getBlock().getLocation());
        Bukkit.getPluginManager().callEvent(e);
        e.handleCheck();
        event.setCancelled(e.isCancelled());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBucketFill(PlayerBucketFillEvent event) {
        ClaimInteractEvent e = new ClaimInteractEvent(event.getPlayer(), event.getBlock().getLocation());
        Bukkit.getPluginManager().callEvent(e);
        e.handleCheck();
        event.setCancelled(e.isCancelled());
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

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {

        ClaimInteractEvent e = new ClaimInteractEvent(event.getPlayer(), event.getBlock().getLocation());
        Bukkit.getPluginManager().callEvent(e);
        e.handleCheck();
        if (e.isCancelled()) {
            event.setCancelled(e.isCancelled());
        } else {
            if (Claim.claimUtil.isInClaim(event.getBlock().getLocation())) {
                if (Claim.claimUtil.isInClaim(event.getPlayer().getLocation())) {
                    Resident r = Claim.getResident(event.getPlayer());
                    r.addBroken(event.getBlock());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPlace(BlockPlaceEvent event) {
        ClaimInteractEvent e = new ClaimInteractEvent(event.getPlayer(), event.getBlock().getLocation());
        Bukkit.getPluginManager().callEvent(e);
        e.handleCheck();
        if (e.isCancelled()) {
            event.setCancelled(e.isCancelled());
        } else {
            if (Claim.claimUtil.isInClaim(event.getPlayer().getLocation())) {
                Resident r = Claim.getResident(event.getPlayer());
                r.addPlaced(event.getBlock());
            }
        }
    }


    @EventHandler(priority = EventPriority.NORMAL)
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


}
