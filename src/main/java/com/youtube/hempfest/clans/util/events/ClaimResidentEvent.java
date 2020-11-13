package com.youtube.hempfest.clans.util.events;

import com.youtube.hempfest.clans.util.StringLibrary;
import com.youtube.hempfest.clans.util.construct.Claim;
import com.youtube.hempfest.clans.util.construct.ClaimUtil;
import com.youtube.hempfest.clans.util.construct.Clan;
import com.youtube.hempfest.clans.util.construct.ClanUtil;
import com.youtube.hempfest.clans.util.data.DataManager;
import com.youtube.hempfest.clans.util.listener.AsyncClanEventBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class ClaimResidentEvent extends AsyncClanEventBuilder implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    public static final List<String> residents = new ArrayList<>();

    private final List<Claim> claimData = new ArrayList<>();

    public static final HashMap<UUID, Long> invisibleResident = new HashMap<>();

    public static final List<UUID> tempStorage = new ArrayList<>();

    public static final HashMap<String, String> claimID = new HashMap<>();

    private static final HashMap<String, String> titleContext = new HashMap<>();

    private final Player p;

    private boolean titlesAllowed = DataManager.titlesAllowed();

    private boolean cancelled;

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

    @Override
    public HandlerList getHandlerList() {
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
        for (Map.Entry<UUID, Long> entry : invisibleResident.entrySet()) {
            Player p = Bukkit.getPlayer(UUID.fromString(entry.getKey().toString()));
            values.add(p);
        }
        return values;
    }

    public boolean lastKnownExists() {
        boolean result = false;
        if (claimID.get(p.getName()) != null) {
            result = true;
        }
        return result;
    }

    /*
    / Equals Wild if not in a claim
     */
    public String lastKnownClaim() {
        String result = "Wild";
        if (lastKnownExists())
            result = claimID.get(p.getName());
        return result;
    }

    public ClanUtil getUtil() {
        return Clan.clanUtil;
    }

    @Override
    public StringLibrary stringLibrary() {
        return new StringLibrary();
    }

    public ClaimUtil getClaimUtil() {
        return Claim.claimUtil;
    }

    public void handleUpdate() {
        titleContext.put("TITLE", "&3&oClaimed land");
        titleContext.put("SUB-TITLE", "&7Owned by: &b%s");
        titleContext.put("W-TITLE", "&4&nWilderness");
        titleContext.put("W-SUB-TITLE", "&7&oOwned by no-one.");
        if (getClaimUtil().isInClaim(p.getLocation())) {
            if (!lastKnownExists()) {
                // Filling the claim history thats non existent under circumstance A
                claimID.put(p.getName(), getClaimUtil().getClaimID(p.getLocation()));
            }
            if (lastKnownExists()) {
                // Checking clan information to decide wether or not to resend claim title.
                if (!claimID.get(p.getName()).equals(getClaimUtil().getClaimID(p.getLocation()))) {
                    claimID.put(p.getName(), getClaimUtil().getClaimID(p.getLocation()));
                    if (getUtil().getClan(p) != null && !getClaim().getOwner().equals(getUtil().getClan(p))) {
                        if (!Arrays.asList(getClaim().getClan().getMembers()).contains(p.getName())) {
                            if (!invisibleResident.containsKey(p.getUniqueId()))
                                invisibleResident.put(p.getUniqueId(), p.getLastPlayed());
                            residents.remove(p.getName());
                            tempStorage.add(p.getUniqueId());
                        }
                        if (Arrays.asList(getClaim().getClan().getMembers()).contains(p.getName())) {
                            invisibleResident.remove(p.getUniqueId());
                        }
                    }
                    if (getUtil().getClan(p) != null && getClaim().getOwner().equals(getUtil().getClan(p))) {
                        if (tempStorage.contains(p.getUniqueId())) {
                            if (!invisibleResident.containsKey(p.getUniqueId()))
                                invisibleResident.put(p.getUniqueId(), p.getLastPlayed());
                            residents.remove(p.getName());
                            tempStorage.remove(p.getUniqueId());
                        }
                    }
                }
            }
            // Sending the title under specific circumstance
            if (!residents.contains(p.getName()) || invisibleResident.containsKey(p.getUniqueId())) {
                    String clanName = getUtil().getClanTag(getClaim().getOwner());
                    String color = "";
                    if (getUtil().getClan(p) != null) {
                        color = getUtil().clanRelationColor(getUtil().getClan(p), getClaim().getOwner());
                    } else {
                        color = "&f&o";
                    }
                    if (titlesAllowed) {
                        p.sendTitle(getClaimUtil().color(String.format(titleContext.get("TITLE"), clanName)), getClaimUtil().color(String.format(titleContext.get("SUB-TITLE"), color + clanName)), 10, 25, 10);
                    }
                    getClaimUtil().sendMessage(p, "Now entering &a" + color + clanName + "'s&7 land @ &f(&eX:" + color + getClaim().getLocation().getChunk().getX() + " &eZ:" + color + getClaim().getLocation().getChunk().getZ() + "&f)");
                    residents.add(p.getName());
                    invisibleResident.remove(p.getUniqueId());
                    return;
            }
        }

        // Not in a claim area, sending wilderness title
        if (!getClaimUtil().isInClaim(p.getLocation())) {
            claimID.remove(p.getName());
            if (!invisibleResident.containsKey(p.getUniqueId()) || residents.contains(p.getName())) {
                if (titlesAllowed) {
                    p.sendTitle(getClaimUtil().color(titleContext.get("W-TITLE")), getClaimUtil().color(titleContext.get("W-SUB-TITLE")), 10, 25, 10);
                }
                getClaimUtil().sendMessage(p, "Now entering &4&nWilderness");
                residents.remove(p.getName());
                invisibleResident.put(p.getUniqueId(), p.getLastPlayed());
            }
        }
    }

}
