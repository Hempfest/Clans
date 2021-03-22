package com.youtube.hempfest.clans.util.construct;

import com.github.sanctum.labyrinth.formatting.string.RandomID;
import com.github.sanctum.labyrinth.library.TimeUtils;
import com.youtube.hempfest.clans.HempfestClans;
import com.youtube.hempfest.clans.util.StringLibrary;
import com.youtube.hempfest.clans.util.data.Config;
import com.youtube.hempfest.clans.util.data.ConfigType;
import com.youtube.hempfest.clans.util.data.DataManager;
import com.youtube.hempfest.clans.util.events.LandPreClaimEvent;
import com.youtube.hempfest.clans.util.events.LandUnClaimEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class ClaimUtil extends StringLibrary {

	public static DataManager dm = new DataManager("Regions", "Configuration");
	public static Config regions = dm.getFile(ConfigType.MISC_FILE);

	public ClanUtil getUtil() {
		return Clan.clanUtil;
	}

	public void obtain(Player p) {
		LandPreClaimEvent event = new LandPreClaimEvent(p);
		Bukkit.getPluginManager().callEvent(event);
		if (!isInClaim(p.getLocation())) {
			Clan clan = HempfestClans.clanManager(p);

			if (!HempfestClans.getInstance().dataManager.claimEffect()) {
				if (clan.getOwnedClaims().length == maxClaims(p)) {
					sendMessage(p, "&c&oMax claim limit reached, contact a staff member for more info.");
					return;
				}
			}
			if (HempfestClans.getInstance().dataManager.claimEffect() && clan.getOwnedClaims().length >= clan.maxClaims()) {
				sendMessage(p, "&c&oMax claim limit reached grow your clan power and size to obtain more land.");
				return;
			}
			if (clan.getOwnedClaims().length >= maxClaims(p)) {
				sendMessage(p, "&c&oClaim hardcap reached. You have as much land as you can get!");
				return;
			}
			if (!event.isCancelled()) {
				int x = p.getLocation().getChunk().getX();
				int z = p.getLocation().getChunk().getZ();
				String world = p.getWorld().getName();
				FileConfiguration d = regions.getConfig();
				String claimID = serial(6);
				d.set(getUtil().getClan(p) + ".Claims." + claimID + ".X", x);
				d.set(getUtil().getClan(p) + ".Claims." + claimID + ".Z", z);
				d.set(getUtil().getClan(p) + ".Claims." + claimID + ".World", world);
				regions.saveConfig();
				clan.messageClan("&3&oNew land was claimed @ Chunk position: &7X:&b" + x + " &7Z:&b" + z + " &3&oin world &7" + world);
				chunkBorderHint(p);
			}
		} else if (isInClaim(p.getLocation())) {
			Claim claim = new Claim(getClaimID(p.getLocation()));
			if (claim.getOwner().equals(getUtil().getClan(p))) {
				sendMessage(p, alreadyOwnClaim());
			} else {
				sendMessage(p, notClaimOwner(getUtil().clanRelationColor(getUtil().getClan(p), claim.getOwner()) + getUtil().getClanTag(claim.getOwner())));

			}
		}
	}

	public final Map<String, Integer> claimsOverPowered = new HashMap<>();
	public final Map<UUID, Date> cooldown = new HashMap<>();

	public void remove(Player p) {
		FileConfiguration d = regions.getConfig();
		Clan clan = HempfestClans.clanManager(p);
		Claim claim = new Claim(getClaimID(p.getLocation()));
		if (isInClaim(p.getLocation())) {
			LandUnClaimEvent event = new LandUnClaimEvent(p, claim);
			Bukkit.getPluginManager().callEvent(event);
			if (Arrays.asList(clan.getOwnedClaims()).contains(getClaimID(p.getLocation()))) {
				if (!event.isCancelled()) {
					d.set(getUtil().getClan(p) + ".Claims." + getClaimID(p.getLocation()), null);
					regions.saveConfig();
					int x = p.getLocation().getChunk().getX();
					int z = p.getLocation().getChunk().getZ();
					String world = p.getWorld().getName();
					clan.messageClan("&e&oLand was un-claimed @ Chunk position: &7X:&3" + x + " &7Z:&3" + z + " &e&oin world &7" + world);
				}
			} else {
				if (getUtil().shieldStatus()) {
					if (getUtil().overPowerBypass()) {

						Clan owner = claim.getClan();
						if (clan.getPower() > owner.getPower()) {
							if (HempfestClans.getMain().getConfig().getBoolean("Clans.raid-shield.claiming-only-enemy")) {
								if (!clan.getEnemies().contains(owner.getClanID())) {
									sendMessage(p, "&c&oYou are not enemies with this clan. Unable to overpower ally claim.");
									return;
								}
							}
							if (!event.isCancelled()) {
								if (HempfestClans.getMain().getConfig().getBoolean("Clans.land-claiming.over-powering.cooldown.enabled")) {
									if (cooldown.containsKey(p.getUniqueId())) {
										long mins = HempfestClans.getMain().getConfig().getInt("Clans.land-claiming.over-powering.cooldown.length");
										if (TimeUtils.isMinutesSince(cooldown.get(p.getUniqueId()), mins)) {
											cooldown.remove(p.getUniqueId());
										} else {
											sendMessage(p, "&c&oYou cannot do this right now, your clan is on cooldown.");
											return;
										}
									} else {
										if (!claimsOverPowered.containsKey(clan.getClanID()))
											claimsOverPowered.putIfAbsent(clan.getClanID(), 1);

										int i = claimsOverPowered.get(clan.getClanID()) + 1;
										if (claimsOverPowered.get(clan.getClanID()) <= HempfestClans.getMain().getConfig().getInt("Clans.land-claiming.over-powering.cooldown.after-uses")) {
											claimsOverPowered.put(clan.getClanID(), i);
										} else {
											cooldown.put(p.getUniqueId(), new Date());
											claimsOverPowered.remove(clan.getClanID());
											return;
										}
									}
								}
								d.set(claim.getOwner() + ".Claims." + getClaimID(p.getLocation()), null);
								regions.saveConfig();
								int x = p.getLocation().getChunk().getX();
								int z = p.getLocation().getChunk().getZ();
								String world = p.getWorld().getName();
								owner.messageClan("&7[&4CLAIM-BREACH&7] &6Clan &d&o" + getUtil().getClanTag(getUtil().getClan(p)) + "&r&o:");
								owner.messageClan("&7&oLand was &4&nover-powered&7&o @ Chunk position: &7X:&c" + x + " &7Z:&c" + z + " &7&oin world &4" + world);
							}
						} else {
							sendMessage(p, "&cYour clans power is too weak in comparison.");
						}
					} else {
						sendMessage(p, "&5&oYou cannot attempt anything right now.. The shield is resilient.");
					}
				} else {
					Clan owner = claim.getClan();
					if (clan.getPower() > owner.getPower()) {
						if (HempfestClans.getMain().getConfig().getBoolean("Clans.raid-shield.claiming-only-enemy")) {
							if (!clan.getEnemies().contains(owner.getClanID())) {
								sendMessage(p, "&c&oYou are not enemies with this clan. Unable to overpower ally claim.");
								return;
							}
						}
						if (HempfestClans.getMain().getConfig().getBoolean("Clans.land-claiming.over-powering.cooldown.enabled")) {
							if (cooldown.containsKey(p.getUniqueId())) {
								long mins = HempfestClans.getMain().getConfig().getInt("Clans.land-claiming.over-powering.cooldown.length");
								if (TimeUtils.isMinutesSince(cooldown.get(p.getUniqueId()), mins)) {
									cooldown.remove(p.getUniqueId());
								} else {
									sendMessage(p, "&c&oYou cannot do this right now, your clan is on cooldown.");
									return;
								}
							} else {
								if (!claimsOverPowered.containsKey(clan.getClanID()))
									claimsOverPowered.putIfAbsent(clan.getClanID(), 1);

								int i = claimsOverPowered.get(clan.getClanID()) + 1;
								if (claimsOverPowered.get(clan.getClanID()) <= HempfestClans.getMain().getConfig().getInt("Clans.land-claiming.over-powering.cooldown.after-uses")) {
									claimsOverPowered.put(clan.getClanID(), i);
								} else {
									cooldown.put(p.getUniqueId(), new Date());
									claimsOverPowered.remove(clan.getClanID());
									return;
								}
							}
						}
						d.set(claim.getOwner() + ".Claims." + getClaimID(p.getLocation()), null);
						regions.saveConfig();
						int x = p.getLocation().getChunk().getX();
						int z = p.getLocation().getChunk().getZ();
						String world = p.getWorld().getName();
						owner.messageClan("&7[&4HIGHER-POWER&7] &d&o" + getUtil().getClanTag(getUtil().getClan(p)) + "&r&o:");
						owner.messageClan("&7&oLand was &4&nover-powered&7&o @ Chunk position: &7X:&3" + x + " &7Z:&3" + z + " &7&oin world &4" + world);
					} else {
						sendMessage(p, "&cYour clans power is too weak in comparison.");
					}
				}
			}
		} else {
			sendMessage(p, "This land belongs to: &4&nWilderness&r, and is free to claim.");
		}
	}

	public void removeAll(Player p) {
		FileConfiguration d = regions.getConfig();
		if (!d.isConfigurationSection(getUtil().getClan(p) + ".Claims")) {
			sendMessage(p, "Your clan has no land to unclaim. Consider obtaining some?");
			return;
		}
		if (!Objects.requireNonNull(d.getConfigurationSection(getUtil().getClan(p) + ".Claims")).getKeys(false).isEmpty()) {
			d.set(getUtil().getClan(p) + ".Claims", null);
			d.createSection(getUtil().getClan(p) + ".Claims");
			regions.saveConfig();
			Clan clan = HempfestClans.clanManager(p);
			clan.messageClan("&e&oAll land has been un-claimed by: &3&n" + p.getName());

		} else {
			sendMessage(p, "Your clan has no land to unclaim. Consider obtaining some?");
		}
	}

	public void loadClaims() {
		FileConfiguration d = regions.getConfig();
		for (String clan : d.getKeys(false)) {
			for (String s : Objects.requireNonNull(d.getConfigurationSection(clan + ".Claims")).getKeys(false)) {
				int x = d.getInt(clan + ".Claims." + s + ".X");
				int z = d.getInt(clan + ".Claims." + s + ".Z");
				String w = d.getString(clan + ".Claims." + s + ".World");
				String[] ID = {clan, s, w};
				int[] pos = {x, z};
				HempfestClans.getInstance().claimMap.put(ID, pos);
			}
		}
	}

	public boolean isInClaim(Location loc) {
		for (Map.Entry<String[], int[]> entry : HempfestClans.getInstance().claimMap.entrySet()) {
			String[] ID = entry.getKey();
			int[] pos = entry.getValue();
			//String clanID = ID[0];
			//String claimID = ID[1];
			String world = ID[2];
			int x = pos[0];
			int z = pos[1];
			if (loc.getChunk().getX() == x && loc.getChunk().getZ() == z && Objects.requireNonNull(loc.getWorld()).getName().equals(world)) {
				return true;
			}
		}
		return false;
	}

	public int maxClaims(Player player) {
		int returnv = 0;
		if (player == null)
			return 0;
		for (int i = 100; i >= 0; i--) {
			if (player.hasPermission("clans.claim.infinite")) {
				returnv = -1;
				break;
			}
			if (player.hasPermission("clans.claim." + i)) {
				returnv = i;
				break;
			}
		}
		if (returnv == -1)
			return 999;

		return returnv;
	}

	public String getClaimID(Location loc) {
		FileConfiguration d = regions.getConfig();
		String id = "";
		if (isInClaim(loc)) {
			for (Map.Entry<String[], int[]> entry : HempfestClans.getInstance().claimMap.entrySet()) {
				String[] ID = entry.getKey();// ID[1] = name
				int[] pos = entry.getValue();// pos[0] = x, pos[1] = z
				if (loc.getChunk().getX() == pos[0] && loc.getChunk().getZ() == pos[1]) {
					id = ID[1];
				}
			}
			//for (String clan : d.getKeys(false)) {
			// for (String s : Objects.requireNonNull(d.getConfigurationSection(clan + ".Claims")).getKeys(false)) {
			//int x = d.getInt(clan + ".Claims." + s + ".X");
			//int z = d.getInt(clan + ".Claims." + s + ".Z");
			// if (loc.getChunk().getX() == x && loc.getChunk().getZ() == z) {
			//  id = s;
			// }
			// }
			// }
		}
		return id;
	}

	public List<String> getAllClaims() {
		List<String> array = new ArrayList<>();
		for (Map.Entry<String[], int[]> entry : HempfestClans.getInstance().claimMap.entrySet()) {
			String[] ID = entry.getKey();
			if (!array.contains(ID[1])) {
				array.add(ID[1]);
			}
		}
		return array;
	}

	public String[] getClaimInfo(String claimID) {
		String[] result = new String[0];
		for (Map.Entry<String[], int[]> entry : HempfestClans.getInstance().claimMap.entrySet()) {
			String[] ID = entry.getKey();
			if (ID[1].equals(claimID)) {
				result = ID;
			}
		}
		return result;
	}

	public int[] getClaimPosition(String[] key) {
		int[] result = new int[0];
		for (Map.Entry<String[], int[]> entry : HempfestClans.getInstance().claimMap.entrySet()) {
			String[] ID = entry.getKey();
			int[] pos = entry.getValue();
			if (Arrays.equals(key, ID)) {
				result = pos;
			}
		}
		return result;
	}

	private String serial(int count) {
		return new RandomID(count, "AKZ0123456789").generate();
	}

	public boolean claimingAllowed() {
		DataManager dm = new DataManager("Config", "Configuration");
		Config main = dm.getFile(ConfigType.MISC_FILE);
		return main.getConfig().getBoolean("Clans.land-claiming.allow");
	}


}
