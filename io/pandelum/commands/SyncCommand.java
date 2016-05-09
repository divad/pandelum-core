package io.pandelum.commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

import io.pandelum.PandelumCore;


public class SyncCommand extends BukkitCommand
{
	private PandelumCore core;
	
	@SuppressWarnings("serial")
	public SyncCommand(String name, PandelumCore plugin)
	{
		super(name);
		this.core         = plugin;
		this.description  = "Save all worlds";
		this.usageMessage = "/sync";
		this.setPermission("pandelum.commands.sync");
		
		this.setAliases(new ArrayList<String>() {{
			add("save");
		}});
	}
	
	@Override
	public boolean execute(CommandSender sender, String label, String[] args)
	{
		if (sender.hasPermission("pandelum.commands.sync") || sender.isOp())
		{
	        core.saveAllWorlds();
	        sender.sendMessage("All worlds saved");
	        return true;
		}
		else
		{
			return false;
		
		}
	}
}