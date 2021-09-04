package dev.reeve.emt.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class SurgeryStartedEvent extends PlayerEvent {
	public static HandlerList handlers = new HandlerList();
	private final Player surgeon;
	
	public SurgeryStartedEvent(Player patient, Player surgeon) {
		super(patient);
		this.surgeon = surgeon;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public Player getSurgeon() {
		return surgeon;
	}
}
