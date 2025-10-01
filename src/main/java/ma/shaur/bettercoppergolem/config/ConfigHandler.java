package ma.shaur.bettercoppergolem.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import ma.shaur.bettercoppergolem.BetterCopperGolem;
import net.fabricmc.loader.api.FabricLoader;

public class ConfigHandler 
{
	private static final File configFile = FabricLoader.getInstance().getConfigDir().resolve(BetterCopperGolem.MOD_ID + "-config.json").toFile();
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private static Config config = null;

	public static Config getConfig() 
	{
		return config;
	}
	
	public static void init() 
	{
		if(config != null) return;
		try 
		{
			loadConfig();
		} 
		catch (JsonSyntaxException | JsonIOException | IOException e) 
		{
			BetterCopperGolem.LOGGER.error("Exception loading config", e);
			if(config == null) config = new Config(); 
		}
	}
	
	public static void loadConfig() throws JsonSyntaxException, JsonIOException, IOException
	{
		if(!configFile.exists()) saveConfig();
		else config = gson.fromJson(new FileReader(configFile), Config.class);
	}
	
	public static void saveConfig() throws JsonIOException, IOException
	{
		if(config == null) config = new Config(); 
		configFile.getParentFile().mkdirs();
		try(FileWriter writer = new FileWriter(configFile))
		{
			gson.toJson(config, writer);
		    writer.flush();
		    writer.close();
		}
	}
}
