package com.youtube.hempfest.clans.util.events;

import com.youtube.hempfest.clans.util.StringLibrary;
import com.youtube.hempfest.clans.util.construct.Clan;
import com.youtube.hempfest.clans.util.construct.ClanUtil;
import com.youtube.hempfest.clans.util.listener.AsyncClanEventBuilder;
import com.youtube.hempfest.hempcore.formatting.component.Text;
import com.youtube.hempfest.hempcore.formatting.component.Text_R2;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class ClanChatEvent extends AsyncClanEventBuilder implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final Player chatting;

    private final Set<Player> receivers;

    private String message;

    private boolean cancelled;

    private final boolean isAsync = true;

    private String static1 = "&7[&3&l&nCC&7] ";

    private String static2 = " &7: ";

    private String highlight = "&f&o%s";

    private String playerMeta = "&3&oClan member &b%s &3&opinged clan chat.";

    private Sound pingSound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;

     {
        if (Bukkit.getVersion().contains("1.16")) {
            static1 = "&#5b626e[&#16b7db&l&nCC&#5b626e] ";
            static2 = " &#5b626e: &f";
            highlight = "&#1696db&o%s";
        }
    }

    public ClanChatEvent(Player sender, Set<Player> receivers, String message, boolean isAsync) {
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

    public void sendClanMessage() {
        ClanUtil clanUtil = new ClanUtil();
        Player p = getChatting();
        for (Player toGet : getReceivers()) {
            if (clanUtil.getClan(toGet) != null) {
                if (clanUtil.getClan(toGet).equals(clanUtil.getClan(p))) {
                    if (Bukkit.getServer().getVersion().contains("1.16")) {
                        clanUtil.sendComponent(toGet, new Text().textHoverable(static1, String.format(highlight, clanUtil.getClanNickname(p)), static2 + getMessage(), String.format(playerMeta, p.getName())));
                    } else {
                        clanUtil.sendComponent(toGet, Text_R2.textHoverable(static1, String.format(highlight, p.getName()), static2 + getMessage(), String.format(playerMeta, p.getName())));
                    }
                    toGet.playSound(toGet.getLocation(), pingSound, 10, 1);
                }
            }
        }
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
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
}
