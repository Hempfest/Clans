package com.youtube.hempfest.clans.util.events;

import com.youtube.hempfest.clans.util.construct.Claim;
import com.youtube.hempfest.clans.util.construct.ClaimUtil;
import com.youtube.hempfest.clans.util.construct.ClanUtil;
import com.youtube.hempfest.clans.util.data.Config;
import com.youtube.hempfest.clans.util.data.ConfigType;
import com.youtube.hempfest.clans.util.data.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.*;
import java.util.function.Function;

public class ClaimResidentEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    public static final List<String> residents = new ArrayList<>();

    private final List<Claim> claimData = new ArrayList<>();

    public static final HashMap<UUID, Long> invisibleResident = new HashMap<>();

    public static final List<UUID> tempStorage = new ArrayList<>();

    public static final HashMap<String, String> claimID = new HashMap<>();

    private static final HashMap<String, String> titleContext = new HashMap<>();

    private final Player p;

    private final DataManager dm = new DataManager("Config", "Configuration");

    private final Config main = dm.getFile(ConfigType.MISC_FILE);

    private boolean titlesAllowed = main.getConfig().getBoolean("Clans.land-claiming.send-titles");

    private boolean cancelled;

    private final boolean isAsync = true;

    private final Function<Long, Long> freshResidentJoinTime = s -> s;

    public ClaimResidentEvent(Player p, boolean isAsync) {
        super(isAsync);
        this.p = p;
        getClaimList();
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    public void setTitlesAllowed(boolean b) {
        this.titlesAllowed = b;
    }

    public void setClaimTitle(String title, String subtitle) {
    titleContext.put("TITLE", title);
    titleContext.put("SUB-TITLE", subtitle);
    }

    public void setWildernessTitle(String title, String subtitle) {
        titleContext.put("W-TITLE", title);
        titleContext.put("W-SUB-TITLE", subtitle);
    }

    public String getClaimTitle() {
        return titleContext.get("TITLE");
    }

    public String getClaimSubTitle() {
        return titleContext.get("SUB-TITLE");
    }

    public String getWildernessTitle() {
        return titleContext.get("W-TITLE");
    }

    public String getWildernessSubTitle() {
        return titleContext.get("W-SUB-TITLE");
    }

    public Claim getClaim() {
        return new Claim(claimID.get(p.getName()), p);
    }

    public List<Claim> getClaimList() {
        if (getClaim() != null)
            if (!claimData.contains(getClaim()))
            claimData.add(getClaim());
        return claimData;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public boolean isTitlesAllowed() {
        return titlesAllowed;
    }

    public boolean isWild() {
        ClaimUtil claimUtil = new ClaimUtil();
        boolean result = true;
        if (claimUtil.isInClaim(p.getLocation()))
            result = false;
        return result;
    }

    public Player getResident() {
        return p;
    }

    public List<String> getResidents() {
        return residents;
    }

    public String[] getResidents(String claimID) {
        List<String> values = new ArrayList<>();
            for (String player : getResidents()) {
                if (ClaimResidentEvent.claimID.get(player).equals(claimID)) {
                    values.add(player);
                }
            }
            return values.toArray(new String[0]);
    }

    public float freshResidentJoinTime() {
        return freshResidentJoinTime.apply(invisibleResident.get(p));
    }

    public List<Player> getFreshResidents() {
        List<Player> values = new ArrayList<>();
        for (Map.Entry entry : invisibleResident.entrySet()) {
            Player p = Bukkit.getPlayer(UUID.fromString(entry.getKey().toString()));
            values.add(p);
        }
        return values;
    }

    public boolean lastKnownExists() {
        boolean result = false;
        if (!claimID.containsKey(p.getName())) {
            result = false;
        }
        if (claimID.get(p.getName()) != null) {
            result = true;
        }
        return result;
    }

    public String lastKnownClaim() {
        String result = "Wild";
        if (lastKnownExists())
            result = claimID.get(p.getName());
        return result;
    }

    public void handleUpdate() {
        ClaimUtil claimUtil = new ClaimUtil();
        ClanUtil clanUtil = new ClanUtil();
        titleContext.put("TITLE", "&3&oClaimed land");
        titleContext.put("SUB-TITLE", "&7Owned by: &b%s");
        titleContext.put("W-TITLE", "&4&nWilderness");
        titleContext.put("W-SUB-TITLE", "&7&oOwned by no-one.");
        if (claimUtil.isInClaim(p.getLocation())) {
            Claim claim = new Claim(claimUtil.getClaimID(p.getLocation()));
            if (!lastKnownExists()) {
                claimID.put(p.getName(), claimUtil.getClaimID(p.getLocation()));
            }
            if (lastKnownExists()) {
                if (!claimID.get(p.getName()).equals(claimUtil.getClaimID(p.getLocation()))) {
                    claimID.put(p.getName(), claimUtil.getClaimID(p.getLocation()));
                    if (clanUtil.getClan(p) != null && !getClaim().getOwner().equals(clanUtil.getClan(p))) {
                        if (!Arrays.asList(claim.getClan().getMembers()).contains(p.getName())) {
                            if (!invisibleResident.containsKey(p.getUniqueId()))
                                invisibleResident.put(p.getUniqueId(), p.getLastPlayed());
                            residents.remove(p.getName());
                            tempStorage.add(p.getUniqueId());
                        }
                        if (Arrays.asList(claim.getClan().getMembers()).contains(p.getName())) {
                            invisibleResident.remove(p.getUniqueId());
                        }
                    }
                    if (clanUtil.getClan(p) != null && getClaim().getOwner().equals(clanUtil.getClan(p))) {
                        if (tempStorage.contains(p.getUniqueId())) {
                            if (!invisibleResident.containsKey(p.getUniqueId()))
                                invisibleResident.put(p.getUniqueId(), p.getLastPlayed());
                            residents.remove(p.getName());
                            tempStorage.remove(p.getUniqueId());
                        }
                    }
                }
            }
            if (!residents.contains(p.getName()) || invisibleResident.containsKey(p.getUniqueId())) {
                    String clanName = clanUtil.getClanTag(claim.getOwner());
                    String color = "";
                    if (clanUtil.getClan(p) != null) {
                        color = clanUtil.clanRelationColor(clanUtil.getClan(p), claim.getOwner());
                    } else {
                        color = "&f&o";
                    }
                    if (titlesAllowed) {
                        p.sendTitle(claimUtil.color(String.format(titleContext.get("TITLE"), clanName)), claimUtil.color(String.format(titleContext.get("SUB-TITLE"), color + clanName)), 10, 25, 10);
                    }
                    claimUtil.sendMessage(p, "Now entering &a" + color + clanName + "'s&7 land @ &f(&eX:" + color + getClaim().getLocation().getChunk().getX() + " &eZ:" + color + getClaim().getLocation().getChunk().getZ() + "&f)");
                    residents.add(p.getName());
                invisibleResident.remove(p.getUniqueId());
                    return;
            }
        }

        // Not in a claim area
        if (!claimUtil.isInClaim(p.getLocation())) {
            claimID.remove(p.getName());
            if (!invisibleResident.containsKey(p.getUniqueId()) || residents.contains(p.getName())) {
                if (titlesAllowed) {
                    p.sendTitle(claimUtil.color(titleContext.get("W-TITLE")), claimUtil.color(titleContext.get("W-SUB-TITLE")), 10, 25, 10);
                }
                claimUtil.sendMessage(p, "Now entering &4&nWilderness");
                residents.remove(p.getName());
                invisibleResident.put(p.getUniqueId(), p.getLastPlayed());
                return;
            }
        }
    }

}
