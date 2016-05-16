package io.pandelum.commands;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import io.pandelum.PandelumCore;


public class FirstnameCommand extends BukkitCommand
{
	private PandelumCore core;
	
	@SuppressWarnings("serial")
	public FirstnameCommand(String name, PandelumCore plugin)
	{
		super(name);
		this.core         = plugin;
		this.description  = "Allows staff to change their first names";
		this.usageMessage = "/firstname <name>";
		this.setPermission("pandelum.staff");
		
		this.setAliases(new ArrayList<String>() {{
			add("fname");
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
			
			if (sender.hasPermission("pandelum.staff") || sender.hasPermission("pandelum.commands.firstname") || sender.isOp())
			{
				if (args.length == 1)
				{
					// Set the firstname in the player profile
			        core.playerProfileSet(player.getUniqueId(), "firstname", args[0]);
			        sender.sendMessage("Your first name has been set to " + args[0]);
			        return true;
				}
				else if (args.length == 2)
				{
					if (sender.isOp())
					{
						Player target = Bukkit.getPlayer(args[0]);
						if (target != null)
						{
							core.playerProfileSet(target.getUniqueId(), "firstname", args[1]);
					        sender.sendMessage("The first name for " + target.getName() + " has been set to " + args[1]);
							return true;
						}
						else
						{
					        sender.sendMessage("I could not find a player matching that name");
						}
					}
					else
					{
						return false;
					}
				}
			}
		}
		
		return false;
	}
}