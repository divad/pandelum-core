package io.pandelum;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;

import io.pandelum.listeners.*;
import io.pandelum.commands.*;

public class PandelumCore extends JavaPlugin
{
	/* The version of this plugin */
	public final String version = "18";
	
	/* The connection to the redis server */
	private Jedis redis = new Jedis("localhost");

	/* The mapping of players (UUID) to their permission attachment */
	private HashMap<UUID,PermissionAttachment> permissions = new HashMap<UUID,PermissionAttachment>();

	/* the chatrooms we currently support */
	/* note that chat* functions dont enforce the argument exists within this list */
	String[] chatrooms = {"rp", "sc"};
	
	
	@Override
	public void onEnable()
	{
		/* Log the startup time of the plugin */
		redis.set("pandelum:core:startup", String.valueOf(System.currentTimeMillis() / 1000L));
		
		/* Load listeners */
		PluginManager pluginManager = getServer().getPluginManager();
		pluginManager.registerEvents(new ChatListener(this),this);
		pluginManager.registerEvents(new PlayerListener(redis,getLogger(),this), this);

		/* Load commands */
		CommandMap commandMap;
		try
		{
			final Field serverCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			serverCommandMap.setAccessible(true);
			commandMap = (CommandMap) serverCommandMap.get(Bukkit.getServer());
			
			/* Register each command */
			commandMap.register("sc",   new StaffChatCommand("sc",this));
			commandMap.register("rp",   new RpCommand("rp",this));
			commandMap.register("fafk", new FakeAfkCommand("fafk",this));
			commandMap.register("sync", new SyncCommand("sync",this));
			
			// ADD: /global command so people can speak in global even when in a chat room
			// ADD: /op and /deop so they are blocked from players
			// ADD: fakeop
			// ADD: block butcher
			// ADD: switch server command
			// ADD: /spy
			// ADD: /me
			// ADD: permissions reload command for a player
			// ADD: permissions modification commands
			// ADD: kick/ban/etc. commands
			
		}
		catch (IllegalAccessException exception)
		{
			getLogger().info("ERROR: Encountered IllegalAccessException when attempting to register commands");
		}
		catch (NoSuchFieldException exception)
		{
			getLogger().info("ERROR: Encountered NoSuchFieldException when attempting to register commands");			
		}
		
		// Delete the keys for each chat room
		// Because they could have stale members
		for (String room  : chatrooms)
		{
			redis.del("chat:room:" + room);
		}
		
		// Just in case players are already online, for each of them check
		// if they're in a chat room and if they are, add them to the chat list
		for (Player player : Bukkit.getOnlinePlayers())
		{
			String room = this.playerProfileGet(player.getUniqueId(), "chatroom");
			if (room != null)
			{
				if (!(room.equals("global")))
				{
					this.chatRoomAddPlayer(room, player.getUniqueId());
				}
			}
		}
		
		/* Schedule a task to run every 5 minutes to save all loaded worlds */
		SaveWorldsRunnable saver = new SaveWorldsRunnable(this);
		saver.runTaskTimer(this, 6000, 6000);
		
		/* Log that initialisation is complete */
		getLogger().info("Pandelum Core v" + version + " started up");
	}
	
	/* Save worlds automatically */
	
	public Map<String,String> getPlayerProfile(UUID player_uuid)
	{
		String key = "player:" + player_uuid.toString() + ":profile";
		return redis.hgetAll(key);
	}
	
	public void playerProfileSet(UUID player_uuid, String key, String value)
	{
		String profile_key = "player:" + player_uuid.toString() + ":profile";		
		redis.hset(profile_key, key, value);
	}
	
	public void playerProfileDelete(UUID player_uuid, String key)
	{
		String profile_key = "player:" + player_uuid.toString() + ":profile";		
		redis.hdel(profile_key, key);
	}
	
	public String playerProfileGet(UUID player_uuid, String key)
	{
		String profile_key = "player:" + player_uuid.toString() + ":profile";		
		return redis.hget(profile_key, key);	
	}
	
