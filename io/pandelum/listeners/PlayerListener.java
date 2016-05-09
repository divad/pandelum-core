package io.pandelum.listeners;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import io.pandelum.PandelumCore;

/*
 * REDIS SCHEMA
 * player:<uuid>:profile - hash of data about the player
 * player:<uuid>:perms - list (set) of global permissions the user has
 * player:<uuid>:perms:<world> - list (set) of permissions per world
 * player:<uuid>:groups - list (set) of groups the user is in
 * 
 * group:<name>:profile - hash of data about the group
 * group:<name>:perms - perms for the group
 * group:<name>:perms:<world> - perms for the group per world
 * 
 * SuperPerms is crap, super crap. Yeah, I said it! Anyway. Many permissions
 * plugins just replace it altogether but naw lets not do that. You attach
 * to a player - when they join the server - a series of permission attachments.
 * each attachment is a list of string permission essentially. You can't qualify
 * the permission i.e. you can't state it is per-world or per server or whatever.
 * 
 * As such when a player switches worlds we must remove permissions or add
 * permissions based on what perms they are meant to have per world. We sadly
 * have to store each permission object we generate too in order to remove
 * them or change them later. 
 * 
 * When perms change or we are asked to reload a players perms we delete their 
 * perms objects and rebuild entirely. In fact we do this whenever they join
 * or change world.
 * 
 */

import redis.clients.jedis.Jedis;

public class PlayerListener implements Listener 
{
	private Jedis redis;
	private Logger log;
	private PandelumCore core;
	
	public PlayerListener(Jedis redisConnection, Logger serverLog, PandelumCore plugin)
	{
		this.redis  = redisConnection;
		this.log    = serverLog;
		this.core   = plugin;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(final PlayerJoinEvent event)
	{
		Player player    = event.getPlayer();
		UUID player_uuid = player.getUniqueId();
		
		// Check to see if we have seen this player before
		String key = "player:" + player_uuid.toString() + ":profile";
		if (!(redis.exists(key)))
		{
			/*
			 * uuid is the account UUID from mojang
			 * name is their current account name from mojang
			 * btime is the 'birth' time, the time they were first ever seen on the server
			 * jtime is the last join time
			 * ipaddr is their last known IP address 
			 * nick is the nickname we have chosen on the server
			 * firstname is  their real first name
			 * score is their creative score or whatever
			 * faf is if they are friends/family or not
			 * donations is the ammount of donations they have given to the server
			 * chatroom is the room chat messages for this user should be sent to
			 * 
			 * MAYBE
			 * spy
			 * token
			 * votes
			 */

			redis.hset(key, "uuid",       player_uuid.toString());
			redis.hset(key, "name",       player.getName());
			redis.hset(key, "btime",      String.valueOf(System.currentTimeMillis() / 1000L));
			redis.hset(key, "jtime",      String.valueOf(System.currentTimeMillis() / 1000L));
			redis.hset(key, "ipaddr",     player.getAddress().toString());
			redis.hset(key, "score",      "0");
			redis.hset(key, "dontations", "0");
			redis.hset(key, "chatroom",   "global");
			
			log.info("Created new profile for " + player.getName() + " (UUID " + player_uuid.toString() + ")");
			event.setJoinMessage("Please welcome " + player.getName() + " to the server!");
		}
		else
		{
			redis.hset(key, "name",       player.getName());
			redis.hset(key, "jtime",      String.valueOf(System.currentTimeMillis() / 1000L));
			redis.hset(key, "ipaddr",     player.getAddress().toString());
			log.info("Updated profile for " + player.getName() + " (UUID " + player_uuid.toString() + ")");
			
			event.setJoinMessage("Welcome back " + player.getName() + " (CS: " + redis.hget(key, "score") + ")");
		}
		
		// Load permissions for the player
		core.rebuildPlayerPermissions(player, player.getWorld());
		
		// Now load the profile
		Map<String,String> profile = core.getPlayerProfile(player_uuid);		
		
		// If the player is configured to be in a chat room we need to add them
		// into the list and remind them that they are in that chat room
		if (profile != null)
		{
			if (profile.containsKey("chatroom"))
			{
				String chatroom = profile.get("chatroom");
				
				if (!(chatroom.equals("global")))
				{
					redis.sadd("chat:room:" + chatroom, player_uuid.toString());
					player.sendMessage("You are chatting in " + chatroom);				
				}
			}
		}
				
		
    }
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onQuit(PlayerQuitEvent event)
	{
		Player player              = event.getPlayer();
		Map<String,String> profile = core.getPlayerProfile(player.getUniqueId()); 
		
		/* TODO: set event.SetQuitMessage with custom stuff? (maybe) */
		//event.setQuitMessage("");
		
		// remove the player from the chatroom active list
		// cos they're going offline and we cant send to them anymore
		if (profile != null)
		{
			if (profile.containsKey("chatroom"))
			{
				String chatroom = profile.get("chatroom");
				
				if (!(chatroom.equals("global")))
				{
					redis.srem("chat:room:" + chatroom, player.getUniqueId().toString());
				}
			}
		}
		
		// Remove the permissions for this player 'cos they're going offline
		core.deletePlayerPermissions(player);
	}
	
	// When a player changes world we must rebuild their permissions 
	// to reflect the permissions they should have in the new world
	@EventHandler
	public void onPlayerSwitchWorld(PlayerChangedWorldEvent event)
	{
		core.rebuildPlayerPermissions(event.getPlayer(), event.getPlayer().getWorld());
	}
}
