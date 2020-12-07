package com.youtube.hempfest.clans.util.data;


import com.youtube.hempfest.clans.HempfestClans;
import com.youtube.hempfest.clans.util.construct.Claim;
import com.youtube.hempfest.clans.util.events.AllyChatEvent;
import com.youtube.hempfest.clans.util.events.ClanChatEvent;
import com.youtube.hempfest.clans.util.timers.AsyncClaimResident;
import com.youtube.hempfest.clans.util.timers.AsyncClanStatus;
import java.io.File;
import java.io.InputStream;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class DataManager {
    String name;
    String directory;
    public DataManager() {}
    public DataManager(String name) {
        this.name = name;
    }
    public DataManager(String name, String directory) {
        this.name = name;
        this.directory = directory;
    }

    public Config getFile(ConfigType type) {
        Config result = null;
        switch (type) {
            case USER_FILE:
                result = new Config(name, "Users");
                break;
            case CLAN_FILE:
                result = new Config(name, "Clans");
                break;
            case MISC_FILE:
                result = new Config(name, directory);
                break;
        }
        return result;
    }

    private static final Config main = new Config("Config", "Configuration");

    public static boolean titlesAllowed() {
       return main.getConfig().getBoolean("Clans.land-claiming.send-titles");
    }

    public void runCleaner() {
        HempfestClans.getInstance().getLogger().info("- Running data cache cleaner.");
        AsyncClanStatus asyncClanStatus = new AsyncClanStatus();
        asyncClanStatus.runTaskTimerAsynchronously(HempfestClans.getInstance(), 10L, 10L);
    }

    public void performResidentEvent(){
        if (!Bukkit.getPluginManager().isPluginEnabled("ClansBorders")) {
            if (Claim.claimUtil.claimingAllowed()) {
                AsyncClaimResident asyncClaimResident = new AsyncClaimResident();
                asyncClaimResident.runTaskTimerAsynchronously(HempfestClans.getInstance(), 2L, 20L);
            }
        } else {
            Bukkit.getLogger().info("[Clans] - Alternative claim notification system found. (Borders)");
        }
    }

    public void formatClanChat(Player p, Set<Player> receivers, String message) {
        ClanChatEvent e = new ClanChatEvent(p, receivers, message, true);
        Bukkit.getPluginManager().callEvent(e);
        if (!e.isCancelled()) {
            e.sendClanMessage();
        }
    }

    public void formatAllyChat(Player p, Set<Player> receivers, String message) {
        AllyChatEvent e = new AllyChatEvent(p, receivers, message, true);
        Bukkit.getPluginManager().callEvent(e);
        if (!e.isCancelled()) {
            e.sendAllyMessage();
        }
    }

    public void copyDefaults() {
        Config main = new Config("Config", "Configuration");
        if (!main.exists()) {
            InputStream mainGrab = HempfestClans.getInstance().getResource("Config.yml");
            Config.copy(mainGrab, main.getFile());
        }
    }

    public File getClanFolder() {
        final File dir = new File(Config.class.getProtectionDomain().getCodeSource().getLocation().getPath().replaceAll("%20", " "));
        File d = new File(dir.getParentFile().getPath(), HempfestClans.getInstance().getDescription().getName() + "/" + "Clans" + "/");
        if(!d.exists()) {
            d.mkdirs();
        }
        return d;
    }

    public File getUserFolder() {
        final File dir = new File(Config.class.getProtectionDomain().getCodeSource().getLocation().getPath().replaceAll("%20", " "));
        File d = new File(dir.getParentFile().getPath(), HempfestClans.getInstance().getDescription().getName() + "/" + "Users" + "/");
        if(!d.exists()) {
            d.mkdirs();
        }
        return d;
    }

}
