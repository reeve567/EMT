package dev.reeve.emt;

import dev.reeve.emt.event.PlayerRevivedEvent;
import dev.reeve.emt.event.RecoveryStartedEvent;
import dev.reeve.emt.event.SurgeryFinishedEvent;
import dev.reeve.emt.event.SurgeryStartedEvent;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class InteractionListener implements Listener {
	
	private final Main main;
	private final Config config;
	private final HealthListener healthListener;
	
	public InteractionListener(Main main, Config config, HealthListener healthListener) {
		this.main = main;
		this.config = config;
		this.healthListener = healthListener;
	}
	
	@EventHandler
	public void onInteract(PlayerInteractAtEntityEvent e) {
		if (e.getRightClicked() instanceof Player) {
			if (e.getPlayer().hasPermission("medic.emt") && healthListener.down.contains(e.getRightClicked().getUniqueId()) && !healthListener.pickingUp.contains(e.getRightClicked().getUniqueId())) {
				healthListener.pickingUp.add(e.getRightClicked().getUniqueId());
				BukkitRunnable runnable = new BukkitRunnable() {
					long timer = config.emtTimer;
					
					@Override
					public void run() {
						e.getPlayer().sendTitle(Main.convert(config.messages.emtReviving.replaceAll("%timer%", String.valueOf(timer))), Main.convert(config.messages.emtRevivingSubtitle));
						e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Main.convert(config.messages.emtRevivingAction)));
						if (timer <= 0) {
							Bukkit.getPluginManager().callEvent(new PlayerRevivedEvent(e.getPlayer(), (Player) e.getRightClicked()));
							cancel();
						}
						timer--;
					}
				};
				runnable.runTaskTimer(main, 20, 20);
				healthListener.emtCountdowns.put(e.getRightClicked().getUniqueId(), runnable);
			}
			if (e.getPlayer().hasPermission("medic.surgeon") && healthListener.hurt.contains(e.getRightClicked().getUniqueId()) && !healthListener.operatingOn.contains(e.getRightClicked().getUniqueId())) {
				healthListener.operatingOn.add(e.getRightClicked().getUniqueId());
				Bukkit.getServer().getPluginManager().callEvent(new SurgeryStartedEvent((Player) e.getRightClicked(), e.getPlayer()));
				BukkitRunnable runnable = new BukkitRunnable() {
					long surgeryTimer = config.surgeryTimer;
					
					@Override
					public void run() {
						e.getPlayer().sendTitle(ChatColor.translateAlternateColorCodes('&', config.messages.surgeonOperating.replaceAll("%timer%", String.valueOf(surgeryTimer))), Main.convert(config.messages.surgeonOperatingSubtitle));
						e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(config.messages.surgeonOperatingAction));
						((Player) e.getRightClicked()).sendTitle(ChatColor.translateAlternateColorCodes('&', config.messages.downOperating.replaceAll("%timer%", String.valueOf(surgeryTimer))), Main.convert(config.messages.downOperatingSubtitle));
						((Player) e.getRightClicked()).spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Main.convert(config.messages.downOperatingAction)));
						if (surgeryTimer <= 0) {
							Bukkit.getPluginManager().callEvent(new SurgeryFinishedEvent((Player) e.getRightClicked()));
							Bukkit.getPluginManager().callEvent(new RecoveryStartedEvent((Player) e.getRightClicked()));
							cancel();
						}
						surgeryTimer--;
					}
				};
				runnable.runTaskTimer(main, 20, 20);
				
				healthListener.downCountdowns.put(e.getRightClicked().getUniqueId(), runnable);
				healthListener.emtCountdowns.put(e.getRightClicked().getUniqueId(), runnable);
			}
		}
	}
}
