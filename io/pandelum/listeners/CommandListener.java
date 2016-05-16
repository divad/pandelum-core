package io.pandelum.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import io.pandelum.PandelumCore;

public class CommandListener implements Listener 
{
	private PandelumCore core;
	
	public CommandListener(PandelumCore plugin)
	{
		this.core = plugin;
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPreprocess(PlayerCommandPreprocessEvent event)
	{
		String command = event.getMessage().substring(1);
		
		String[] blockedCommands = {"motd", "essentials:motd", "essentials:ping", "essentials:nick",
			"bukkit:pl", "bukkit:plugins", "plugins", "pl",
			"rl", "reload", "bukkit:rl", "bukkit:reload",
			"ver", "version", "about", "bukkit:ver", "bukkit:version", "bukkit:about",
			"op", "deop"
		};
		
		for (String cmd : blockedCommands)
		{
			if (command.startsWith(cmd))
			{
				event.setCancelled(true);
				core.getLogger().info("Disabled command sent by " + event.getPlayer().getName());
				break;
			}
		}
	}
}
