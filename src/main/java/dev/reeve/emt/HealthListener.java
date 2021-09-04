package dev.reeve.emt;

import dev.reeve.emt.event.*;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class HealthListener implements Listener {
	public final HashSet<UUID> down = new HashSet<>();
	public final HashSet<UUID> hurt = new HashSet<>();
	public final HashSet<UUID> recovering = new HashSet<>();
	public final HashSet<UUID> pickingUp = new HashSet<>();
	public final HashSet<UUID> operatingOn = new HashSet<>();
	public final HashMap<UUID, BukkitRunnable> downCountdowns = new HashMap<>();
	public final HashMap<UUID, BukkitRunnable> emtCountdowns = new HashMap<>();
	private final Main main;
	private final Config config;
	
	public HealthListener(Main main, Config config) {
		this.main = main;
		this.config = config;
	}
	
	@EventHandler
	public void onHealthChange(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			Player player = (Player) e.getEntity();
			UUID uuid = player.getUniqueId();
			if ((int) (player.getHealth() - e.getDamage() - 0.5) <= (int) config.downHealth && (int) (player.getHealth() - e.getDamage()) > 0 && !down.contains(player.getUniqueId())) {
				player.teleport(getFirstBlock(player.getLocation()));
				down.add(e.getEntity().getUniqueId());
				hurt.remove(e.getEntity().getUniqueId());
				BukkitRunnable runnable = new BukkitRunnable() {
					long downTimer = config.knockdownTimer;
					
					@Override
					public void run() {
						Player player = Bukkit.getPlayer(uuid);
						if (player != null && player.isOnline()) {
							if (pickingUp.contains(player.getUniqueId())) {
								player.sendTitle(Main.convert(config.messages.downReviving.replaceAll("%timer%", String.valueOf(downTimer))), Main.convert(config.messages.downRevivingSubtitle));
								player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Main.convert(config.messages.downRevivingAction)));
							} else {
								player.sendTitle(Main.convert(config.messages.downWaiting.replaceAll("%timer%", String.valueOf(downTimer))), Main.convert(config.messages.downWaitingSubtitle));
								player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Main.convert(config.messages.downWaitingAction)));
							}
							
							if (downTimer <= 0) {
								player.setHealth(0.0);
								cancel();
							}
						}
						downTimer--;
					}
				};
				
				runnable.runTaskTimer(main, 20, 20);
				
				downCountdowns.put(player.getUniqueId(), runnable);
			}
		}
	}
	
	@EventHandler
	public void onRegen(EntityRegainHealthEvent e) {
		if (e.getRegainReason() != EntityRegainHealthEvent.RegainReason.CUSTOM) {
			if (hurt.contains(e.getEntity().getUniqueId()) || down.contains(e.getEntity().getUniqueId())) {
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onRevived(PlayerRevivedEvent e) {
		downCountdowns.get(e.getRevived().getUniqueId()).cancel();
		downCountdowns.remove(e.getRevived().getUniqueId());
		down.remove(e.getRevived().getUniqueId());
		hurt.add(e.getRevived().getUniqueId());
		pickingUp.remove(e.getRevived().getUniqueId());
		e.getRevived().teleport(e.getRevived().getLocation().add(0.0, 1.0, 0.0));
		e.getRevived().setHealth(config.hurtHealth);
	}
	
	@EventHandler
	public void onSurgeryStart(SurgeryStartedEvent e) {
		e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, config.surgeryTimer * 2 * 20, 1));
		e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, config.surgeryTimer * 20, 1));
		e.getPlayer().teleport(getFirstBlock(e.getPlayer().getLocation()));
	}
	
	@EventHandler
	public void onSurgeryEnd(SurgeryFinishedEvent e) {
		operatingOn.remove(e.getPlayer().getUniqueId());
		hurt.remove(e.getPlayer().getUniqueId());
		recovering.add(e.getPlayer().getUniqueId());
		e.getPlayer().removePotionEffect(PotionEffectType.BLINDNESS);
		e.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
	}
	
	@EventHandler
	public void onRecoveryStart(RecoveryStartedEvent e) {
		e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, config.recoveryTimer * 2 * 20, 1));
		UUID uuid = e.getPlayer().getUniqueId();
		new BukkitRunnable() {
			long timer = config.recoveryTimer;
			
			@Override
			public void run() {
				Player player = Bukkit.getPlayer(uuid);
				if (player != null && player.isOnline()) {
					e.getPlayer().sendTitle(Main.convert(config.messages.downRecovering.replaceAll("%timer%", String.valueOf(timer))), Main.convert(config.messages.downRecoveringSubtitle));
					e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Main.convert(config.messages.downRecoveringAction)));
					if (timer <= 0) {
						Bukkit.getPluginManager().callEvent(new RecoveryFinishedEvent(e.getPlayer()));
						cancel();
					}
				}
				timer--;
			}
		}.runTaskTimer(main, 20, 20);
	}
	
	@EventHandler
	public void onRecoveryEnd(RecoveryFinishedEvent e) {
		recovering.remove(e.getPlayer().getUniqueId());
		e.getPlayer().removePotionEffect(PotionEffectType.CONFUSION);
		e.getPlayer().teleport(e.getPlayer().getLocation().add(0.0, 1.0, 0.0));
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		if (hurt.contains(e.getEntity().getUniqueId()) || down.contains(e.getEntity().getUniqueId()) || downCountdowns.containsKey(e.getEntity().getUniqueId()) || emtCountdowns.containsKey(e.getEntity().getUniqueId())) {
			hurt.remove(e.getEntity().getUniqueId());
			down.remove(e.getEntity().getUniqueId());
			recovering.remove(e.getEntity().getUniqueId());
			pickingUp.remove(e.getEntity().getUniqueId());
			operatingOn.remove(e.getEntity().getUniqueId());
			
			downCountdowns.get(e.getEntity().getUniqueId()).cancel();
			downCountdowns.remove(e.getEntity().getUniqueId());
			emtCountdowns.get(e.getEntity().getUniqueId()).cancel();
			emtCountdowns.remove(e.getEntity().getUniqueId());
		}
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		if (e.getTo().getBlockY() != e.getFrom().getBlockY() || e.getTo().getBlockX() != e.getFrom().getBlockX() || e.getTo().getBlockZ() != e.getFrom().getBlockZ()) {
			if (down.contains(e.getPlayer().getUniqueId()) || recovering.contains(e.getPlayer().getUniqueId()) || operatingOn.contains(e.getPlayer().getUniqueId()))
				e.getPlayer().teleport(e.getFrom().getBlock().getLocation().add(0.5, 0.0, 0.5));
		}
	}
	
	private Location getFirstBlock(Location location) {
		Location temp = location.add(0.0, 1.0, 0.0);
		do {
			temp = temp.add(0.0, -1.0, 0.0);
		} while (temp.getBlock().isPassable() && temp.getBlockY() > 0);
		
		return temp.add(0.5, 0.0, 0.5);
	}
}