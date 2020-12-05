package com.youtube.hempfest.clans.util.listener;


import com.youtube.hempfest.clans.HempfestClans;

public class TabCompleteEntry {

	public static void add(String value) {
		HempfestClans.getInstance().tabList.add(value);
	}

	public static void remove(String value) {
		HempfestClans.getInstance().tabList.remove(value);
	}

	public static String[] getContainer() {
		return HempfestClans.getInstance().tabList.toArray(new String[0]);
	}


}
