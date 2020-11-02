package com.youtube.hempfest.clans.util.construct;

import com.youtube.hempfest.clans.HempfestClans;
import com.youtube.hempfest.clans.util.data.Config;
import com.youtube.hempfest.clans.util.data.ConfigType;
import com.youtube.hempfest.clans.util.data.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

public class Clan {

    String clanID;

    Player p;

    private DataManager dm() {
        return new DataManager(clanID, null);
    }
    
    private ClanUtil getUtil() {
        return HempfestClans.getInstance().clanUtil;
    }

    public Clan(String clanID, Player p) {
        this.clanID = clanID;
        this.p = p;
    }

    public void updateBase(Location loc) {

        Config clan = dm().getFile(ConfigType.CLAN_FILE);
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        float yaw = loc.getYaw();
        float pitch = loc.getPitch();
        World w = loc.getWorld();
        clan.getConfig().set("base.x", x);
        clan.getConfig().set("base.y", y);
        clan.getConfig().set("base.z", z);
        List<Float> exact = new ArrayList<>();
        exact.add(yaw);
        exact.add(pitch);
        clan.getConfig().set("base.float", exact);
        clan.getConfig().set("base.world", w.getName());
        clan.saveConfig();
        messageClan("&6The clan base was updated.");
    }

    public Location getBase() {
        Config clan = dm().getFile(ConfigType.CLAN_FILE);
        try {
            double x = clan.getConfig().getDouble("base.x");
            double y = clan.getConfig().getDouble("base.y");
            double z = clan.getConfig().getDouble("base.z");
            float yaw = clan.getConfig().getFloatList("base.float").get(0);
            float pitch = clan.getConfig().getFloatList("base.float").get(1);
            World w = Bukkit.getWorld(clan.getConfig().getString("base.world"));
            Location toGet = new Location(w, x, y, z, yaw, pitch);
            return toGet;
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public String[] getClanInfo() {
        
        Config clan = dm().getFile(ConfigType.CLAN_FILE);
        List<String> array = new ArrayList<>();
        String password = clan.getConfig().getString("password");
        String owner = clan.getConfig().getString("owner");
        List<String> members = clan.getConfig().getStringList("members");
        List<String> mods = clan.getConfig().getStringList("moderators");
        List<String> admins = clan.getConfig().getStringList("admins");
        List<String> allies = clan.getConfig().getStringList("allies");
        List<String> enemies = clan.getConfig().getStringList("enemies");
        String status = "LOCKED";
        if (password == null)
            status = "OPEN";
        array.add(" ");
        array.add("&2&lClan&7: &f" + getUtil().getColor(getChatColor()) + getUtil().getClanTag(clanID));
        array.add("&f&m---------------------------");
        array.add("&2" + getUtil().getRankTag("Owner") + ": &f" + owner);
        array.add("&2Status: &f" + status);
        array.add("&2&lPower [&e" + format(String.valueOf(getPower())) + "&2&l]");
        if (getBase() != null)
            array.add("&2Base: &aSet");
        if (getBase() == null)
            array.add("&2Base: &7Not set");
        array.add("&2" + getUtil().getRankTag("Admin") + "s [&b" + admins.size() + "&2]");
        array.add("&2" + getUtil().getRankTag("Moderator") + "s [&e" + mods.size() + "&2]");
        array.add("&2Claims [&e" + getOwnedClaims().length + "&2]");
        array.add("&f&m---------------------------");
        if (allies.isEmpty())
            array.add("&2Allies [&b" + "0" + "&2]");
        if (allies.size() > 0) {
            array.add("&2Allies [&b" + allies.size() + "&2]");
            for (String clanId : allies) {
                array.add("&f- &e&o" + getUtil().getClanTag(clanId));
            }
        }
        for (String clanId : getUtil().getAllClanIDs()) {
            if (getUtil().getEnemies(clanId).contains(getUtil().getClan(p))) {
                enemies.add(clanId);
            }
        }
        if (enemies.isEmpty())
            array.add("&2Enemies [&b" + "0" + "&2]");
        if (enemies.size() > 0) {
            array.add("&2Enemies [&b" + enemies.size() + "&2]");
            for (String clanId : enemies) {
                array.add("&f- &c&o" + getUtil().getClanTag(clanId));
            }
        }
        array.add("&f&m---------------------------");
        array.add("&n" + getUtil().getRankTag("Member") + "s&r [&7" + members.size() + "&r] - " + members.toString());
        array.add(" ");
        return array.toArray(new String[0]);
    }

    public void changePassword(String newPassword) {
        Config c = dm().getFile(ConfigType.CLAN_FILE);
        if (newPassword.equals("empty")) {
            c.getConfig().set("password", null);
            c.saveConfig();
            messageClan("&b&o&nThe clan status was set to&r &a&oOPEN.");
            return;
        }
        c.getConfig().set("password", newPassword);
        c.saveConfig();
        messageClan("&b&o&nThe clan password has been changed.");
    }

    public void changeTag(String newTag) {
        if (newTag.length() > HempfestClans.getMain().getConfig().getInt("Clans.tag-size")) {
            getUtil().sendMessage(p, "&c&oThe clan name you have chosen is too long! Max tag length reached.");
            return;
        }
        Config c = dm().getFile(ConfigType.CLAN_FILE);
        c.getConfig().set("name", newTag);
        c.saveConfig();
        messageClan("&3&o&nThe clan name has been changed.");
    }

    public void changeColor(String newColor) {
        Config c = dm().getFile(ConfigType.CLAN_FILE);
        c.getConfig().set("name-color", newColor);
        c.saveConfig();
        messageClan(getUtil().getColor(newColor) + "The clan name color has been changed.");
    }

    public String getClanTag() {
        Config c = dm().getFile(ConfigType.CLAN_FILE);
        return c.getConfig().getString("name");
    }

    public String getChatColor() {
        Config c = dm().getFile(ConfigType.CLAN_FILE);
        if (c.getConfig().getString("name-color") == null) {
            return "WHITE";
        }
        return c.getConfig().getString("name-color");
    }

    public String[] getMembers() {
        Config c = dm().getFile(ConfigType.CLAN_FILE);
        List<String> array = new ArrayList<>(c.getConfig().getStringList("members"));
        return array.toArray(new String[0]);
    }

    public void messageClan(String message) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (getUtil().getClan(p) != null && getUtil().getClan(p).equals(clanID)) {
                p.sendMessage(getUtil().color("&7[&6&l" + getUtil().getClanTag(clanID) + "&7] " + message));
            }
        }
    }

