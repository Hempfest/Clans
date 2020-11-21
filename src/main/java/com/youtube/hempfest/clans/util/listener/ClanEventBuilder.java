package com.youtube.hempfest.clans.util.listener;

import com.youtube.hempfest.clans.util.StringLibrary;
import com.youtube.hempfest.clans.util.construct.ClanUtil;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class ClanEventBuilder extends Event{

    public abstract HandlerList getHandlers();

    public abstract ClanUtil getUtil();

    public abstract StringLibrary stringLibrary();


}
