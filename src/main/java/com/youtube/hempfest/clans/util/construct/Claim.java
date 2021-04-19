package com.youtube.hempfest.clans.util.construct;

import com.youtube.hempfest.clans.HempfestClans;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class Claim {

	public static ClaimUtil claimUtil = new ClaimUtil();

	private final String[] key;
	private final int[] pos;

	protected Claim(String[] key, int[] pos) {
		this.key = key;
		this.pos = pos;
	}

	/**
	 * Attempt converting a given location to a claim.
	 *
	 * @param loc The location to convert
	 * @return A clan claim or null if the location has no id.
	 */
	public static Claim from(Location loc) {
		return claimUtil.getClaimID(loc) != null ? HempfestClans.getInstance().claimManager.getClaim(claimUtil.getClaimID(loc)) : null;
	}

	/**
	 * Attempt converting a given location to a claim.
	 *
	 * @param chunk The chunk to convert
	 * @return A clan claim or null if the location has no id.
	 */
	public static Claim from(Chunk chunk) {
		return HempfestClans.getInstance().claimManager.getId(chunk.getX(), chunk.getZ(), chunk.getWorld().getName()) != null ? HempfestClans.getInstance().claimManager.getClaim(HempfestClans.getInstance().claimManager.getId(chunk.getX(), chunk.getZ(), chunk.getWorld().getName())) : null;
	}

	public String[] getKey() {
		return this.key;
	}

	public int[] getPos() {
		return this.pos;
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
		return this.key[1];
	}

	/**
	 * @return Gets the clanID of the claim object.
	 */
	public String getOwner() {
		return this.key[0];
	}

	/**
	 * @return Gets the specific chunk of the claim location.
	 */
	public Chunk getChunk() {
		return getLocation().getChunk();
	}

	/**
	 * @return Gets a list of all known online residents within the claim.
	 */
	public List<Resident> getResidents() {
		List<Resident> query = new ArrayList<>();
		for (Resident r : HempfestClans.residents) {
			if (r.getClaim().getClaimID().equals(getClaimID())) {
				query.add(r);
			}
		}
		return query;
	}

	/**
	 * @param name The player name to search for
	 * @return A resident object for the given claim
	 */
	public Resident getResident(String name) {
		return HempfestClans.residents.stream().filter(r -> r.getPlayer().getName().equals(name)).findFirst().orElse(null);
	}

	/**
	 * @return Gets the centered location of the claim objects chunk.
	 */
	public Location getLocation() {
		int x = this.pos[0];
		int y = 110;
		int z = this.pos[1];
		String world = this.key[2];
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

	public static Resident getResident(Player p) {
		return HempfestClans.residents.stream().filter(r -> r.getPlayer().getName().equals(p.getName())).findFirst().orElse(null);
	}

}