    public double format(String amount) {
        // Assigning value to BigDecimal object b1
        BigDecimal b1 = new BigDecimal(amount);

        MathContext m = new MathContext(3); // 4 precision

        // b1 is rounded using m
        BigDecimal b2 = b1.round(m);
        return b2.doubleValue();
    }

    public void givePower(double amount) {
        Config c = dm().getFile(ConfigType.CLAN_FILE);
        if (!c.getConfig().isDouble("bonus")) {
            c.getConfig().set("bonus", 0.0);
            c.saveConfig();
        }
        double current = c.getConfig().getDouble("bonus");
        c.getConfig().set("bonus", (current + amount));
        c.saveConfig();
        messageClan("&a&oNew power was gained. The clan grows stronger..");
        System.out.println(String.format("[%s] - Gave " + '"' + amount + '"' + " power to clan " + '"' + clanID + '"', HempfestClans.getInstance().getDescription().getName()));
    }

    public void takePower(double amount) {
        Config c = dm().getFile(ConfigType.CLAN_FILE);
        if (!c.getConfig().isDouble("bonus")) {
            c.getConfig().set("bonus", 0.0);
            c.saveConfig();
        }
        double current = c.getConfig().getDouble("bonus");
        c.getConfig().set("bonus", (current - amount));
        c.saveConfig();
        messageClan("&c&oPower was stolen from us.. we need to earn it back");
        System.out.println(String.format("[%s] - Took " + '"' + amount + '"' + " power from clan " + '"' + clanID + '"', HempfestClans.getInstance().getDescription().getName()));
    }

    public double getPower() {
        Config c = dm().getFile(ConfigType.CLAN_FILE);
        double result = 0.0;
        double multiplier = 1.4;
         double add = getMembers().length + 0.56;
        int claimAmount = getOwnedClaims().length;
        result = result + add + (claimAmount * multiplier);
        double bonus = c.getConfig().getDouble("bonus");
        return result + bonus;
    }

    public String[] getOwnedClaims() {
        ClaimUtil claimUtil = new ClaimUtil();
        DataManager dm = new DataManager("Regions", "Configuration");
        Config regions = dm.getFile(ConfigType.MISC_FILE);
        List<String> array = new ArrayList<>();
        for (String clan : claimUtil.getAllClaims()) {
            for (String claim : regions.getConfig().getConfigurationSection(clan + ".Claims").getKeys(false)) {
                if (clan.equals(clanID))
                    array.add(claim);
            }
        }
        return array.toArray(new String[0]);
    }



}