	public void chatRoomSendMessage(String room, Player sender, String message)
	{
		if (room.equals("sc"))
		{
			// send the message to all players online who have the staff chat permission
			for (Player player : Bukkit.getOnlinePlayers())
			{
				if (player.hasPermission("pandelum.chat.sc"))
					player.sendMessage("[sc] " + sender.getDisplayName() + ": " + message);
			}
		}
		else
		{
		
			Set<String> members = redis.smembers("chat:room:" + room);
			if (members != null)
			{
				Iterator<String> it = members.iterator();
			    while(it.hasNext())
			    {
			        String playerUUID = it.next();
			        Player player = Bukkit.getPlayer(UUID.fromString(playerUUID));
			        if (player != null)
			        {
			        	player.sendMessage("[" + room + "] " + sender.getName() + ": " + message);
			        }
			        else
			        {
			        	// This player is not online right now so we'll remove them 
			        	// from the list 'cos they shouldn't be there
			        	redis.srem("chat:room:" + room, playerUUID);
			        }
			    }
			}
		}
		
	    getLogger().info("CHAT [" + room + "] " + sender.getName() + ": " + message);
		
	}
	
	public void chatRoomAddPlayer(String room, UUID player_uuid)
	{
		// You can't add people to staff chat - members are based on perms
		if (!(room.equals("sc")))
			redis.sadd("chat:room:" + room, player_uuid.toString());
	}
	
	public void chatRoomRemovePlayer(String room, UUID player_uuid)
	{
		// You can't remove people from staff chat - members are based on perms		
		if (!(room.equals("sc")))
			redis.srem("chat:room:" + room, player_uuid.toString());
	}
	
	public void chatSwitchRoom(Player player, String room)
	{
		UUID player_uuid = player.getUniqueId();
		
		// Get existing chat room
		String chatroom = this.playerProfileGet(player_uuid, "chatroom");
		
		if (chatroom == null)
		{
			chatroom = "global";
		}
		
		// If they're not already in the room...
		if (!(chatroom.equals(room)))
		{
			// Switch the user to the new room
			this.playerProfileSet(player_uuid, "chatroom", room);
			this.chatRoomAddPlayer(room, player_uuid);
			player.sendMessage("You are now chatting in " + room);
			
			// If they were in a previous room that wasn't global then remove
			// them from that room 'cos they just switched
			if (!(chatroom.equals("global")))
			{
				this.chatRoomRemovePlayer(chatroom,player_uuid);
			}
		}
		else
		{
			// The player was already in the room so they probably
			// are toggling i.e. leaving the room and returning to global
			this.chatRoomRemovePlayer(room, player_uuid);
			this.playerProfileSet(player_uuid, "chatroom", "global");
			player.sendMessage("You are no longer chatting in " + room);
		}		
		
	}	
	
	public boolean playerProfileContainsKey(UUID player_uuid, String key)
	{
		Map<String,String> profile = this.getPlayerProfile(player_uuid);
		
		if (profile != null)
		{
			if (profile.containsKey("fafk"))
			{
				return true;
			}
		}		
		
		return false;
	}		
	
	public void deletePlayerPermissions(Player player)
	{
		// Remove the permissions attachment for this player
		if (permissions.containsKey(player.getUniqueId()))
		{
			player.removeAttachment(permissions.get(player.getUniqueId()));
			permissions.remove(player.getUniqueId());
		}
	}
	
	public void rebuildPlayerPermissions(Player player, World world)
	{
		UUID player_uuid = player.getUniqueId();

		// Reset the old permissions
		deletePlayerPermissions(player);
		
		// Create a new attachment
		PermissionAttachment playerPermissions = player.addAttachment(this);
		
		// Load and grant the global perms for the player
		Set<String> globalPermissions = redis.smembers("player:" + player_uuid.toString() + ":perms");
		
		if (globalPermissions != null)
		{
			for (String str : globalPermissions)
			{
				getLogger().info("DEBUG: Setting permission " + str + " on " + player_uuid);
			    playerPermissions.setPermission(str, true);
			}
		}
				
		// Load and grant the world perms for the player (the world the player is in)
		Set<String> worldPermissions = redis.smembers("player:" + player_uuid.toString() + ":perms:" + world.toString());
		
		if (worldPermissions != null)
		{
			for (String str : worldPermissions)
			{
			    playerPermissions.setPermission(str, true);
			}
		}
		
		// TODO group permissions
		// TODO score permissions
		// TODO add 'global/default' permissions
		
		this.permissions.put(player_uuid, playerPermissions);		
	}
	
	public void saveAllWorlds()
	{
        for (World world : Bukkit.getWorlds())
        {
            world.save();
            getLogger().info("Saved world " + world.getName());
        }
	}
	

}