package dev.reeve.emt.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class SurgeryFinishedEvent extends PlayerEvent {
	public static HandlerList handlers = new HandlerList();
	
	public SurgeryFinishedEvent(Player who) {
		super(who);
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
