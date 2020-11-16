package com.youtube.hempfest.clans.util.dynmap;

import com.youtube.hempfest.clans.util.construct.Claim;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerSet;

public class HempfestDynmapIntegration {

	public MarkerSet markerset = null;

	public static DynmapAPI dapi = null;

	public void registerDynmap() {
		dapi = (DynmapAPI) Bukkit.getServer().getPluginManager().getPlugin("dynmap");
	}

	public void fillMap() {
		try {
			markerset = dapi.getMarkerAPI().createMarkerSet("clans.claim.markerset", "Claims", dapi.getMarkerAPI().getMarkerIcons(), false);
		} catch (NullPointerException e){
			markerset = dapi.getMarkerAPI().getMarkerSet("clans.claim.markerset");
		}
		for (String claim : Claim.claimUtil.getAllClaims()) {

			Claim c = new Claim(claim);
			int cx1 = c.getChunk().getX()*16;
			int cz1 = c.getChunk().getZ()*16;

			int cx2 = c.getChunk().getX()*16+15;
			int cz2 = c.getChunk().getZ()*16+15;

			AreaMarker am = markerset.createAreaMarker(c.getClaimID(), c.getClan().getClanTag(), false, c.getChunk().getWorld().getName(), new double[1000], new double[1000], false);
			double[] d1 = {cx1, cx2};
			double[] d2 = {cz1, cz2};
			am.setCornerLocations(d1, d2);
			am.setLabel(c.getClaimID());
			am.setDescription(c.getClan().getClanTag() + " - " + Arrays.asList(c.getClan().getMembers()).toString());
			int stroke = 1;
			double strokeOpac = 50.5;
			am.setLineStyle(stroke, strokeOpac, 0x03d3fc);
			am.setFillStyle(1, 0x03d3fc);
		}
	}

	public void updateMap(String claimID) {
		Claim c = new Claim(claimID);
		int cx1 = c.getChunk().getX()*16;
		int cz1 = c.getChunk().getZ()*16;

		int cx2 = c.getChunk().getX()*16+15;
		int cz2 = c.getChunk().getZ()*16+15;
		String world = c.getChunk().getWorld().getName();

		AreaMarker am = markerset.createAreaMarker(c.getClaimID(), c.getClan().getClanTag(), false, c.getChunk().getWorld().getName(), new double[1000], new double[1000], false);
		double[] d1 = {cx1, cx2};
		double[] d2 = {cz1, cz2};
		am.setCornerLocations(d1, d2);
		am.setLabel(c.getClaimID());
		am.setDescription(c.getClan().getClanTag() + " - " + Arrays.asList(c.getClan().getMembers()).toString());
		int stroke = 3;
		double strokeOpac = 50.5;
		double Opac = 10.5;
		am.setLineStyle(stroke, strokeOpac, 0x03d3fc);
		am.setFillStyle(Opac, 0x03d3fc);
	}

	public void removeMarker(String claimID) {
		Claim c = new Claim(claimID);
		AreaMarker am = markerset.createAreaMarker(c.getClaimID(), c.getClan().getClanTag(), false, c.getChunk().getWorld().getName(), new double[1000], new double[1000], false);
		markerset.getAreaMarkers().remove(am);
	}



}
