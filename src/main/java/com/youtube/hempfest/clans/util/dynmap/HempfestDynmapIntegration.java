package com.youtube.hempfest.clans.util.dynmap;

import com.youtube.hempfest.clans.util.construct.Claim;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerSet;

public class HempfestDynmapIntegration {

	private String failedAttempt;

	public MarkerSet markerset = null;

	public static DynmapAPI dapi = null;

	public void registerDynmap() {
		dapi = (DynmapAPI) Bukkit.getServer().getPluginManager().getPlugin("dynmap");
		try {
			markerset = dapi.getMarkerAPI().createMarkerSet("clans.claim.markerset", "Claims", dapi.getMarkerAPI().getMarkerIcons(), false);
		} catch (NullPointerException e){
			markerset = dapi.getMarkerAPI().getMarkerSet("clans.claim.markerset");
		}
	}

	public void fillMap(String[] ownedClaims) {
		int i = 0;
		for (String claim : ownedClaims) {

			Claim c = new Claim(claim);
			int cx1 = c.getChunk().getX()*16;
			int cz1 = c.getChunk().getZ()*16;

			int cx2 = c.getChunk().getX()*16+16;
			int cz2 = c.getChunk().getZ()*16+16;

			AreaMarker am = markerset.createAreaMarker(c.getClaimID(), c.getClan().getClanTag(), false, c.getChunk().getWorld().getName(), new double[1000], new double[1000], false);
				double[] d1 = {cx1, cx2};
				double[] d2 = {cz1, cz2};
				try {
					am.setCornerLocations(d1, d2);
					am.setLabel(c.getClaimID());
					am.setDescription(c.getClan().getClanTag() + " - " + Arrays.asList(c.getClan().getMembers()).toString());
					int stroke = 1;
					double strokeOpac = 0.0;
					double Opac = 0.3;
					am.setLineStyle(stroke, strokeOpac, 0xedfffc);
					am.setFillStyle(Opac, 0x42cbf5);
				} catch (NullPointerException e) {
					i++;
				}
		}
		if (i > 0) {
			if (Bukkit.getVersion().contains("1.16")) {
				setFailedAttempt("&#f5bf42&oA number of claims have already been marked and are being skipped. &f(&#42f5da" + i + "&f)");
			} else {
				setFailedAttempt("&6&oA number of claims have already been marked and are being skipped. &f(&b" + i + "&f)");
			}
		}
	}

	public String getFailedAttempt() {
		return failedAttempt;
	}

	private void setFailedAttempt(String failedAttempt) {
		this.failedAttempt = failedAttempt;
	}

	/*
	public void updateMap(String claimID) {
		Claim c = new Claim(claimID);
		int cx1 = c.getChunk().getX()*16;
		int cz1 = c.getChunk().getZ()*16;

		int cx2 = c.getChunk().getX()*16+17;
		int cz2 = c.getChunk().getZ()*16+17;
		String world = c.getChunk().getWorld().getName();

		AreaMarker am = markerset.createAreaMarker(c.getClaimID(), c.getClan().getClanTag(), false, c.getChunk().getWorld().getName(), new double[1000], new double[1000], false);
		double[] d1 = {cx1, cx2};
		double[] d2 = {cz1, cz2};
		am.setCornerLocations(d1, d2);
		am.setLabel(c.getClaimID());
		am.setDescription(c.getClan().getClanTag() + " - " + Arrays.asList(c.getClan().getMembers()).toString());
		int stroke = 12;
		double strokeOpac = 0.0;
		double Opac = 100;
		am.setLineStyle(stroke, strokeOpac, 0x42cbf5);
		am.setFillStyle(Opac, 0x42cbf5);
	}

	 */

	public void removeMarker(String claimID) {
		Claim c = new Claim(claimID);
		for (AreaMarker areaMarker : markerset.getAreaMarkers()) {
			if (areaMarker.getMarkerID().equals(c.getClaimID())){
				areaMarker.deleteMarker();
			}
		}

	}



}
