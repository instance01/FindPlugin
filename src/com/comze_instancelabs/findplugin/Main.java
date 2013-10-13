package com.comze_instancelabs.findplugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;


public class Main extends JavaPlugin implements Listener{
	@Override
    public void onEnable(){
		getLogger().info("Initializing FindPlugins . . .");
		//getServer().getPluginManager().registerEvents(this, this);
		
		getConfig().addDefault("config.auto_updating", true);
		
		getConfig().options().copyDefaults(true);
		this.saveConfig();
		
		try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :(
        }
		
		if(getConfig().getBoolean("config.auto_updating")){
        	Updater updater = new Updater(this, 63238, this.getFile(), Updater.UpdateType.DEFAULT, false);
        }
    }

    @Override
    public void onDisable() {
    	getLogger().info("Disabling.");
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
    	if(cmd.getName().equalsIgnoreCase("find") || cmd.getName().equalsIgnoreCase("f") || cmd.getName().equalsIgnoreCase("findplugin")){
    		if(sender.hasPermission("findplugin.use")){
	    		if(args.length > 0){
	    			String c = args[0];
	    			if(c.startsWith("/"))
	    				c = c.substring(1);
	    			
					try {
						Player p = (Player)sender;
						//sender.sendMessage(Integer.toString(walk("plugins\\", c, p).size()));
						//scans through the plugins folder for jars
						scan("plugins/", c, p);
						sender.sendMessage("§2Done searching.");
					} catch (IOException e) {
						sender.sendMessage("§4An error has occured!");
						e.printStackTrace();
					}
	    		}
	    		return true;	
    		}else{
    			sender.sendMessage("§3[FindPluginByCmd] §4You don't have permission!");
    		}
    	}
    	return false;
    }
    
    
    
    public List<String> scan(String path, String arg, Player p) throws IOException{
    	String a = arg;
        File root = new File(path);
        File[] list = root.listFiles();

        List<String> t = new ArrayList<String>();
        List<String> jars = new ArrayList<String>();
        
        if (list == null) return null;

        for ( File f : list ) {
            if ( f.isDirectory() ) {
            	//skip if directory
            }else{
            	//for each jar file found in the path provided:
            	if(f.getName().endsWith(".jar")){
	     		    String newp = path + f.getName();
	     		    JarFile jarFile = new JarFile(newp);
					JarEntry entry = jarFile.getJarEntry("plugin.yml");
					InputStream input = jarFile.getInputStream(entry);
					//process the inputstream of selected plugin.yml
					t = process(input, arg, f.getName(), p);
					jarFile.close();
            	}
            }
        }
        return t;
    }


    
	public List<String> process(InputStream input, String a, String name, Player p) throws IOException {
		List<String> l = new ArrayList<String>();
		InputStreamReader isr = new InputStreamReader(input);
		BufferedReader reader = new BufferedReader(isr);
		String line;
		boolean found = false;
		int linenumber_ = 0;
		int linenumber = 0;
		while ((line = reader.readLine()) != null) {
			linenumber_ += 1;
			if(!found){
				linenumber = linenumber_;
			}
			if(line.contains(a)){
				found = true;
			}
		}
		if(found){
			p.sendMessage("§3[FindPluginByCmd] §2" + name + ": plugin.yml line " + linenumber);
			l.add(name);
		}
		reader.close();
		return l;
	}
}


