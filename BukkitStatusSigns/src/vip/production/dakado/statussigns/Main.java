package vip.production.dakado.statussigns;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class Main extends JavaPlugin implements Listener {

	
	private static Main plugin;
	 public static BukkitTask pocitani = null;
	
	 
	 
	 
	public void onEnable() {
		String verze = getServer().getVersion(); //version checking
		System.out.println("Version: " + verze); 
        if (!verze.contains("1.7.2")) {
        	System.out.println("*************************************************");
        	System.out.println("[StatusSigns] Plugin disabled due to version missmatch!");
        	System.out.println("[StatusSigns] Please update your server to 1.7.2 or download new version of Status Signs!");
        	System.out.println("*************************************************");
        	getServer().getPluginManager().disablePlugin(this);
        }
		
		
		
		
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		this.saveDefaultConfig();
		plugin = this;
		
		
	   	 pocitani = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
	 		  @Override
	   		  public void run() {
	 			  int pl = 0;
		for(Player p : Bukkit.getOnlinePlayers()){
			pl++;
		}
		
		
		int id = getConfig().getInt("Data.id");
		
		for (int i = 0; i < id; i++) {
			boolean check = getConfig().getBoolean("Signs." + i + ".created");
			if (check == true) {
			int x = getConfig().getInt("Signs." + i + ".x");
			int y = getConfig().getInt("Signs." + i + ".y");
			int z = getConfig().getInt("Signs." + i + ".z");
			World w = Bukkit.getWorld( getConfig().getString("Signs." + i + ".world"));
			
			Location loc = new Location(Bukkit.getWorld( getConfig().getString("Signs." + i + ".world")), x, y, z);
        Block block = w.getBlockAt(loc);
        Sign thesign = (Sign) block.getState();
        thesign.setLine(2, pl + "/" + getConfig().getString("Format.slots"));
        thesign.update();
	 		  }
		}
	 		  }
	    	},20L, getConfig().getInt("Settings.refresh")*20L);
	   	 
	   	 
		
	
	}
	public void onDisable() {}
	
	
	
	
	
	
	
    @EventHandler
    public void onSignChange(SignChangeEvent e) {

        String[] lines = e.getLines();
        if(lines[0].equalsIgnoreCase("statussign")) {
        	if(hasPerm(e.getPlayer(), "create")) {
            e.setLine(0, ChatColor.AQUA + "" + ChatColor.BOLD + getConfig().getString("Format.first"));
            e.setLine(1, ChatColor.WHITE + getConfig().getString("Format.second"));
            e.setLine(2, ChatColor.GREEN + "?/" + getConfig().getString("Format.slots"));
            e.setLine(3, ChatColor.GREEN + getConfig().getString("Format.fourth"));
            
            Block block = e.getBlock();
            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();
            String world = block.getWorld().getName();
            int id = getConfig().getInt("Data.id");
            getConfig().set("Signs." + id + ".x", x);
            getConfig().set("Signs." + id + ".y", y);
            getConfig().set("Signs." + id + ".z", z);
            getConfig().set("Signs." + id + ".world", world);
            getConfig().set("Signs." + id + ".created", true);
            int idplus = id + 1;
            getConfig().set("Data.id", idplus);
            saveConfig();
            
            e.getPlayer().sendMessage(ChatColor.BLUE + "[" + ChatColor.GREEN + "StatusSigns" + ChatColor.BLUE + "] " + ChatColor.GREEN + "Status sign sucesfully created.");
        	} else {
        		e.setCancelled(true);
        		e.getPlayer().sendMessage(ChatColor.BLUE + "[" + ChatColor.GREEN + "StatusSigns" + ChatColor.BLUE + "] " + ChatColor.RED + "YOu do not have permission for this.");
        	}
        }
    }
	
	
    
    
	public Boolean hasPerm(CommandSender p, String permission) {
		if (!(p instanceof Player)) return true;
		if (p.hasPermission("statussigns." + permission)) return true;
		return false;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		CommandSender p = sender;
		FileConfiguration config = getConfig();
		
		
		if (cmd.getName().equalsIgnoreCase("ssigns")) {
			
			if (args.length == 1) {
				if (args[0].equals("remove")) {
					//tady dat do configu nejaky removemod na true a potom v block break eventu to bude naslouchat jen když je ten removemod true,
					//tak to musí projet všechny cedulky a pokud se souøadnice budou shodovat setne to created na false
					p.sendMessage(ChatColor.BLUE + "[" + ChatColor.GREEN + "StatusSigns" + ChatColor.BLUE + "] " + ChatColor.GREEN + "Remove mode has been enabled.");
					getConfig().set("Data.removemode", true);
					saveConfig();
				}
				if (args[0].equals("reload")) {
					reloadConfig();
					sender.sendMessage(ChatColor.BLUE + "[" + ChatColor.GREEN + "StatusSigns" + ChatColor.BLUE + "] " + ChatColor.GREEN + "Status Signs config has been reloaded.");
				}
			}
			
		}
		return true;
	}
    
	
	
	
	
	
	
	
	
    
	@EventHandler
	public void onBreak(BlockBreakEvent e) {
		if (e.getBlock().getType() == Material.SIGN || e.getBlock().getType() == Material.SIGN_POST) {
			if (getConfig().getBoolean("Data.removemode") == true) {
				Player p = e.getPlayer();
				if(hasPerm(p, "remove")) {
					int id = getConfig().getInt("Data.id");
					int X = e.getBlock().getX();
					int Y = e.getBlock().getY();
					int Z = e.getBlock().getZ();
					for (int i = 0; i < id; i++) {
						boolean check = getConfig().getBoolean("Signs." + i + ".created");
						if (check = true) {
						int x = getConfig().getInt("Signs." + i + ".x");
						int y = getConfig().getInt("Signs." + i + ".y");
						int z = getConfig().getInt("Signs." + i + ".z");
						if (X == x && Y == y && Z == z) {
							getConfig().set("Signs." + i + ".created", false);
							getConfig().set("Data.removemode", false);
							saveConfig();
							p.sendMessage(ChatColor.BLUE + "[" + ChatColor.GREEN + "StatusSigns" + ChatColor.BLUE + "] " + ChatColor.GOLD + "StatusSign has been removed.");
							p.sendMessage(ChatColor.BLUE + "[" + ChatColor.GREEN + "StatusSigns" + ChatColor.BLUE + "] " + ChatColor.RED + "Remove mode has been disabled.");
						}
						}
					}
				}
			}
		}
	}
	
	
	
	
    
    
    
    
	
}
