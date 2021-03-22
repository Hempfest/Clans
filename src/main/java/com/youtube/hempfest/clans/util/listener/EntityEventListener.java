package com.youtube.hempfest.clans.util.listener;

import com.youtube.hempfest.clans.util.construct.Claim;
import com.youtube.hempfest.clans.util.events.ClaimInteractEvent;
import com.youtube.hempfest.clans.util.events.PlayerPunchPlayerEvent;
import com.youtube.hempfest.clans.util.events.PlayerShootPlayerEvent;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

public class EntityEventListener implements Listener {


	@EventHandler(priority = EventPriority.NORMAL)
	public void onTNTExplode(EntityExplodeEvent e) {
		if (e.getEntity() instanceof Creeper) {
			for (Block exploded : e.blockList()) {
				if (Claim.claimUtil.isInClaim(exploded.getLocation())) {
					e.setCancelled(true);
					break;
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onProjectileHit(ProjectileHitEvent event) {
		if (event.getEntity().getShooter() instanceof Player) {
			Player p = (Player) event.getEntity().getShooter();
			ClaimInteractEvent e = new ClaimInteractEvent(p, event.getEntity().getLocation());
			Bukkit.getPluginManager().callEvent(e);
			e.handleCheck();
			if (e.isCancelled()) {
				event.getEntity().remove();
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerHit(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
			Player target = (Player) event.getEntity();
			Player p = (Player) event.getDamager();
			PlayerPunchPlayerEvent e = new PlayerPunchPlayerEvent(p, target);
			Bukkit.getPluginManager().callEvent(e);
			e.perform();
			event.setCancelled(e.canHurt());
		}

		if (event.getEntity() instanceof Player && event.getDamager() instanceof Projectile && (
				(Projectile) event.getDamager()).getShooter() instanceof Player) {
			Projectile pr = (Projectile) event.getDamager();
			Player p = (Player) pr.getShooter();
			Player target = (Player) event.getEntity();
			PlayerShootPlayerEvent e = new PlayerShootPlayerEvent(p, target);
			Bukkit.getPluginManager().callEvent(e);
			e.perform();
			event.setCancelled(e.canHurt());
		}

	}

}
