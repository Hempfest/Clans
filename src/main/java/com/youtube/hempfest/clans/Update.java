package com.youtube.hempfest.clans;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class Update {

	private final int PROJECT_ID = 78415;
	private URL checkURL;
	private String newVersion;
	private final HempfestClans plugin;

	public Update(HempfestClans plugin) {
		this.plugin = plugin;
		this.newVersion = plugin.getDescription().getVersion();
		try {
			this.checkURL = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + PROJECT_ID);
		} catch (MalformedURLException e) {
		}
	}

	public String getLatestVersion() {
		return newVersion;
	}

	public String getResourceURL() {
		return "https://www.spigotmc.org/resources/" + PROJECT_ID;
	}

	public boolean hasUpdate() throws Exception {
		URLConnection con = checkURL.openConnection();
		this.newVersion = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
		String version = plugin.getDescription().getVersion();
		String[] vSplit = version.split("\\.");
		String[] nvSplit = newVersion.split("\\.");
		int vlast = Integer.parseInt(vSplit[2]);
		int vmid = Integer.parseInt(vSplit[1]);
		int zlast = Integer.parseInt(nvSplit[2]);
		int zmid = Integer.parseInt(nvSplit[1]);
		int vstart = Integer.parseInt(vSplit[0]);
		int zstart = Integer.parseInt(nvSplit[0]);
		if (zstart > vstart) {
			return true;
		}
		if (zstart == vstart && zmid == vmid) {
			if (zlast == vlast) {
				return false;
			}
			return zlast > vlast;
		}
		if (zstart == vstart && zmid > vmid) {
			if (zlast > vlast) {
				return true;
			}
			return true;
		}
		return false;
	}

}
