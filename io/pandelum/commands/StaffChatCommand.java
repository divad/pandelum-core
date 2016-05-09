package io.pandelum.commands;

import java.util.ArrayList;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import io.pandelum.PandelumCore;


public class StaffChatCommand extends BukkitCommand
{
	private PandelumCore core;
	
	@SuppressWarnings("serial")
	public StaffChatCommand(String name, PandelumCore plugin)
	{
		super(name);
		this.core         = plugin;
		this.description  = "Roleplay chat command";
		this.usageMessage = "/sc [<message>]";
		this.setPermission("pandelum.chat.sc");
		
		this.setAliases(new ArrayList<String>() {{
			add("a");
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
			if (player.hasPermission("pandelum.chat.sc"))
			{
				if (args.length > 0)
				{
					core.chatRoomSendMessage("sc", player, String.join(" ", args));
				}
				else
				{
					core.chatSwitchRoom(player, "sc");
				}
			}
		}
		
		return true;
	}
}