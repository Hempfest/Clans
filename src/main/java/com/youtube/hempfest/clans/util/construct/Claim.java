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
	private final Config regions = dm.getFile(ConfigType.MISC_FILE);
	public static ClaimUtil claimUtil = new ClaimUtil();

	public Claim(String claimID) {
		this.claimID = claimID;
	}


	/**
	 * @param p variable constructor access is deprecated and replaced by
	 *          {@link #loadPlayer(Player p)}
	 * @deprecated As of version 2.0.6
	 */
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
			if (clans.getClanID().equals(getOwner())) {
				clan = clans;
			}
		}
		return clan;
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

	public Chunk getChunk() {
		return getLocation().getChunk();
	}

	public Location getLocation() {
		String[] ID = Claim.claimUtil.getClaimInfo(claimID);
		int[] pos = Claim.claimUtil.getClaimPosition(ID);
		int x = pos[0];
		int y = 110;
		int z = pos[1];
		String world = ID[2];
		Location teleportLocation = new Location(Bukkit.getWorld(world), x << 4, y, z << 4).add(7, 0, 7);
		if (!hasSurface(teleportLocation)) {
			teleportLocation = new Location(Bukkit.getWorld(world), x << 4, y, z << 4).add(7, 10, 7);
		}
		return teleportLocation;
	}

	private boolean hasSurface(Location location) {
		Block feet = location.getBlock();
		if (!feet.getType().isAir() && !feet.getLocation().add(0, 1, 0).getBlock().getType().isAir()) {
			return false; // not transparent (will suffocate)
		}
		Block head = feet.getRelative(BlockFace.UP);
		if (!head.getType().isAir()) {
			return false; // not transparent (will suffocate)
		}
		Block ground = feet.getRelative(BlockFace.DOWN);
		return ground.getType().isSolid(); // not solid
	}

}
