package com.youtube.hempfest.clans.util.construct;

import com.youtube.hempfest.clans.HempfestClans;
import com.youtube.hempfest.clans.util.data.Config;
import com.youtube.hempfest.clans.util.data.ConfigType;
import com.youtube.hempfest.clans.util.data.DataManager;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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

	/**
	 * @param p variable constructor access is deprecated and replaced by
	 *          {@link #Claim(String claimID)}
	 * @deprecated As of version 2.0.6
	 */
	@Deprecated
	public Claim(String claimID, Player p) {
		this.p = p;
		this.claimID = claimID;
	}

	/**
	 * @return Gets a new clan object regarding properties of the claim owner.
	 */
	public Clan getClan() {
		return Clan.clanUtil.getClan(getOwner());
	}

	/**
	 * @return Gets the claimID from the claim object
	 */
	public String getClaimID() {
		return this.claimID;
	}

	/**
	 * @return Gets the clanID of the claim object.
	 */
	public String getOwner() {
		String owner = "";
		for (Map.Entry<String[], int[]> entry : HempfestClans.getInstance().claimMap.entrySet()) {
			String[] id = entry.getKey();
			int[] pos = entry.getValue();
			if (id[1].equals(claimID)) {
				owner = id[0];
				break;
			}
		}
		return owner;
	}

	/**
	 * @return Gets the specific chunk of the claim location.
	 */
	public Chunk getChunk() {
		return getLocation().getChunk();
	}

	/**
	 * @return Gets the centered location of the claim objects chunk.
	 */
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
		if (!feet.getType().equals(Material.AIR) && !feet.getLocation().add(0.0D, 1.0D, 0.0D).getBlock().getType().equals(Material.AIR))
			return false;
		Block head = feet.getRelative(BlockFace.UP);
		if (!head.getType().equals(Material.AIR))
			return false;
		Block ground = feet.getRelative(BlockFace.DOWN);
		return ground.getType().isSolid();
	}
}
