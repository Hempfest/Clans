package com.youtube.hempfest.clans.util.events;

import com.youtube.hempfest.clans.util.construct.Clan;
import com.youtube.hempfest.clans.util.construct.ClanUtil;
import com.youtube.hempfest.clans.util.data.Config;
import com.youtube.hempfest.clans.util.data.ConfigType;
import com.youtube.hempfest.clans.util.data.DataManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerKillPlayerEvent extends Event{

    private static final HandlerList handlers = new HandlerList();

    private final Player killer;

    private final Player victim;

    public PlayerKillPlayerEvent(Player p, Player target) {
        this.killer = p;
        this.victim = target;
    }

    public Player getShooter() {
        return killer;
    }

    public Player getShot() {
        return victim;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public HandlerList getHandlerList() {
        return handlers;
    }

    public ClanUtil getUtil() {
        return new ClanUtil();
    }

    public Clan getClan(String clanID, Player clanPlayer) {
        return new Clan(clanID, clanPlayer);
    }

    public void perform() {
        ClanUtil clanUtil = getUtil();
        if (clanUtil.getClan(killer) != null) {
            Clan kill = getClan(clanUtil.getClan(killer), killer);
            DataManager dm = new DataManager(killer.getUniqueId().toString(), null);
            Config user = dm.getFile(ConfigType.USER_FILE);
            int kills = user.getConfig().getInt("kills");
            if (clanUtil.getClan(victim) != null) {
                if (!clanUtil.getClan(killer).equals(clanUtil.getClan(victim))) {
                    Clan dead = getClan(clanUtil.getClan(victim), victim);
                    kill.givePower(0.11);
                    dead.takePower(0.11);
                    DataManager dm2 = new DataManager(victim.getUniqueId().toString(), null);
                    Config user2 = dm2.getFile(ConfigType.USER_FILE);
                    int deaths = user2.getConfig().getInt("deaths");
                    user.getConfig().set("kills", (kills + 1));
                    user2.getConfig().set("deaths", (deaths + 1));
                    user.saveConfig();
                    user2.saveConfig();
                }
            } else {
                // victim not in a clan
                user.getConfig().set("kills", (kills + 1));
                user.saveConfig();
                kill.givePower(0.11);
            }
        }
        if (clanUtil.getClan(killer) == null) {
            if (clanUtil.getClan(victim) != null) {
                    Clan dead = getClan(clanUtil.getClan(victim), victim);
                    dead.takePower(0.11);
                    DataManager dm2 = new DataManager(victim.getUniqueId().toString(), null);
                    Config user2 = dm2.getFile(ConfigType.USER_FILE);
                    int deaths = user2.getConfig().getInt("deaths");
                    user2.getConfig().set("deaths", (deaths + 1));
                    user2.saveConfig();
            }
        }
    }
}
