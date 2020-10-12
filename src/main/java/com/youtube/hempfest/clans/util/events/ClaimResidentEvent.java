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

    private static List<Player> residents = new ArrayList<>();

    private static HashMap<UUID, Long> invisibleResident = new HashMap<>();

    private static HashMap<Player, String> claimID = new HashMap<>();

    private Player p;

    private String claimTitle = "&3&oClaimed land";

    private String claimSubTitle = "&7Owned by: &b%s";

    private String wildernessTitle = "&4&nWilderness";

    private String wildernessSubTitle = "&7&oOwned by no-one.";

    private DataManager dm = new DataManager("Config", "Configuration");

    private Config main = dm.getFile(ConfigType.MISC_FILE);

    private boolean titlesAllowed = main.getConfig().getBoolean("Clans.land-claiming.send-titles");

    private boolean cancelled;

    private final boolean isAsync = true;

    private Function<Long, Long> freshResidentJoinTime = s -> s;

    public ClaimResidentEvent(Player p, boolean isAsync) {
        super(isAsync);
        this.p = p;
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

    public void setClaimTitle(String claimTitle) {
        this.claimTitle = claimTitle;
    }

    public void setClaimSubTitle(String claimSubTitle) {
        this.claimSubTitle = claimSubTitle;
    }

    public void setWildernessTitle(String wildernessTitle) {
        this.wildernessTitle = wildernessTitle;
    }

    public void setWildernessSubTitle(String wildernessSubTitle) {
        this.wildernessSubTitle = wildernessSubTitle;
    }

    public String getClaimTitle() {
        return claimTitle;
    }

    public String getClaimSubTitle() {
        return claimSubTitle;
    }

    public String getWildernessTitle() {
        return wildernessTitle;
    }

    public String getWildernessSubTitle() {
        return wildernessSubTitle;
    }

    public Claim getClaim() {
        return new Claim(claimID.get(p), p);
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

    public List<Player> getResidents() {
        return residents;
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
        if (!claimID.containsKey(p)) {
            result = false;
        }
        if (claimID.get(p) != null) {
            result = true;
        }
        return result;
    }

    public String lastKnownClaim() {
        String result = "Wild";
        if (lastKnownExists())
            result = claimID.get(p);
        return result;
    }

    public void handleUpdate() {
        ClaimUtil claimUtil = new ClaimUtil();
        Claim claim = new Claim(claimUtil.getClaimID(p.getLocation()));
        if (claimUtil.isInClaim(p.getLocation())) {
            if (!lastKnownExists()) {
                claimID.put(p, claimUtil.getClaimID(p.getLocation()));
            }
            if (lastKnownExists()) {
                if (!claimID.get(p).equals(claimUtil.getClaimID(p.getLocation()))) {
                    if (!Arrays.asList(claim.getClan().getMembers()).contains(p.getName())) {
                        if (!invisibleResident.containsKey(p.getUniqueId())) {
                            invisibleResident.put(p.getUniqueId(), p.getLastPlayed());
                        }
                        if (residents.contains(p)) {
                            residents.remove(p);
                        }
                        claimID.put(p, claimUtil.getClaimID(p.getLocation()));
                    }
                    if (Arrays.asList(claim.getClan().getMembers()).contains(p.getName())) {
                        if (!invisibleResident.containsKey(p.getUniqueId())) {
                            invisibleResident.put(p.getUniqueId(), p.getLastPlayed());
                        }
                        if (residents.contains(p)) {
                            residents.remove(p);
                        }
                        claimID.put(p, claimUtil.getClaimID(p.getLocation()));
                    }
                }
            }
            if (!residents.contains(p)) {
                ClanUtil clanUtil = new ClanUtil();
                String clanName = clanUtil.getClanTag(claim.getOwner());
                String color = "";
                if (clanUtil.getClan(p) != null) {
                    color = clanUtil.clanRelationColor(clanUtil.getClan(p), claim.getOwner());
                } else {
                    color = "&f&o";
                }
                if (titlesAllowed) {
                    p.sendTitle(claimUtil.color(String.format(claimTitle, clanName)), claimUtil.color(String.format(claimSubTitle, color + clanName)), 10, 25, 10);
                }
                claimUtil.sendMessage(p, "Now entering &a" + color + clanName + "'s&7 land @ &f(&eX:" + color + getClaim().getLocation().getChunk().getX() + " &eZ:" + color + getClaim().getLocation().getChunk().getZ() + "&f)");
                residents.add(p);
                if (!invisibleResident.containsKey(p.getUniqueId())) {
                    invisibleResident.put(p.getUniqueId(), p.getLastPlayed());
                }
            }
        }
        if (!claimUtil.isInClaim(p.getLocation())) {
            if (claimID.containsKey(p)) {
                claimID.remove(p);
            }
            if (!invisibleResident.containsKey(p.getUniqueId())) {
                invisibleResident.put(p.getUniqueId(), p.getLastPlayed());
                if (titlesAllowed) {
                    p.sendTitle(claimUtil.color(wildernessTitle), claimUtil.color(wildernessSubTitle), 10, 25, 10);
                }
                claimUtil.sendMessage(p, "Now entering &4&nWilderness");
            }
            if (residents.contains(p)) {
                residents.remove(p);
                if (titlesAllowed) {
                    p.sendTitle(claimUtil.color(wildernessTitle), claimUtil.color(wildernessSubTitle), 10, 25, 10);
                }
                claimUtil.sendMessage(p, "Now entering &4&nWilderness");
            }
        }
    }

}
