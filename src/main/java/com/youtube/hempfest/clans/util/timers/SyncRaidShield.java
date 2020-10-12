package com.youtube.hempfest.clans.util.timers;

import com.youtube.hempfest.clans.util.events.RaidShieldEvent;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class SyncRaidShield extends BukkitRunnable {
    @Override
    public void run() {
            RaidShieldEvent e = new RaidShieldEvent();
            Bukkit.getPluginManager().callEvent(e);
            if (!e.isCancelled()) {
                e.handleUpdate();
            }
    }
}
