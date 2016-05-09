package io.pandelum.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import io.pandelum.PandelumCore;

public class FakeAfkCommand extends BukkitCommand
{
	private PandelumCore core;
	
	public FakeAfkCommand(String name, PandelumCore plugin)
	{
		super(name);
		this.core         = plugin;
		this.description  = "Fake AFK Command";
		this.usageMessage = "/fafk";
		this.setPermission("pandelum.staff");
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
			if (label.equalsIgnoreCase("fafk"))
			{
				Player player = (Player) sender;

				if (player.hasPermission("pandelum.staff"))
				{
					if (core.playerProfileContainsKey(player.getUniqueId(), "fafk"))
					{
						core.playerProfileDelete(player.getUniqueId(), "fafk");
						Bukkit.broadcastMessage(ChatColor.GRAY + "* " + ChatColor.RESET + player.getDisplayName() + ChatColor.RESET + ChatColor.GRAY + " is no longer AFK.");
					}
					else
					{
						core.playerProfileSet(player.getUniqueId(), "fafk", "true");
						Bukkit.broadcastMessage(ChatColor.GRAY + "* " + ChatColor.RESET + player.getDisplayName() + ChatColor.RESET + ChatColor.GRAY + " is now AFK.");
					}
				}
			}
		}
		
		return true;
	}
}