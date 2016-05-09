package io.pandelum;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class SaveWorldsRunnable extends BukkitRunnable
{
    private final PandelumCore core;

    public SaveWorldsRunnable (PandelumCore plugin) 
    {
        this.core = plugin;
    }

    @Override
    public void run()
    {
    	if (Bukkit.getWorlds().get(0).isAutoSave())
    	{
    		core.getLogger().info("Starting automatic world save");
    		core.saveAllWorlds();
    		core.getLogger().info("Finished automatic world save");
        }
    	else
    	{
    		core.getLogger().info("Automatic world saving is disabled, type /save-on to enable");
        }
    }

}