package com.youtube.hempfest.clans.util.construct;

import com.youtube.hempfest.clans.util.data.Config;
import com.youtube.hempfest.clans.util.data.ConfigType;
import com.youtube.hempfest.clans.util.data.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class Claim {
	private final String claimID;

	private Player p;

	private final DataManager dm = new DataManager("Regions", "Configuration");

	private final Config regions = this.dm.getFile(ConfigType.MISC_FILE);

	public static ClaimUtil claimUtil = new ClaimUtil();

	public Claim(String claimID) {
		this.claimID = claimID;
	}

	public Claim(String claimID, Player p) {
		this.p = p;
		this.claimID = claimID;
	}

	public void loadPlayer(Player p) {
		this.p = p;
	}

	public Clan getClan() {
		Clan clan = null;
		for (Clan clans : Clan.clanUtil.getClans) {
			if (clans.getClanID().equals(getOwner()))
				clan = clans;
		}
		return clan;
	}

	public String getClaimID() {
		return this.claimID;
	}

	public String getOwner() {
		FileConfiguration d = this.regions.getConfig();
		String owner = "";
		for (String clan : d.getKeys(false)) {
			for (String s : d.getConfigurationSection(clan + ".Claims").getKeys(false)) {
				if (s.equals(this.claimID))
					owner = clan;
			}
		}
		return owner;
	}

	public Chunk getChunk() {
		return getLocation().getChunk();
	}

	public Location getLocation() {
		String[] ID = claimUtil.getClaimInfo(this.claimID);
		int[] pos = claimUtil.getClaimPosition(ID);
		int x = pos[0];
		int y = 110;
		int z = pos[1];
		String world = ID[2];
		Location teleportLocation = (new Location(Bukkit.getWorld(world), (x << 4), y, (z << 4))).add(7.0D, 0.0D, 7.0D);
		if (Bukkit.getVersion().contains("1.12"))
			return teleportLocation;
		if (!hasSurface(teleportLocation))
			teleportLocation = (new Location(Bukkit.getWorld(world), (x << 4), y, (z << 4))).add(7.0D, 10.0D, 7.0D);
		return teleportLocation;
	}

	private boolean hasSurface(Location location) {
		Block feet = location.getBlock();
		if (!feet.getType().isAir() && !feet.getLocation().add(0.0D, 1.0D, 0.0D).getBlock().getType().isAir())
			return false;
		Block head = feet.getRelative(BlockFace.UP);
		if (!head.getType().isAir())
			return false;
		Block ground = feet.getRelative(BlockFace.DOWN);
		return ground.getType().isSolid();
	}
}
