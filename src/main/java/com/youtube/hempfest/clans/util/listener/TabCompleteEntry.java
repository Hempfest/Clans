package com.youtube.hempfest.clans.util.listener;


import java.util.ArrayList;
import java.util.List;

public class TabCompleteEntry {

	private static final List<String> tabList = new ArrayList<>();

	public static void add(String value) {
		tabList.add(value);
	}

	public static void remove(String value) {
		tabList.remove(value);
	}

	public static String[] getContainer() {
		return tabList.toArray(new String[0]);
	}


}
