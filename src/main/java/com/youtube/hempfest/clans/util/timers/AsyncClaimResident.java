package com.youtube.hempfest.clans.util.timers;

import com.youtube.hempfest.clans.util.events.ClaimResidentEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AsyncClaimResident extends BukkitRunnable {

    Player p;

    public AsyncClaimResident(Player p) {
        this.p = p;
    }

    @Override
    public void run() {
        ClaimResidentEvent event = new ClaimResidentEvent(p, true);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            event.handleUpdate();
        }
    }
}
