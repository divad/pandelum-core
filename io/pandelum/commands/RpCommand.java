package io.pandelum.commands;

import java.util.ArrayList;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import io.pandelum.PandelumCore;


public class RpCommand extends BukkitCommand
{
	private PandelumCore core;
	
	@SuppressWarnings("serial")
	public RpCommand(String name, PandelumCore plugin)
	{
		super(name);
		this.core         = plugin;
		this.description  = "Roleplay chat command";
		this.usageMessage = "/rp [<message>]";
		
		this.setAliases(new ArrayList<String>() {{
			add("roleplay");
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
			Player player = (Player) sender;
			
			if (args.length > 0)
			{
				core.chatRoomSendMessage("rp", player, String.join(" ", args));
			}
			else
			{
				core.chatSwitchRoom(player, "rp");
			}			
		}
		
		return true;
	}
}