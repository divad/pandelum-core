package io.pandelum.commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import io.pandelum.PandelumCore;


public class GlobalCommand extends BukkitCommand
{
	private PandelumCore core;
	
	@SuppressWarnings("serial")
	public GlobalCommand(String name, PandelumCore plugin)
	{
		super(name);
		this.core         = plugin;
		this.description  = "Save all worlds";
		this.usageMessage = "/global";
		
		this.setAliases(new ArrayList<String>() {{
			add("g");
		}});
	}
	
	@Override
	public boolean execute(CommandSender sender, String label, String[] args)
	{
		if (!(sender instanceof Player))
		{
			sender.sendMessage("This command can only be run by a player.");
		}
		else
		{
			if (args.length > 0)
			{
				core.chatRoomSendMessage("global", (Player) sender, String.join(" ", args));
			}
			else
			{
				return false;
			}
		}
		
		return true;
	}
}