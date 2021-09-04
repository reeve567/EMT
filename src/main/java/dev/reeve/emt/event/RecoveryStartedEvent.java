package dev.reeve.emt.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class RecoveryStartedEvent extends PlayerEvent {
	public static HandlerList handlers = new HandlerList();
	public RecoveryStartedEvent(Player who) {
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
