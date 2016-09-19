package Clicker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class CPS extends JavaPlugin implements Listener, CommandExecutor {

    //Making the HashMap concurrent allows it to be cleared whenever with no errors.
    ConcurrentHashMap<UUID, Integer> average;
    ConcurrentHashMap<UUID, Integer> current;

    public void onEnable() {
        average = new ConcurrentHashMap<>();
        current = new ConcurrentHashMap<>();
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("cps").setExecutor(this);
        smoothingTask();
        clearingTask();
    }
    private List<String> pl = new ArrayList<>();
    
    private void clearingTask()
    {
    	new BukkitRunnable() {
			
			@Override
			public void run() {
				pl.clear();
				
			}
		}.runTaskTimer(this, 0, 100);
    	
    	
    }
    private void smoothingTask() {
        new BukkitRunnable() {
            public void run() {
                Integer[] tempSort = new Integer[2];
                for (Map.Entry<UUID, Integer> entry : current.entrySet()) {
                    if (!average.containsKey(entry.getKey()))
                        average.put(entry.getKey(), entry.getValue());
                    int lastCps = average.get(entry.getKey());
                    tempSort[0] = lastCps;
                    tempSort[1] = entry.getValue();
                    Arrays.sort(tempSort);
                    int median;
                    if (tempSort.length % 2 == 0)
                        median = (tempSort[tempSort.length / 2] + tempSort[tempSort.length / 2 - 1]) / 2;
                    else
                        median = tempSort[tempSort.length / 2];
                    
                    if(median > 10)
                    {
                    	if(pl.contains(Bukkit.getPlayer(entry.getKey()).getName()))
                    	{
                    		return;
                    	}
                    	else
                    	{
                    		for(Player all : Bukkit.getOnlinePlayers())
                        	{
                        		if(all.hasPermission("CPS.Alert"))
                        		{
                        			all.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "!" + ChatColor.GRAY + "] "+ ChatColor.DARK_RED + Bukkit.getPlayer(entry.getKey()).getName() + ChatColor.GRAY + " has high CPS " + ChatColor.GRAY + "[" + ChatColor.RED + median + ChatColor.GRAY +"]");
                        			pl.add(Bukkit.getPlayer(entry.getKey()).getName());
                        		}
                        	}
                    	}
                    	
                    }
                    average.put(entry.getKey(), median);
                    current.put(entry.getKey(), 0);
                }
            }
        }.runTaskTimer(this, 0L, 20L);
    }

    public void clearLoggedList() {
        current.clear();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        //if (e.get() == EquipmentSlot.OFF_HAND) return;
    	
        if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_AIR)
            if (current.containsKey(e.getPlayer().getUniqueId()))
                current.put(e.getPlayer().getUniqueId(), current.get(e.getPlayer().getUniqueId()) + 1);
            else
                current.put(e.getPlayer().getUniqueId(), 1);
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {
        if (cs instanceof Player) {
            Player p = (Player) cs;
            if (cmd.getLabel().equalsIgnoreCase("cps")) {
                if (average.containsKey(p.getUniqueId())) {
                    p.sendMessage(ChatColor.RED + "Your CPS: " + average.get(p.getUniqueId()));
                } else {
                    p.sendMessage(ChatColor.RED + "Your CPS: " + 0);
                }
            }
        }
        return true;
    }
}