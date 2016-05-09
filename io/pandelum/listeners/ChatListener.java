package io.pandelum.listeners;

import io.pandelum.PandelumCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener 
{
	private PandelumCore core;
	
	public ChatListener(PandelumCore plugin)
	{
		this.core = plugin;
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onChat(AsyncPlayerChatEvent event)
	{
		// Cancel the standard event as we want to manage chat ourselves
		event.setCancelled(true);
		
		/* The event contains who sent the message and the message itself */
		Player sender  = event.getPlayer();
		String message = event.getMessage();

		// Assume they're chatting in global by default
		String chatroomTarget = "global";
		
		// Check if they are not chatting in global
		String chatroom = core.playerProfileGet(sender.getUniqueId(), "chatroom");
		if (chatroom != null)
		{
			if (!chatroom.equals(("global")))
			{
				chatroomTarget = chatroom;
			}
		}
		
		// Send the message
		core.chatRoomSendMessage(chatroomTarget, sender, message);
	}
}
