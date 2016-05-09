package io.pandelum.listeners;

import io.pandelum.PandelumCore;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener 
{
	private PandelumCore core;
	private Logger log;
	
	public ChatListener(PandelumCore plugin)
	{
		this.core = plugin;
		log       = plugin.getLogger();
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onChat(AsyncPlayerChatEvent event)
	{
		// Cancel the standard event as we want to manage chat ourselves
		event.setCancelled(true);
		
		/* The event contains who sent the message and the message itself */
		Player sender  = event.getPlayer();
		String message = event.getMessage();
		
		/* Prevent caps lock spam */
		/* TODO: staff can do as much as they like */
		int upperChars = 0;
		for (int i = 0; i < message.length(); i++)
		{
			if (Character.isUpperCase(message.charAt(i)))
			{
				upperChars++;
			}
		}
		
        if ((upperChars / message.length() * 100) >= 60)
        {
        	sender.sendMessage("Your message contained too many uppercase characters");
        	return;
        }
        
		/* TODO Donors coin! */
		
		/* Messages starting with a full stop are where the user wishes to start
		 * the message with a forward slash but cannot (as it would be a command)
		 * so we remove the full stop to make it look like it did */
		if (message.startsWith("./"))
		{
			message = message.substring(1);
		}
		
		/* We don't want people to colour their own messages */
		message = ChatColor.stripColor(message);

		boolean inChatRoom = false;
		
		/* TODO Handle chat rooms */
		String chatroom = core.playerProfileGet(sender.getUniqueId(), "chatroom");
		if (chatroom != null)
		{
			if (!chatroom.equals(("global")))
			{
				inChatRoom = true;
				core.chatRoomSendMessage(chatroom, sender, message);
			}
		}
		
		
		/* TODO: handle coloured display name based on rank/PR */
		
		/* TODO MAYBE - custom json stuff? */
		
		/* TODO: multi-server chat */
		
		/* Send the message in global */
		if (!inChatRoom)
		{
			for (Player player : Bukkit.getOnlinePlayers())
	            player.sendMessage(sender.getDisplayName() + ": " + message);
			log.info("CHAT " + sender.getName() + ": " + message);
		}
	}
}
