package dev.reeve.emt.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerRevivedEvent extends PlayerEvent {
	public static final HandlerList handlers = new HandlerList();
	private final Player revived;
	
	public PlayerRevivedEvent(Player emt, Player revived) {
		super(emt);
		this.revived = revived;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	public Player getRevived() {
		return revived;
	}
}
