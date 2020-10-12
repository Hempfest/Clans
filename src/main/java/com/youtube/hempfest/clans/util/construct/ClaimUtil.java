package com.youtube.hempfest.clans.util.construct;

import com.youtube.hempfest.clans.HempfestClans;
import com.youtube.hempfest.clans.util.StringLibrary;
import com.youtube.hempfest.clans.util.data.Config;
import com.youtube.hempfest.clans.util.data.ConfigType;
import com.youtube.hempfest.clans.util.data.DataManager;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClaimUtil extends StringLibrary {

    DataManager dm = new DataManager("Regions", "Configuration");
    Config regions = dm.getFile(ConfigType.MISC_FILE);
    ClanUtil clanUtil = new ClanUtil();

    public void obtain(Player p) {
        if (!isInClaim(p.getLocation())) {
            int x = p.getLocation().getChunk().getX();
            int z = p.getLocation().getChunk().getZ();
            String world = p.getWorld().getName();
            FileConfiguration d = regions.getConfig();
            String claimID = serial(6);
            d.set(clanUtil.getClan(p) + ".Claims." + claimID + ".X", x);
            d.set(clanUtil.getClan(p) + ".Claims." + claimID + ".Z", z);
            d.set(clanUtil.getClan(p) + ".Claims." + claimID + ".World", world);
            regions.saveConfig();
            Clan clan = new Clan(clanUtil.getClan(p), p);
            clan.messageClan("&3&oNew land was claimed @ Chunk position: &7X:&b" + x + " &7Z:&b" + z + " &3&oin world &7" + world);
            chunkBorderHint(p);
        } else {
            Claim claim = new Claim(getClaimID(p.getLocation()));
            if (claim.getOwner().equals(clanUtil.getClan(p))) {
                sendMessage(p, alreadyOwnClaim());
            } else {
                sendMessage(p, notClaimOwner(clanUtil.clanRelationColor(clanUtil.getClan(p), claim.getOwner()) + clanUtil.getClanTag(claim.getOwner())));
                return;
            }
        }
    }

    public void remove(Player p) {
        FileConfiguration d = regions.getConfig();
        Clan clan = new Clan(clanUtil.getClan(p), p);
        if (isInClaim(p.getLocation())) {
            if (Arrays.asList(clan.getOwnedClaims()).contains(getClaimID(p.getLocation()))) {
                d.set(clanUtil.getClan(p) + ".Claims." + getClaimID(p.getLocation()), null);
                regions.saveConfig();
                int x = p.getLocation().getChunk().getX();
                int z = p.getLocation().getChunk().getZ();
                String world = p.getWorld().getName();
                clan.messageClan("&e&oLand was un-claimed @ Chunk position: &7X:&3" + x + " &7Z:&3" + z + " &e&oin world &7" + world);
            } else {
                if (HempfestClans.getInstance().shield.shieldStatus()) {
                    if (clanUtil.overPowerBypass()) {
                        Claim claim = new Claim(getClaimID(p.getLocation()), p);
                        Clan clan2 = new Clan(claim.getOwner(), p);
                        if (clan.getPower() > clan2.getPower()) {
                            d.set(claim.getOwner() + ".Claims." + getClaimID(p.getLocation()), null);
                            regions.saveConfig();
                            int x = p.getLocation().getChunk().getX();
                            int z = p.getLocation().getChunk().getZ();
                            String world = p.getWorld().getName();
                            Clan result = new Clan(claim.getOwner(), p);
                            result.messageClan("&7[&4CLAIM-BREACH&7] &6Clan &d&o" + clanUtil.getClanTag(clanUtil.getClan(p)) + "&r&o:");
                            result.messageClan("&7&oLand was &4&nover-powered&7&o @ Chunk position: &7X:&c" + x + " &7Z:&c" + z + " &7&oin world &4" + world);
                        } else {
                            sendMessage(p, "&cYour clans power is too weak in comparison.");
                            return;
                        }
                    } else {
                        sendMessage(p, "&5&oYou cannot attempt anything right now.. The shield is resilient.");
                        return;
                    }
                } else {
                    Claim claim = new Claim(getClaimID(p.getLocation()), p);
                    Clan clan2 = new Clan(claim.getOwner(), p);
                    if (clan.getPower() > clan2.getPower()) {
                        d.set(claim.getOwner() + ".Claims." + getClaimID(p.getLocation()), null);
                        regions.saveConfig();
                        int x = p.getLocation().getChunk().getX();
                        int z = p.getLocation().getChunk().getZ();
                        String world = p.getWorld().getName();
                        Clan result = new Clan(claim.getOwner(), p);
                        result.messageClan("&7[&4HIGHER-POWER&7] &d&o" + clanUtil.getClanTag(clanUtil.getClan(p)) + "&r&o:");
                        result.messageClan("&7&oLand was &4&nover-powered&7&0 @ Chunk position: &7X:&3" + x + " &7Z:&3" + z + " &7&oin world &4" + world);
                    } else {
                        sendMessage(p, "&cYour clans power is too weak in comparison.");
                        return;
                    }
                    return;
                }
            }
        } else {
            sendMessage(p, "This land belongs to: &4&nWilderness&r, and is free to claim.");
        }
    }

    public void removeAll(Player p) {
        FileConfiguration d = regions.getConfig();
        if (!d.getConfigurationSection(clanUtil.getClan(p) + ".Claims").getKeys(false).isEmpty()) {
            d.set(clanUtil.getClan(p) + ".Claims", null);
            d.createSection(clanUtil.getClan(p) + ".Claims");
            regions.saveConfig();
            Clan clan = new Clan(clanUtil.getClan(p), p);
            clan.messageClan("&e&oAll land has been un-claimed by: &3&n" + p.getName());
        } else {
            sendMessage(p, "Your clan has no land to unclaim. Consider obtaining some?");
        }
    }

    public boolean isInClaim(Location loc) {
        FileConfiguration d = regions.getConfig();
        for (String clan : d.getKeys(false)) {
            for (String s : d.getConfigurationSection(clan + ".Claims").getKeys(false)) {
                int x = d.getInt(clan + ".Claims." + s + ".X");
                int z = d.getInt(clan + ".Claims." + s + ".Z");
                String w = d.getString(clan + ".Claims." + s + ".World");
                if ((loc.getChunk().getX() <= x) && (loc.getChunk().getZ() <= z) && (loc.getChunk().getX() >= x)
                        && (loc.getChunk().getZ() >= z) && loc.getWorld().getName().equals(w)) {
                    return true;
                }

            }
        }

        return false;
    }

    public String getClaimID(Location loc) {
        FileConfiguration d = regions.getConfig();
        String id = "";
        if (isInClaim(loc)) {
            for (String clan : d.getKeys(false)) {
                for (String s : d.getConfigurationSection(clan + ".Claims").getKeys(false)) {
                    int x = d.getInt(clan + ".Claims." + s + ".X");
                    int z = d.getInt(clan + ".Claims." + s + ".Z");
                    if (loc.getChunk().getX() == x && loc.getChunk().getZ() == z) {
                        id = s;
                    }
                }
            }
        }
        return id;
    }

    public List<String> getAllClaims() {
        List<String> array = new ArrayList<>();
        for (String region : regions.getConfig().getKeys(false)) {
            array.add(region);
        }
        return array;
    }

    private String serial(int count) {
        String ALPHA_NUMERIC_STRING = "AKZ0123456789";
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }




}
