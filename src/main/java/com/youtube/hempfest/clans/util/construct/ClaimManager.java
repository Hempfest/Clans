package com.youtube.hempfest.clans.util.construct;

import com.youtube.hempfest.clans.HempfestClans;
import com.youtube.hempfest.clans.util.data.Config;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

public class ClaimManager {

	private final Config regions;
	private final LinkedList<Claim> claims;

	public ClaimManager() {
		this.claims = new LinkedList<>();
		this.regions = Config.get("Regions", "Configuration");
	}

	public static ClaimManager getInstance() {
		return HempfestClans.getInstance().claimManager;
	}

	public boolean isInClaim(Location location) {
		return claims.stream().anyMatch(c -> location.getChunk().getX() == c.getPos()[0] && location.getChunk().getZ() == c.getPos()[1] && location.getWorld().getName().equals(c.getKey()[2]));
	}

	public boolean isInClaim(int x, int z, String world) {
		return claims.stream().anyMatch(c -> x == c.getPos()[0] && z == c.getPos()[1] && world.equals(c.getKey()[2]));
	}

	public boolean isInClaim(Chunk chunk) {
		return claims.stream().anyMatch(c -> chunk.getX() == c.getPos()[0] && chunk.getZ() == c.getPos()[1] && chunk.getWorld().getName().equals(c.getKey()[2]));
	}

	public String getId(Location location) {
		return claims.stream().filter(c -> location.getChunk().getX() == c.getPos()[0] && location.getChunk().getZ() == c.getPos()[1] && location.getWorld().getName().equals(c.getKey()[2])).map(Claim::getClaimID).findFirst().orElse(null);
	}

	public String getId(int x, int z, String world) {
		return claims.stream().filter(c -> x == c.getPos()[0] && z == c.getPos()[1] && world.equals(c.getKey()[2])).map(Claim::getClaimID).findFirst().orElse(null);
	}

	public Claim getClaim(String claimID) {
		return claims.stream().filter(c -> c.getClaimID().equals(claimID)).findFirst().orElse(null);
	}

	public Config getFile() {
		return regions;
	}

	public List<Claim> getClaims() {
		return Collections.unmodifiableList(claims);
	}

	public void refresh() {
		claims.clear();
		FileConfiguration d = regions.getConfig();
		for (String clan : d.getKeys(false)) {
			for (String claimID : Objects.requireNonNull(d.getConfigurationSection(clan + ".Claims")).getKeys(false)) {
				int x = d.getInt(clan + ".Claims." + claimID + ".X");
				int z = d.getInt(clan + ".Claims." + claimID + ".Z");
				String w = d.getString(clan + ".Claims." + claimID + ".World");
				String[] ID = {clan, claimID, w};
				int[] pos = {x, z};
				Claim c = new Claim(ID, pos);
				claims.add(c);
			}
		}
	}

}
