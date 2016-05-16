package io.pandelum.commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

import io.pandelum.PandelumCore;


public class RpListCommand extends BukkitCommand
{
	private PandelumCore core;
	
	public RpListCommand(String name, PandelumCore plugin)
	{
		super(name);
		this.core         = plugin;
		this.description  = "List players in roleplay chat";
		this.usageMessage = "/rplist";
	}
	
	@Override
	public boolean execute(CommandSender sender, String label, String[] args)
	{
		ArrayList<String> members = core.chatRoomGetPlayers("rp");
		sender.sendMessage("Players in RP: " + String.join(" ", members));	
		return true;
	}
}