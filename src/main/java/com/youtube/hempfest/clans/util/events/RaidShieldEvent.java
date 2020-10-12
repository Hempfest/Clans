package com.youtube.hempfest.clans.util.events;

import com.youtube.hempfest.clans.HempfestClans;
import com.youtube.hempfest.clans.util.data.Config;
import com.youtube.hempfest.clans.util.data.ConfigType;
import com.youtube.hempfest.clans.util.data.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RaidShieldEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final DataManager dm = new DataManager("Config", "Configuration");

    private final Config main = dm.getFile(ConfigType.MISC_FILE);

    private int on = main.getConfig().getInt("Clans.raid-shield.up-time");

    private int off = main.getConfig().getInt("Clans.raid-shield.down-time");

    private String shieldOn = "%s &a&lRAID SHIELD ENABLED";

    private String shieldOff = "%s &c&lRAID SHIELD DISABLED";

    private boolean cancelled;

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    public int getStartTime() {
        return on;
    }

    public int getStopTime() {
        return off;
    }

    public String getShieldOn() {
        return shieldOn;
    }

    public String getShieldOff() {
        return shieldOff;
    }

    public boolean shieldOn() {
        return HempfestClans.getInstance().shield.shieldStatus();
    }

    public void setShieldOn(String shieldOn) {
        this.shieldOn = shieldOn;
    }

    public void setShieldOff(String shieldOff) {
        this.shieldOff = shieldOff;
    }

    public void setStartTime(int i) {
        this.on = i;
    }

    public void setStopTime(int i) {
        this.off = i;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    private boolean configAllow() {
        return main.getConfig().getBoolean("Clans.raid-shield.allow");
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public void handleUpdate() {
            if (configAllow()) {

                String world = main.getConfig().getString("Clans.raid-shield.main-world");
                if (HempfestClans.getInstance().shield.isNight(world, on, off)) {
                    if (HempfestClans.getInstance().shield.shieldStatus() == true) {
                        HempfestClans.getInstance().shield.setRaidShield(false);
                        Bukkit.broadcastMessage(HempfestClans.getInstance().shield.color(String.format(shieldOff, HempfestClans.getInstance().shield.getPrefix())));
                    }
                }
                if (!HempfestClans.getInstance().shield.isNight(world, on, off)) {
                    if (HempfestClans.getInstance().shield.shieldStatus() == false) {
                        HempfestClans.getInstance().shield.setRaidShield(true);
                        Bukkit.broadcastMessage(HempfestClans.getInstance().shield.color(String.format(shieldOn, HempfestClans.getInstance().shield.getPrefix())));
                    }
                }
            }
    }

}
