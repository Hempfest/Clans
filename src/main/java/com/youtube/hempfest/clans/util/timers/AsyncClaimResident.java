package com.youtube.hempfest.clans.util.timers;

import com.youtube.hempfest.clans.util.events.ClaimResidentEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AsyncClaimResident extends BukkitRunnable {

    @Override
    public void run() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            ClaimResidentEvent event = new ClaimResidentEvent(p, true);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                event.handleUpdate();
            }
        }
    }
}
