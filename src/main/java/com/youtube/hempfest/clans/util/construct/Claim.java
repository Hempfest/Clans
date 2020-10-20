package com.youtube.hempfest.clans.util.construct;

import com.youtube.hempfest.clans.HempfestClans;
import com.youtube.hempfest.clans.util.data.Config;
import com.youtube.hempfest.clans.util.data.ConfigType;
import com.youtube.hempfest.clans.util.data.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class Claim {

    String claimID;
    Player p;
    DataManager dm = new DataManager("Regions", "Configuration");
    Config regions = dm.getFile(ConfigType.MISC_FILE);

    public Claim(String claimID) {
        this.claimID = claimID;
    }

    public Claim(String claimID, Player p) {
        this.p = p;
        this.claimID = claimID;
    }

    public Clan getClan() {
        return new Clan(getOwner(), p);
    }

    public String getClaimID() {
        return claimID;
    }


    public String getOwner() {
        FileConfiguration d = regions.getConfig();
        String owner = "";
        for (String clan : d.getKeys(false)) {
            for (String s : d.getConfigurationSection(clan + ".Claims").getKeys(false)) {
                if (s.equals(claimID))
                    owner = clan;
            }
        }
        return owner;
    }

    public Location getLocation() {
        Location teleportLocation = null;
        try {
            FileConfiguration d = regions.getConfig();
                String clan = getOwner();
                int x = d.getInt(clan + ".Claims." + claimID + ".X");
                int y = 110;
                int z = d.getInt(clan + ".Claims." + claimID + ".Z");
                String world = d.getString(clan + ".Claims." + claimID + ".World");
                boolean isOnLand = false;
                while (isOnLand == false) {
                    assert world != null;
                    teleportLocation = new Location(Bukkit.getWorld(world), x << 4, y, z << 4).add(7, 0, 7);
                    if (teleportLocation.getBlock().getType() != Material.AIR) {
                        isOnLand = true;
                    } else
                        y--;

                }
        } catch (IllegalArgumentException e) {
            HempfestClans.getInstance().getLogger().severe(String.format("[%s] - A non existent location was pulled while grabbing a non existent directory.", HempfestClans.getInstance().getDescription().getName()));
        }
        return teleportLocation;
    }

}
