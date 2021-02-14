package com.youtube.hempfest.clans.metadata;

import com.github.sanctum.labyrinth.library.HUID;
import com.youtube.hempfest.clans.util.construct.Clan;
import java.io.Serializable;

public abstract class ClanMeta implements Serializable {

	public abstract HUID getId();

	public abstract String value();

	public abstract String value(int index);

	public abstract Clan getClan();

}
