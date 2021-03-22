package com.youtube.hempfest.clans.util.listener;

import com.github.sanctum.labyrinth.data.VaultHook;
import com.github.sanctum.labyrinth.task.Schedule;
import com.youtube.hempfest.clans.HempfestClans;
import com.youtube.hempfest.clans.util.construct.Claim;
import com.youtube.hempfest.clans.util.events.ClaimInteractEvent;
import com.youtube.hempfest.clans.util.events.ClanCreateEvent;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockEventListener implements Listener {

	@EventHandler
	public void onClanBuy(ClanCreateEvent event) {
		Player p = event.getMaker();
		if (HempfestClans.getMain().getConfig().getBoolean("Clans.creation.charge")) {
			double amount = HempfestClans.getMain().getConfig().getDouble("Clans.creation.amount");
			EconomyResponse takeMoney = VaultHook.getEconomy().withdrawPlayer(p, amount);
			if (!takeMoney.transactionSuccess()) {
				event.setCancelled(true);
				event.stringLibrary().sendMessage(p, "&c&oYou don't have enough money. Amount needed: &6" + amount);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPistonExtend(BlockPistonExtendEvent e) {
		Block piston = e.getBlock();
		if (!Claim.claimUtil.isInClaim(piston.getLocation())) {
			for (Block pushed : e.getBlocks()) {
				if (Claim.claimUtil.isInClaim(pushed.getLocation())) {
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPistonRetract(BlockPistonRetractEvent e) {
		Block piston = e.getBlock();
		if (!Claim.claimUtil.isInClaim(piston.getLocation())) {
			for (Block pushed : e.getBlocks()) {
				if (Claim.claimUtil.isInClaim(pushed.getLocation())) {
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onBlockBreak(BlockBreakEvent event) {

		ClaimInteractEvent e = new ClaimInteractEvent(event.getPlayer(), event.getBlock().getLocation());
		Bukkit.getPluginManager().callEvent(e);
		e.handleCheck();
		if (e.isCancelled()) {
			event.setCancelled(true);
		} else {
			if (Claim.claimUtil.isInClaim(event.getBlock().getLocation())) {
				if (Claim.claimUtil.isInClaim(event.getPlayer().getLocation()) && Claim.claimUtil.isInClaim(event.getBlock().getLocation())) {
					Schedule.sync(() -> Claim.getResident(event.getPlayer()).addBroken(event.getBlock())).run();
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onBlockPlace(BlockPlaceEvent event) {
		ClaimInteractEvent e = new ClaimInteractEvent(event.getPlayer(), event.getBlock().getLocation());
		Bukkit.getPluginManager().callEvent(e);
		e.handleCheck();
		if (e.isCancelled()) {
			event.setCancelled(true);
		} else {
			if (Claim.claimUtil.isInClaim(event.getPlayer().getLocation()) && Claim.claimUtil.isInClaim(event.getBlock().getLocation())) {
				Schedule.sync(() -> Claim.getResident(event.getPlayer()).addPlaced(event.getBlock())).run();
			}
		}
	}

}
