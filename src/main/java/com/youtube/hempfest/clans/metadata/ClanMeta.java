package com.youtube.hempfest.clans.metadata;

import com.youtube.hempfest.clans.util.construct.Clan;
import com.youtube.hempfest.hempcore.library.HUID;
import java.io.Serializable;

public abstract class ClanMeta implements Serializable {

	public abstract HUID getId();

	public abstract String value();

	public abstract String value(int index);

	public abstract Clan getClan();

}
