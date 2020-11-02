package com.youtube.hempfest.clans.util.events;

import com.youtube.hempfest.clans.HempfestClans;
import com.youtube.hempfest.clans.util.construct.ClanUtil;
import com.youtube.hempfest.clans.util.versions.Component;
import com.youtube.hempfest.clans.util.versions.ComponentR1_8_1;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AllyChatEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final Player chatting;

    private final Set<Player> receivers;

    private String message;

    private boolean cancelled;

    private final boolean isAsync = true;

    private String static1 = "&7[&a&l&nAC&7] ";

    private String static2 = " &7: ";

    private String highlight = "&f&o%s";

    private String playerMeta = "&2&oClan ally &a%s &2&opinged ally chat.";

    private Sound pingSound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;

    public AllyChatEvent(Player sender, Set<Player> receivers, String message, boolean isAsync) {
        super(isAsync);
        this.chatting = sender;
        this.receivers = receivers;
        this.message = message;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
    this.cancelled = b;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getChatting() {
        return chatting;
    }

    public Set<Player> getReceivers() {
        return receivers;
    }

    public String getMessage() {
        return message;
    }

    public Sound getPingSound() {
        return pingSound;
    }

    public String getStatic1() {
        return static1;
    }

    public String getStatic2() {
        return static2;
    }

    public String getHighlight() {
        return highlight;
    }

    public String getPlayerMeta() {
        return playerMeta;
    }

    public void setPingSound(Sound pingSound) {
        this.pingSound = pingSound;
    }

    public void setPlayerMeta(String playerMeta) {
        this.playerMeta = playerMeta;
    }

    public void setStatic1(String static1) {
        this.static1 = static1;
    }

    public void setStatic2(String static2) {
        this.static2 = static2;
    }

    public void setHighlight(String highlight) {
        this.highlight = highlight;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ClanUtil getUtil() {
        return HempfestClans.getInstance().clanUtil;
    }

    public void sendAllyMessage() {
        Player p = getChatting();
        for (Player toGet : getReceivers()) {
            if (getUtil().getClan(toGet) != null) {
                List<String> clanId = new ArrayList<>();
                if (getUtil().getAllies(getUtil().getClan(toGet)).contains(getUtil().getClan(p))) {
                    clanId.add(getUtil().getClan(toGet));
                    if (clanId.size() != 0) {
                        if (Bukkit.getServer().getVersion().contains("1.16")) {
                            getUtil().sendComponent(toGet, Component.textHoverable(static1, String.format(highlight, getUtil().getClanNickname(p)), static2 + getMessage(), String.format(playerMeta, p.getName())));
                        } else {
                            getUtil().sendComponent(toGet, ComponentR1_8_1.textHoverable(static1, String.format(highlight, p.getName()), static2 + getMessage(), String.format(playerMeta, p.getName())));
                        }
                        toGet.playSound(toGet.getLocation(), pingSound, 10, 1);
                    }
                }
            }
        }
        if (Bukkit.getServer().getVersion().contains("1.16")) {
            getUtil().sendComponent(p, Component.textHoverable(static1, String.format(highlight, getUtil().getClanNickname(p)), static2 + getMessage(), String.format(playerMeta, p.getName())));
        } else {
            getUtil().sendComponent(p, ComponentR1_8_1.textHoverable(static1, String.format(highlight, p.getName()), static2 + getMessage(), String.format(playerMeta, p.getName())));
        }
        p.playSound(p.getLocation(), pingSound, 10, 1);
    }

}
