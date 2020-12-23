package com.youtube.hempfest.clans.util.timers;

import com.youtube.hempfest.clans.util.construct.Claim;
import com.youtube.hempfest.clans.util.events.ClaimResidentEvent;
import com.youtube.hempfest.clans.util.events.WildernessInhabitantEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class SyncClaimResident extends BukkitRunnable {

    @Override
    public void run() {
        if (Bukkit.getOnlinePlayers().size() > 0) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!Claim.claimUtil.isInClaim(p.getLocation())) {
                    WildernessInhabitantEvent event = new WildernessInhabitantEvent(p);
                    Bukkit.getPluginManager().callEvent(event);
                    if (!event.isCancelled()) {
                        event.handleUpdate();
                    }
                } else {
                    ClaimResidentEvent event = new ClaimResidentEvent(p);
                    Bukkit.getPluginManager().callEvent(event);
                    if (!event.isCancelled()) {
                        event.handleUpdate();
                    }
                }
            }
        }
    }
}
