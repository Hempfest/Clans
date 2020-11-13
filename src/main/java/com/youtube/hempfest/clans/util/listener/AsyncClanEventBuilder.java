package com.youtube.hempfest.clans.util.listener;

import com.youtube.hempfest.clans.util.StringLibrary;
import com.youtube.hempfest.clans.util.construct.ClanUtil;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class AsyncClanEventBuilder extends Event {


    protected AsyncClanEventBuilder(boolean isAsync) {
        super(isAsync);
    }

    public abstract HandlerList getHandlers();

    public abstract HandlerList getHandlerList();

    public abstract ClanUtil getUtil();

    public abstract StringLibrary stringLibrary();


}
