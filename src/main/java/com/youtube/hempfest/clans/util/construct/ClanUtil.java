package com.youtube.hempfest.clans.util.construct;

import com.youtube.hempfest.clans.HempfestClans;
import com.youtube.hempfest.clans.util.Color;
import com.youtube.hempfest.clans.util.HighestValue;
import com.youtube.hempfest.clans.util.RankPriority;
import com.youtube.hempfest.clans.util.StringLibrary;
import com.youtube.hempfest.clans.util.data.Config;
import com.youtube.hempfest.clans.util.data.ConfigType;
import com.youtube.hempfest.clans.util.data.DataManager;
import com.youtube.hempfest.clans.util.versions.Component;
import com.youtube.hempfest.clans.util.versions.ComponentR1_8_1;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class ClanUtil extends StringLibrary {

    public boolean raidShield;
    public static HashMap<Player, String> chatMode = new HashMap<>();

    public void create(Player p, String clanName, String password) {
    DataManager dm = new DataManager(p.getUniqueId().toString(), null);
    Config user = dm.getFile(ConfigType.USER_FILE);
    if (getClan(p) == null) {
        if (clanName.length() > HempfestClans.getMain().getConfig().getInt("Clans.tag-size")) {
            sendMessage(p, "&c&oThe clan name you have chosen is too long! Max tag length reached.");
            return;
        }
        FileConfiguration local = user.getConfig();
        String newID = clanCode();
        local.set("Clan", newID);
        HempfestClans.playerClan.put(p.getUniqueId(), newID);
        user.saveConfig();
        if (password == null) {
            createClanFile(newID, clanName);
            sendMessage(p, "Clan " + '"' + clanName + '"' + " created with no password.");
            Bukkit.broadcastMessage(color(getPrefix() + " &a&o" + p.getName() + String.format(" created an &fOPEN &a&oclan &7%s.", clanName)));
        } else {
            createClanFile(newID, clanName, password);
            sendMessage(p, "Clan " + '"' + clanName + '"' + " created with password: &e&o" + password);
            Bukkit.broadcastMessage(color(getPrefix() + " &a&o" + p.getName() + String.format(" created a &fLOCKED &a&oclan &7%s.", clanName)));
        }
        DataManager data = new DataManager(newID, null);
        Config clanFile = data.getFile(ConfigType.CLAN_FILE);
        FileConfiguration fc = clanFile.getConfig();
        List<String> members = fc.getStringList("members");
        members.add(p.getName());
        fc.set("members", members);
        fc.set("owner", p.getName());
        clanFile.saveConfig();
    } else {
        sendMessage(p, alreadyInClan());
    }
    }

    public void leave(Player p) {
    if (getClan(p) != null) {
        Clan clanIndex = new Clan(getClan(p), p);
        DataManager dm = new DataManager(getClan(p), null);
        Config clan = dm.getFile(ConfigType.CLAN_FILE);
        DataManager data = new DataManager(p.getUniqueId().toString(), null);
        Config user = data.getFile(ConfigType.USER_FILE);
        switch (getRank(p)) {
            case "Owner":
                DataManager dataManager = new DataManager("Regions", "Configuration");
                Config regions = dataManager.getFile(ConfigType.MISC_FILE);
                regions.getConfig().set(getClan(p), null);
                regions.saveConfig();
                String clanName = clan.getConfig().getString("name");
                clan.delete();
                user.getConfig().set("Clan", null);
                user.saveConfig();
                Bukkit.broadcastMessage(color(getPrefix() + " &c&oClan " + '"' + clanName + '"' + " has fallen.."));
                HempfestClans.playerClan.remove(p.getUniqueId());
                break;
            case "Admin":
                clanIndex.messageClan("&e&oPlayer " + '"' + p.getName() + '"' + " left the clan..");
                List<String> admins = clan.getConfig().getStringList("admins");
                admins.remove(p.getName());
                clan.getConfig().set("admins", admins);
                user.getConfig().set("Clan", null);
                clan.saveConfig();
                user.saveConfig();
                HempfestClans.playerClan.remove(p.getUniqueId());
                break;
            case "Moderator":
                clanIndex.messageClan("&e&oPlayer " + '"' + p.getName() + '"' + " left the clan..");
                List<String> moderators = clan.getConfig().getStringList("moderators");
                moderators.remove(p.getName());
                clan.getConfig().set("moderators", moderators);
                user.getConfig().set("Clan", null);
                clan.saveConfig();
                user.saveConfig();
                HempfestClans.playerClan.remove(p.getUniqueId());
                break;
            case "Member":
                clanIndex.messageClan("&e&oPlayer " + '"' + p.getName() + '"' + " left the clan..");
                List<String> members = clan.getConfig().getStringList("members");
                members.remove(p.getName());
                clan.getConfig().set("members", members);
                user.getConfig().set("Clan", null);
                clan.saveConfig();
                user.saveConfig();
                HempfestClans.playerClan.remove(p.getUniqueId());
                break;
        }
    } else {
        sendMessage(p, notInClan());
    }
    }

    public void joinClan(Player p, String clanName, String password) {
        if (getClan(p) == null) {
                if (!getAllClanNames().contains(clanName)) {
                    sendMessage(p, "&c&oThis clan does not exist!");
                    return;
                }
                if (getClanPassword(getClanID(clanName)) == null) {
                    DataManager data = new DataManager(p.getUniqueId().toString(), null);
                    Config user = data.getFile(ConfigType.USER_FILE);
                    user.getConfig().set("Clan", getClanID(clanName));
                    user.saveConfig();
                    HempfestClans.playerClan.put(p.getUniqueId(), getClanID(clanName));
                    DataManager dm = new DataManager(getClan(p), null);
                    Config clan = dm.getFile(ConfigType.CLAN_FILE);
                    FileConfiguration fc = clan.getConfig();
                    List<String> members = fc.getStringList("members");
                    members.add(p.getName());
                    fc.set("members", members);
                    clan.saveConfig();
                    Clan clanIndex = new Clan(getClan(p), p);
                    clanIndex.messageClan("&a&oPlayer " + '"' + p.getName() + '"' + " joined the clan.");
                    return;
                }
                if (getClanPassword(getClanID(clanName)) != null && password.equals("none")) {
                    sendMessage(p, "&c&oThis clan requires a password to join.");
                    return;
                }
                if (getClanPassword(getClanID(clanName)).equals(password)) {
                    DataManager data = new DataManager(p.getUniqueId().toString(), null);
                    Config user = data.getFile(ConfigType.USER_FILE);
                    user.getConfig().set("Clan", getClanID(clanName));
                    user.saveConfig();
                    HempfestClans.playerClan.put(p.getUniqueId(), getClanID(clanName));
                    DataManager dm = new DataManager(getClan(p), null);
                    Config clan = dm.getFile(ConfigType.CLAN_FILE);
                    FileConfiguration fc = clan.getConfig();
                    List<String> members = fc.getStringList("members");
                    members.add(p.getName());
                    fc.set("members", members);
                    clan.saveConfig();
                    Clan clanIndex = new Clan(getClan(p), p);
                    clanIndex.messageClan("&a&oPlayer " + '"' + p.getName() + '"' + " joined the clan.");
                } else {
                    sendMessage(p, wrongPassword());
                }
        } else
            sendMessage(p, alreadyInClan());
    }

    public List<String> getAllyRequests(String clanID) {
        DataManager dm = new DataManager(clanID, null);
        Config clan = dm.getFile(ConfigType.CLAN_FILE);
        return new ArrayList<>(clan.getConfig().getStringList("ally-requests"));
    }

    public void sendAllyRequest(Player p, String clanID, String targetClanID) {
        DataManager dm = new DataManager(targetClanID, null);
        Config clan = dm.getFile(ConfigType.CLAN_FILE);
        if (getAllyRequests(clanID).contains(targetClanID)) {
            addAlly(clanID, targetClanID);
            return;
        }
        if (getAllyRequests(targetClanID).contains(clanID)) {
            sendMessage(p, "&e&oWaiting on a response from &f" + getClanTag(targetClanID));
            return;
        }
        List<String> allies = getAllyRequests(targetClanID);
        allies.add(clanID);
        clan.getConfig().set("ally-requests", allies);
        clan.saveConfig();
        Clan clanIndex = new Clan(targetClanID, null);
        sendMessage(p, "&a&oAlly invitation sent.");
        clanIndex.messageClan("&a&oClan " + '"' + "&e" + getClanTag(clanID) + "&a&o" + '"' + " wishes to ally, to accept\n&7Type &f/clan ally &6" + getClanTag(clanID));
    }

    public void addAlly(String clanID, String targetClanID) {
        DataManager dm = new DataManager(clanID, null);
        Config clan = dm.getFile(ConfigType.CLAN_FILE);
        DataManager dm2 = new DataManager(targetClanID, null);
        Config clan2 = dm2.getFile(ConfigType.CLAN_FILE);
        if (getAllyRequests(clanID).contains(targetClanID)) {
            List<String> allyRequests = getAllyRequests(clanID);
            allyRequests.remove(targetClanID);
            clan.getConfig().set("ally-requests", allyRequests);
            clan.saveConfig();
        }
        List<String> allies = getAllies(clanID);
        List<String> allies2 = getAllies(targetClanID);
        allies.add(targetClanID);
        allies2.add(clanID);
        clan.getConfig().set("allies", allies);
        clan.saveConfig();
        clan2.getConfig().set("allies", allies2);
        clan2.saveConfig();
        Clan clanIndex = new Clan(clanID, null);
        Clan clanIndex2 = new Clan(targetClanID, null);
        clanIndex.messageClan("&a&oNow allies with clan " + '"' + "&e" + getClanTag(targetClanID) + "&a&o" + '"');
        clanIndex2.messageClan("&a&oNow allies with clan " + '"' + "&e" + getClanTag(clanID) + "&a&o" + '"');
    }

    public void removeAlly(String clanID, String targetClanID) {
        DataManager dm = new DataManager(clanID, null);
        Config clan = dm.getFile(ConfigType.CLAN_FILE);
        List<String> allies = getAllies(clanID);
        allies.remove(targetClanID);
        clan.getConfig().set("allies", allies);
        clan.saveConfig();
    }

    public void addEnemy(String clanID, String targetClanID) {
        DataManager dm = new DataManager(clanID, null);
        Config clan = dm.getFile(ConfigType.CLAN_FILE);
        if (getAllies(clanID).contains(targetClanID)) {
            removeAlly(clanID, targetClanID);
            removeAlly(targetClanID, clanID);
        }
        List<String> enemies = getEnemies(clanID);
        enemies.add(targetClanID);
        clan.getConfig().set("enemies", enemies);
        clan.saveConfig();
        Clan clanIndex = new Clan(clanID, null);
        Clan clanIndex2 = new Clan(targetClanID, null);
        clanIndex.messageClan("&4&oNow enemies with clan " + '"' + "&e" + getClanTag(targetClanID) + "&4&o" + '"');
        clanIndex2.messageClan("&4&oNow enemies with clan " + '"' + "&e" + getClanTag(clanID) + "&4&o" + '"');
    }

    public void removeEnemy(String clanID, String targetClanID) {
        DataManager dm = new DataManager(clanID, null);
        Config clan = dm.getFile(ConfigType.CLAN_FILE);
        List<String> enemies = getEnemies(clanID);
        enemies.remove(targetClanID);
        clan.getConfig().set("enemies", enemies);
        clan.saveConfig();
        Clan clanIndex = new Clan(clanID, null);
        Clan clanIndex2 = new Clan(targetClanID, null);
        clanIndex.messageClan("&f&oNow neutral with clan " + '"' + "&e" + getClanTag(targetClanID) + "&f&o" + '"');
        clanIndex2.messageClan("&f&oNow neutral with clan " + '"' + "&e" + getClanTag(clanID) + "&f&o" + '"');
    }

    public double getKD(UUID playerID) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerID);
        int kills = player.getStatistic(Statistic.PLAYER_KILLS);
        int deaths = player.getStatistic(Statistic.DEATHS);
        double result;
        if (deaths == 0) {
            result = kills;
        } else {
            double value = (double) kills / deaths;
            result = Math.round(value);
        }
        return result;
    }

    public UUID getUserID(String playerName) {
        UUID result = null;
        for (UUID player : getAllUsers()) {
            DataManager dm = new DataManager(player.toString(), null);
            Config user = dm.getFile(ConfigType.USER_FILE);
            if (user.getConfig().getString("username") == null) {
                user.getConfig().set("username", Bukkit.getOfflinePlayer(player).getName());
                user.saveConfig();
            }
            if (Objects.equals(user.getConfig().getString("username"), playerName)) {
                result = player;
            }
        }
        return result;
    }

    public List<UUID> getAllUsers() {
        DataManager dm = new DataManager();
        List<UUID> result = new ArrayList<>();
        for (File file : Objects.requireNonNull(dm.getUserFolder().listFiles())) {
            result.add(UUID.fromString(file.getName().replace(".yml", "")));
        }
        return result;
    }

    public static void updateUsername(Player p) {
        DataManager data = new DataManager(p.getUniqueId().toString(), null);
        Config user = data.getFile(ConfigType.USER_FILE);
        if (user.getConfig().getString("username") == null) {
            user.getConfig().set("username", p.getName());
            user.saveConfig();
        }
    }

    public String getCurrentRank(int rankPower) {
        String result = "";
        switch (rankPower) {
            case 0:
                result = "members";
                break;
            case 1:
                result = "moderators";
                break;
            case 2:
                result = "admins";
                break;
            case 3:
                result = "owner";
                break;
        }
        return result;
    }

    public String getRankUpgrade(int rankPower) {
        String result = "";
        switch (rankPower) {
            case 0:
                result = "moderators";
                break;
            case 1:
                result = "admins";
                break;
        }
        return result;
    }

    public String getRankDowngrade(int rankPower) {
        String result = "";
        switch (rankPower) {
            case 1:
                result = "members";
                break;
            case 2:
                result = "moderators";
                break;
        }
        return result;
    }

    private void createClanFile(String clanID, String name) {
        DataManager dm = new DataManager(clanID, null);
        Config clan = dm.getFile(ConfigType.CLAN_FILE);
        FileConfiguration local = clan.getConfig();
        List<String> members = new ArrayList<>();
        List<String> mods = new ArrayList<>();
        List<String> admins = new ArrayList<>();
        List<String> allies = new ArrayList<>();
        List<String> enemies = new ArrayList<>();
        local.set("name", name);
        local.set("members", members);
        local.set("moderators", mods);
        local.set("admins", admins);
        local.set("allies", allies);
        local.set("enemies", enemies);
        clan.saveConfig();
        System.out.printf("[%s] - Clan " + '"' + clanID + '"' + " created.%n", HempfestClans.getInstance().getDescription().getName());
    }

    private void createClanFile(String clanID, String name, String password) {
        DataManager dm = new DataManager(clanID, null);
        Config clan = dm.getFile(ConfigType.CLAN_FILE);
        FileConfiguration local = clan.getConfig();
        List<String> members = new ArrayList<>();
        List<String> mods = new ArrayList<>();
        List<String> admins = new ArrayList<>();
        List<String> allies = new ArrayList<>();
        List<String> enemies = new ArrayList<>();
        local.set("name", name);
        local.set("password", password);
        local.set("members", members);
        local.set("moderators", mods);
        local.set("admins", admins);
        local.set("allies", allies);
        local.set("enemies", enemies);
        clan.saveConfig();
        System.out.printf("[%s] - Clan " + '"' + clanID + '"' + " created.%n", HempfestClans.getInstance().getDescription().getName());
    }

    public void demotePlayer(Player target) {
        ClanUtil clanUtil = new ClanUtil();
        DataManager data = new DataManager(getClan(target), null);
        Config clan = data.getFile(ConfigType.CLAN_FILE);
        if (clan.getConfig().getStringList("members").contains(target.getName())) {
            if (clanUtil.getRankPower(target) != 3 || clanUtil.getRankPower(target) != 0) {
                String currentRank = clanUtil.getCurrentRank(clanUtil.getRankPower(target));
                List<String> array = clan.getConfig().getStringList(clanUtil.getRankDowngrade(clanUtil.getRankPower(target)));
                List<String> array2 = clan.getConfig().getStringList(currentRank);
                array2.remove(target.getName());
                clan.getConfig().set(currentRank, array2);
                if (!clanUtil.getRankDowngrade(clanUtil.getRankPower(target)).equals("members"))
                    array.add(target.getName());
                clan.getConfig().set(clanUtil.getRankDowngrade(clanUtil.getRankPower(target)), array);
                clan.saveConfig();
                Clan clanIndex = new Clan(getClan(target), target);
                clanIndex.messageClan("&d&oPlayer " + '"' + target.getName() + '"' + " was demoted.");
            }
        }
    }

    public void promotePlayer(Player target) {
        ClanUtil clanUtil = new ClanUtil();
        DataManager data = new DataManager(getClan(target), null);
        Config clan = data.getFile(ConfigType.CLAN_FILE);
        if (clan.getConfig().getStringList("members").contains(target.getName())) {
            if (clanUtil.getRankPower(target) < clanUtil.maxRankPower()) {
                String currentRank = clanUtil.getCurrentRank(clanUtil.getRankPower(target));
                List<String> array = clan.getConfig().getStringList(clanUtil.getRankUpgrade(clanUtil.getRankPower(target)));
                List<String> array2 = clan.getConfig().getStringList(currentRank);
                if (!currentRank.equals("members"))
                    array2.remove(target.getName());
                clan.getConfig().set(currentRank, array2);
                array.add(target.getName());
                clan.getConfig().set(clanUtil.getRankUpgrade(clanUtil.getRankPower(target)), array);
                clan.saveConfig();
                Clan clanIndex = new Clan(getClan(target), target);
                clanIndex.messageClan("&a&oPlayer " + '"' + target.getName() + '"' + " was promoted.");
            }
        }
    }

    public void kickPlayer(Player target) {
        DataManager dm = new DataManager(target.getUniqueId().toString(), null);
        Config user = dm.getFile(ConfigType.USER_FILE);
        DataManager data = new DataManager(getClan(target), null);
        Config clan = data.getFile(ConfigType.CLAN_FILE);
        FileConfiguration fc = clan.getConfig();
        List<String> members = fc.getStringList("members");
        List<String> admins = fc.getStringList("admins");
        List<String> moderators = fc.getStringList("moderators");
        if (fc.getStringList("members").contains(target.getName())) {
            members.remove(target.getName());
            fc.set("members", members);
        }
        if (fc.getStringList("moderators").contains(target.getName())) {
            moderators.remove(target.getName());
            fc.set("moderators", moderators);
        }
        if (fc.getStringList("admins").contains(target.getName())) {
            admins.remove(target.getName());
            fc.set("admins", admins);
        }
        clan.saveConfig();
        user.getConfig().set("Clan", null);
        user.saveConfig();
        HempfestClans.playerClan.remove(target.getUniqueId());
    }

    public void teleportBase(Player p) {
        Clan clan = new Clan(getClan(p), p);
        if (clan.getBase() != null) {
            p.teleport(clan.getBase());
        }
    }

    public void transferOwner(Player p, String target) {
        DataManager dm = new DataManager(getClan(p), null);
        Config clan = dm.getFile(ConfigType.CLAN_FILE);
        Clan clanIndex = new Clan(getClan(p), p);
        if (Arrays.asList(clanIndex.getMembers()).contains(target)) {
            if (getRankPower(p) == 3) {
                clan.getConfig().set("owner", target);
                clan.saveConfig();
                sendMessage(p, "&d&oOwnership transferred.. It was a nice run..");
            } else {
                sendMessage(p ,"&cYou don't have clan clearance.");
            }
        } else {
            sendMessage(p, "&c&oMember not found.");
        }
    }

    public String getClan(Player p) {
        if (!HempfestClans.playerClan.containsKey(p.getUniqueId())) {
            return null;
        }
        return HempfestClans.playerClan.get(p.getUniqueId());
    }

    public void changeNickname(Player p, String newName) {
        DataManager dm = new DataManager(p.getUniqueId().toString(), null);
        Config user = dm.getFile(ConfigType.USER_FILE);
        if (newName.equals("empty")) {
            user.getConfig().set("Nickname", p.getName());
            newName = p.getName();
        } else {
            user.getConfig().set("Nickname", newName);
        }
        user.saveConfig();
        sendMessage(p, "&3&oChat nickname updated to: &7" + newName);
    }

    public String getClanNickname(Player p) {
        DataManager dm = new DataManager(p.getUniqueId().toString(), null);
        Config user = dm.getFile(ConfigType.USER_FILE);
        return user.getConfig().getString("Nickname") != null ? user.getConfig().getString("Nickname") : p.getName();
    }

    public String getRank(Player p) {
        DataManager dm = new DataManager(getClan(p), null);
        Config clan = dm.getFile(ConfigType.CLAN_FILE);
        String rank = "";
        FileConfiguration fc = clan.getConfig();
        if (fc.getStringList("members").contains(p.getName())) {
            rank = "Member";
        }
        if (fc.getStringList("moderators").contains(p.getName())) {
            rank = "Moderator";
        }
        if (fc.getStringList("admins").contains(p.getName())) {
            rank = "Admin";
        }
        if (Objects.equals(fc.getString("owner"), p.getName())) {
            rank = "Owner";
        }
        return rank;
    }

    public String getRankTag(String rank) {
        String result = "";
        DataManager dm = new DataManager("Config", "Configuration");
        Config main = dm.getFile(ConfigType.MISC_FILE);
        String member = main.getConfig().getString("Formatting.Styles.Full.Member");
        String mod = main.getConfig().getString("Formatting.Styles.Full.Moderator");
        String admin = main.getConfig().getString("Formatting.Styles.Full.Admin");
        String owner = main.getConfig().getString("Formatting.Styles.Full.Owner");
        switch (rank) {
            case "Member":
                result = member;
                break;
            case "Moderator":
                result = mod;
                break;
            case "Admin":
                result = admin;
                break;
            case "Owner":
                result = owner;
                break;
        }
        return result;
    }

    public String getMemberRank(String clanID, String member) {
        DataManager dm = new DataManager(clanID, null);
        Config clan = dm.getFile(ConfigType.CLAN_FILE);
        String rank = "";
        FileConfiguration fc = clan.getConfig();
        if (fc.getStringList("members").contains(member)) {
            rank = "Member";
        }
        if (fc.getStringList("moderators").contains(member)) {
            rank = "Moderator";
        }
        if (fc.getStringList("admins").contains(member)) {
            rank = "Admin";
        }
        if (Objects.equals(fc.getString("owner"), member)) {
            rank = "Owner";
        }
        return rank;
    }

    public int getRankPower(Player p) {
        return getRankPriority(getRank(p)).toInt();
    }

    public int maxRankPower() {
        return 2;
    }

    public RankPriority getRankPriority(String rank) {
        RankPriority priority = null;
        switch (rank) {
            case "Owner":
                priority = RankPriority.HIGHEST;
                break;
            case "Admin":
                priority = RankPriority.HIGHER;
                break;
            case "Moderator":
                priority = RankPriority.HIGH;
                break;
            case "Member":
                priority = RankPriority.NORMAL;
                break;
        }
        return priority;
    }

    public String getClanTag(String clanID) {
        DataManager dm = new DataManager(clanID, null);
        Config clan = dm.getFile(ConfigType.CLAN_FILE);
        return clan.getConfig().getString("name");
    }

    public String getColor(String color) {
        String result = "&f";
        for (Color c : Color.values()) {
            String cName = c.name().replace("_", "");
            if (color.equalsIgnoreCase(cName)) {
                result = c.toCode();
            }
        }
        return result;
    }

    public List<String> getAllies(String clanID) {
        DataManager dm = new DataManager(clanID, null);
        Config clan = dm.getFile(ConfigType.CLAN_FILE);
        return new ArrayList<>(clan.getConfig().getStringList("allies"));
    }

    public List<String> getEnemies(String clanID) {
        DataManager dm = new DataManager(clanID, null);
        Config clan = dm.getFile(ConfigType.CLAN_FILE);
        return new ArrayList<>(clan.getConfig().getStringList("enemies"));
    }

    public boolean isNeutral(String clanID, String targetClanID) {
        return !getAllies(clanID).contains(targetClanID) && !getEnemies(clanID).contains(targetClanID);
    }

    public String clanRelationColor(String clanID, String targetClanID) {
        String result = "&f&o";
        ClanUtil clanUtil = new ClanUtil();
        try {
            if (clanUtil.getAllClanIDs().contains(targetClanID)) {
                if (isNeutral(clanID, targetClanID)) {
                    result = "&f";
                }
                if (clanID.equals(targetClanID)) {
                    result = "&6&l";
                }
                if (getAllies(clanID).contains(targetClanID)) {
                    result = "&a";
                }
                if (getEnemies(clanID).contains(targetClanID)) {
                    result = "&c";
                }
            }
        } catch (NullPointerException ignored) {
        }
        return result;
    }

    public String getClanPassword(String clanID) {
        DataManager dm = new DataManager(clanID, null);
        Config clan = dm.getFile(ConfigType.CLAN_FILE);
        return clan.getConfig().getString("password");
    }

    public void getMyClanInfo(Player p, int page) {
        String clanID = getClan(p);
        Clan clanIndex = new Clan(clanID, p);
        DataManager dm = new DataManager(clanID, null);
        Config clan = dm.getFile(ConfigType.CLAN_FILE);
        List<String> array = new ArrayList<>();
        String owner = clan.getConfig().getString("owner");
        String password = clan.getConfig().getString("password");
        List<String> members = clan.getConfig().getStringList("members");
        List<String> mods = clan.getConfig().getStringList("moderators");
        List<String> admins = clan.getConfig().getStringList("admins");
        List<String> allies = clan.getConfig().getStringList("allies");
        List<String> allyRequests = clan.getConfig().getStringList("ally-requests");
        List<String> enemies = clan.getConfig().getStringList("enemies");
        array.add(" ");
        array.add("&6&lClan&7: &f" + getColor(clanIndex.getChatColor()) + getClanTag(clanID));
        array.add("&f&m---------------------------");
        array.add("&6" + getRankTag("Owner") + ": &f" + owner);
        if (password == null)
            password = "NO PASS";
        if (clanIndex.getBase() != null)
            array.add("&6Base: &aSet");
        if (clanIndex.getBase() == null)
            array.add("&6Base: &7Not set");
        array.add("&6Color: " + getColor(clanIndex.getChatColor()) + clanIndex.getChatColor());
        array.add("&6Password: &f" + password);
        array.add("&6&lPower [&e" + clanIndex.format(String.valueOf(clanIndex.getPower())) + "&6&l]");
        array.add("&6" + getRankTag("Admin") + "s [&b" + admins.size() + "&6]");
        array.add("&6" + getRankTag("Moderator") + "s [&e" + mods.size() + "&6]");
        array.add("&6Claims [&e" + clanIndex.getOwnedClaims().length + "&6]");
        array.add("&f&m---------------------------");
        if (allyRequests.size() > 0) {
            array.add("&6Ally Requests [&b" + allyRequests.size() + "&6]");
            for (String clanId : allyRequests) {
                array.add("&f- &e&o" + getClanTag(clanId));
            }
        }
        if (allyRequests.isEmpty())
            array.add("&6Ally Requests [&b" + 0 + "&6]");
        if (allies.size() > 0) {
            array.add("&6Allies [&b" + allies.size() + "&6]");
            for (String clanId : allies) {
                array.add("&f- &e&o" + getClanTag(clanId));
            }
        }
        for (String clanId : getAllClanIDs()) {
            if (getEnemies(clanId).contains(getClan(p))) {
                enemies.add(clanId);
            }
        }
        if (allies.isEmpty())
            array.add("&6Allies [&b" + 0 + "&6]");
        if (enemies.size() > 0) {
            array.add("&6Enemies [&b" + enemies.size() + "&6]");
            for (String clanId : enemies) {
                array.add("&f- &c&o" + getClanTag(clanId));
            }
        }
        if (enemies.isEmpty())
            array.add("&6Enemies [&b" + 0 + "&6]");
        array.add("&f&m---------------------------");
        array.add("&n" + getRankTag("Member") + "s&r [&7" + members.size() + "&r]");
        printArray(p, array);
        paginatedMemberList(p, members, page);
        p.sendMessage( " ");
    }

    public String getClanID(String clanName) {
        String result = "N?A";
        for (String ID : getAllClanIDs()) {
        DataManager dm = new DataManager(ID, null);
        Config clan = dm.getFile(ConfigType.CLAN_FILE);
        if (Objects.equals(clan.getConfig().getString("name"), clanName)) {
           result = ID;
        }
        }
        return result;
    }

    public List<String> getAllClanNames() {
        List<String> array = new ArrayList<>();
        for (String clan : getAllClanIDs()) {
            DataManager dm = new DataManager(clan, null);
            Config c = dm.getFile(ConfigType.CLAN_FILE);
            array.add(c.getConfig().getString("name"));
        }
        return array;
    }

    public List<String> getAllClanIDs() {
        DataManager dm = new DataManager();
        List<String> array = new ArrayList<>();
        for (File file : Objects.requireNonNull(dm.getClanFolder().listFiles())) {
            array.add(file.getName().replace(".yml", ""));
        }
        return array;
    }


    private void printArray(Player p, List<String> list) {
        for (String l : list) {
            p.sendMessage(color(l));
        }
    }

    public boolean overPowerBypass() {
        DataManager dm = new DataManager("Config", "Configuration");
        Config main = dm.getFile(ConfigType.MISC_FILE);
        return main.getConfig().getBoolean("Clans.raid-shield.claiming");
    }

    public int tagChangeClearance() {
        DataManager dm = new DataManager("Config", "Configuration");
        Config main = dm.getFile(ConfigType.MISC_FILE);
        return main.getConfig().getInt("Clans.rename-clearance");
    }

    public int colorChangeClearance() {
        DataManager dm = new DataManager("Config", "Configuration");
        Config main = dm.getFile(ConfigType.MISC_FILE);
        return main.getConfig().getInt("Clans.recolor-clearance");
    }

    public int positionClearance() {
        DataManager dm = new DataManager("Config", "Configuration");
        Config main = dm.getFile(ConfigType.MISC_FILE);
        return main.getConfig().getInt("Clans.position-clearance");
    }

    public int unclaimAllClearance() {
        DataManager dm = new DataManager("Config", "Configuration");
        Config main = dm.getFile(ConfigType.MISC_FILE);
        return main.getConfig().getInt("Clans.land-claiming.unclaim-all-clearance");
    }

    public int claimingClearance() {
        DataManager dm = new DataManager("Config", "Configuration");
        Config main = dm.getFile(ConfigType.MISC_FILE);
        return main.getConfig().getInt("Clans.land-claiming.clearance");
    }

    public int baseClearance() {
        DataManager dm = new DataManager("Config", "Configuration");
        Config main = dm.getFile(ConfigType.MISC_FILE);
        return main.getConfig().getInt("Clans.clan-base.clearance");
    }

    public int kickClearance() {
        DataManager dm = new DataManager("Config", "Configuration");
        Config main = dm.getFile(ConfigType.MISC_FILE);
        return main.getConfig().getInt("Clans.kick-clearance");

    }

    public int passwordClearance() {
        DataManager dm = new DataManager("Config", "Configuration");
        Config main = dm.getFile(ConfigType.MISC_FILE);
        return main.getConfig().getInt("Clans.password-clearance");

    }

    private String clanCode() {
        return  serial(4) + "-" + serial(4) + "-" + serial(4);
    }

    private String serial(int count) {
        String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    public void setRaidShield(boolean value) {
        this.raidShield = value;
    }

    public boolean shieldStatus() {
        return raidShield;
    }

    public boolean isNight(String world, int on, int off) {
        Server server = Bukkit.getServer();
        long time = Objects.requireNonNull(server.getWorld(world)).getTime();

        return time <= on || time >= off;
    }

    public void getLeaderboard(Player p, int page) {
        int o = 10;

        HashMap<String, Double> clans = new HashMap<>();

        // Filling the hashMap
        for (String clanID : getAllClanIDs()) {
            Clan clan = new Clan(clanID, null);
            clans.put(getClanTag(clanID), clan.getPower());
        }

        p.sendMessage(color("&7&m------------&7&l[&6&oPage &l" + page + " &7: &6&oTop Clans&7&l]&7&m------------"));
        int totalPageCount = 1;
        if ((clans.size() % o) == 0) {
            if (clans.size() > 0) {
                totalPageCount = clans.size() / o;
            }
        } else {
            totalPageCount = (clans.size() / o) + 1;
        }
        String nextTop = "";
        Double nextTopBal = 0.0;




        if (page <= totalPageCount) {
            // begin line
            if (clans.isEmpty()) {
                p.sendMessage(ChatColor.WHITE + "The list is empty!");
            } else {
                int i1 = 0, k = 0;
                page--;
                HighestValue comp =  new HighestValue(clans);
                TreeMap<String,Double> sorted_map = new TreeMap<>(comp);


                sorted_map.putAll(clans);


                for (Map.Entry<String, Double> clanName : sorted_map.entrySet()) {

                    if (clanName.getValue() > nextTopBal) {
                        nextTop = clanName.getKey();
                        nextTopBal = clanName.getValue();



                    }

                    int pagee = page + 1;

                    k++;
                    if ((((page * o) + i1 + 1) == k) && (k != ((page * o) + o + 1))) {
                        i1++;
                        Clan dummy = new Clan(null, null);
                        if (Bukkit.getServer().getVersion().contains("1.16")) {
                            sendComponent(p, Component.textRunnable("",
                                    " &7# &3&l" + k + " &b&o" + nextTop + " &7: &6&l" + dummy.format(String.valueOf(nextTopBal)),
                                    "&6" + nextTop + " &a&oplaces &7#&6" + k + "&a&o on page " + pagee + ".", "c info " + nextTop));
                        } else {
                            sendComponent(p, ComponentR1_8_1.textRunnable( "",
                                    " &7# &3&l" + k + " &b&o" + nextTop + " &7: &6&l" + dummy.format(String.valueOf(nextTopBal)),
                                    "&6" + nextTop + " &a&oplaces &7#&6" + k + "&a&o on page " + pagee + ".", "c info " + nextTop));
                        }

                    }
                    clans.remove(nextTop);
                    nextTop = "";
                    nextTopBal = 0.0;

                }

                int point; point = page + 1; if (page >= 1) {
                    int last; last = point - 1; point = point + 1;
                    if (Bukkit.getServer().getVersion().contains("1.16")) {
                        sendComponent(p, Component.textRunnable("&b&oNavigate &7[", "&3&lCLICK", "&7] : &7[", "&c&lCLICK&7]", "&b&oClick this to goto the &5&onext page.", "&b&oClick this to go &d&oback a page.", "c top " + point, "c top " + last));
                    } else {
                        sendComponent(p, ComponentR1_8_1.textRunnable( "&b&oNavigate &7[", "&3&lCLICK", "&7] : &7[", "&c&lCLICK&7]", "&b&oClick this to goto the &5&onext page.", "&b&oClick this to go &d&oback a page.", "c top " + point, "c top " + last));
                    }
                } if (page == 0) {
                    point = page + 1 + 1;
                    if (Bukkit.getServer().getVersion().contains("1.16")) {
                        sendComponent(p, Component.textRunnable("&b&oNavigate &7[", "&3&lCLICK", "&7]", "&b&oClick this to goto the &5&onext page.", "c top " + point));
                    } else {
                        sendComponent(p, ComponentR1_8_1.textRunnable( "&b&oNavigate &7[", "&3&lCLICK", "&7]", "&b&oClick this to goto the &5&onext page.", "c top " + point));
                    }
                }


            }
            // end line
        } else
        {
            p.sendMessage(ChatColor.DARK_AQUA + "There are only " + ChatColor.GRAY + totalPageCount + ChatColor.DARK_AQUA + " pages!");

        }
    }

}
