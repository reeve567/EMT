package dev.reeve.emt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Main extends JavaPlugin {
	
	private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private final File configFile = new File(getDataFolder(), "config.json");
	private Config config;
	
	@Override
	public void onEnable() {
		configLoad();
		
		HealthListener healthListener = new HealthListener(this, config);
		Bukkit.getPluginManager().registerEvents(healthListener, this);
		Bukkit.getPluginManager().registerEvents(new InteractionListener(this, config, healthListener), this);
	}
	
	private void configLoad() {
		if (!getDataFolder().exists())
			getDataFolder().mkdir();
		
		if (configFile.exists()) {
			try {
				config = gson.fromJson(new FileReader(configFile), Config.class);
				
				if (config == null) {
					config = new Config();
					
					FileWriter writer = new FileWriter(configFile);
					writer.write(gson.toJson(new Config()));
					writer.close();
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			configSave();
		}
	}
	
	private void configSave() {
		if (config == null)
			config = new Config();
		try {
			FileWriter writer = new FileWriter(configFile);
			writer.write(gson.toJson(config));
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String convert(String str) {
		return ChatColor.translateAlternateColorCodes('&', str);
	}
}
