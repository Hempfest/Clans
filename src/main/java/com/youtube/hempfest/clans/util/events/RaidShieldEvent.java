package com.youtube.hempfest.clans.util.events;

import com.youtube.hempfest.clans.util.StringLibrary;
import com.youtube.hempfest.clans.util.construct.Clan;
import com.youtube.hempfest.clans.util.construct.ClanUtil;
import com.youtube.hempfest.clans.util.data.Config;
import com.youtube.hempfest.clans.util.data.ConfigType;
import com.youtube.hempfest.clans.util.data.DataManager;
import com.youtube.hempfest.clans.util.listener.ClanEventBuilder;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class RaidShieldEvent extends ClanEventBuilder implements Cancellable {

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
        return Clan.clanUtil.shieldStatus();
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

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public ClanUtil getUtil() {
        return Clan.clanUtil;
    }

    @Override
    public StringLibrary stringLibrary() {
        return new StringLibrary();
    }

    public void handleUpdate() {

                String world = main.getConfig().getString("Clans.raid-shield.main-world");
                if (Clan.clanUtil.isNight(world, on, off)) {
                    if (Clan.clanUtil.shieldStatus()) {
                        Clan.clanUtil.setRaidShield(false);
                        Bukkit.broadcastMessage(Clan.clanUtil.color(String.format(shieldOff, Clan.clanUtil.getPrefix())));
                    }
                }
                if (!Clan.clanUtil.isNight(world, on, off)) {
                    if (!Clan.clanUtil.shieldStatus()) {
                        Clan.clanUtil.setRaidShield(true);
                        Bukkit.broadcastMessage(Clan.clanUtil.color(String.format(shieldOn, Clan.clanUtil.getPrefix())));
                    }
                }
    }

}
