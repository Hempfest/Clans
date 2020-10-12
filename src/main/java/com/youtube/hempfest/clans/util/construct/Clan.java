package com.youtube.hempfest.clans.util.construct;

import com.youtube.hempfest.clans.util.data.Config;
import com.youtube.hempfest.clans.util.data.ConfigType;
import com.youtube.hempfest.clans.util.data.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Clan {

    String clanID;

    Player p;

    public Clan(String clanID, Player p) {
        this.clanID = clanID;
        this.p = p;
    }

    public void updateBase(Location loc) {
        DataManager dm = new DataManager(clanID, null);
        Config clan = dm.getFile(ConfigType.CLAN_FILE);
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
        DataManager dm = new DataManager(clanID, null);
        Config clan = dm.getFile(ConfigType.CLAN_FILE);
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
        ClanUtil clanUtil = new ClanUtil();
        DataManager dm = new DataManager(clanID, null);
        Config clan = dm.getFile(ConfigType.CLAN_FILE);
        List<String> array = new ArrayList<>();
        String password = clan.getConfig().getString("password");
        String owner = clan.getConfig().getString("owner");
        List<String> members = clan.getConfig().getStringList("members");
        List<String> mods = clan.getConfig().getStringList("moderators");
        List<String> admins = clan.getConfig().getStringList("admins");
        List<String> allies = clan.getConfig().getStringList("allies");
        List<String> allyRequests = clan.getConfig().getStringList("ally-requests");
        List<String> enemies = clan.getConfig().getStringList("enemies");
        String status = "LOCKED";
        if (password == null)
            status = "OPEN";
        array.add(" ");
        array.add("&2&lClan&7: &f" + clanUtil.getClanTag(clanID));
        array.add("&f&m---------------------------");
        array.add("&2" + clanUtil.getRankTag("Owner") + ": &f" + owner);
        array.add("&2Status: &f" + status);
        array.add("&2&lPower [&e" + getPower() + "&2&l]");
        if (getBase() != null)
            array.add("&2Base: &aSet");
        if (getBase() == null)
            array.add("&2Base: &7Not set");
        array.add("&2" + clanUtil.getRankTag("Admin") + "s [&b" + admins.size() + "&2]");
        array.add("&2" + clanUtil.getRankTag("Moderator") + "s [&e" + mods.size() + "&2]");
        array.add("&2Claims [&e" + getOwnedClaims().length + "&2]");
        array.add("&f&m---------------------------");
        if (allies.isEmpty())
            array.add("&2Allies [&b" + allies.size() + "&2]");
        if (allies.size() > 0) {
            array.add("&2Allies [&b" + allies.size() + "&2]");
            for (String clanId : allies) {
                array.add("&f- &e&o" + clanUtil.getClanTag(clanId));
            }
        }
        for (String clanId : clanUtil.getAllClanIDs()) {
            if (clanUtil.getEnemies(clanId).contains(clanUtil.getClan(p))) {
                enemies.add(clanId);
            }
        }
        if (enemies.isEmpty())
            array.add("&2Enemies [&b" + enemies.size() + "&2]");
        if (enemies.size() > 0) {
            array.add("&2Enemies [&b" + enemies.size() + "&2]");
            for (String clanId : enemies) {
                array.add("&f- &c&o" + clanUtil.getClanTag(clanId));
            }
        }
        array.add("&f&m---------------------------");
        array.add("&n" + clanUtil.getRankTag("Member") + "s&r [&7" + members.size() + "&r] - " + members.toString());
        array.add(" ");
        return array.toArray(new String[0]);
    }

    public void changePassword(String newPassword) {
        DataManager dm = new DataManager(clanID, null);
        Config c = dm.getFile(ConfigType.CLAN_FILE);
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
        DataManager dm = new DataManager(clanID, null);
        Config c = dm.getFile(ConfigType.CLAN_FILE);
        c.getConfig().set("name", newTag);
        c.saveConfig();
        messageClan("&3&o&nThe clan name has been changed.");
    }

    public String getClanTag() {
        DataManager dm = new DataManager(clanID, null);
        Config c = dm.getFile(ConfigType.CLAN_FILE);
        return c.getConfig().getString("name");
    }

    public String[] getMembers() {
        List<String> array = new ArrayList<>();
        DataManager dm = new DataManager(clanID, null);
        Config c = dm.getFile(ConfigType.CLAN_FILE);
        array.addAll(c.getConfig().getStringList("members"));
        return array.toArray(new String[0]);
    }

    public void messageClan(String message) {
        ClanUtil clanUtil = new ClanUtil();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (clanUtil.getClan(p) != null && clanUtil.getClan(p).equals(clanID)) {
                p.sendMessage(clanUtil.color("&7[&6&l" + clanUtil.getClanTag(clanID) + "&7] " + message));
            }
        }
    }

    public double format(double amount) {
        String number = String.valueOf(amount);
        Double numParsed = Double.valueOf(Double.parseDouble(number));
        String numString = String.format("%,.2f", new Object[] { numParsed });
        return Double.parseDouble(numString);
    }

    public double getPower() {
        double result = 0.0;
        double multiplier = 1.4;
        double add = getMembers().length + 0.56;
        int claimAmount = getOwnedClaims().length;
        result = add + (claimAmount * multiplier);
        return format(result);
    }

    public String[] getOwnedClaims() {
        ClanClaim clanClaim = new ClanClaim();
        DataManager dm = new DataManager("Regions", "Configuration");
        Config regions = dm.getFile(ConfigType.MISC_FILE);
        List<String> array = new ArrayList<>();
        for (String clan : clanClaim.getAllClaims()) {
            for (String claim : regions.getConfig().getConfigurationSection(clan + ".Claims").getKeys(false)) {
                if (clan.equals(clanID))
                    array.add(claim);
            }
        }
        return array.toArray(new String[0]);
    }



}
