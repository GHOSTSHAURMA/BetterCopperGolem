package ma.shaur.bettercoppergolem.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import ma.shaur.bettercoppergolem.BetterCopperGolem;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.option.SimpleOption.TooltipFactory;
import net.minecraft.text.Text;

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
			BetterCopperGolem.LOGGER.error("Error loading config", e);
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

	public static SimpleOption<?>[] asOptions() 
	{
		ArrayList<SimpleOption<?>> options = new ArrayList<>();
		
		// It's honestly faster to manually type that make something smarter. I don't have to explain myself to you
		options.add(SimpleOption.ofBoolean(translationKey("shulker_and_bundle_sorting"), tooltipFactory(config._shulkerAndBundleSorting), config.shulkerAndBundleSorting, (b) -> config.shulkerAndBundleSorting = b));

		options.add(SimpleOption.ofBoolean(translationKey("ignore_color"), tooltipFactory(config._ignoreColor, config._ignoreColorTrue, config._ignoreColorFalse), config.ignoreColor, (b) -> config.ignoreColor = b));

		options.add(SimpleOption.ofBoolean(translationKey("allow_individual_items_match_container_contents"), tooltipFactory(config._allowIndividualItemsMatchContainerContents, config._allowIndividualItemsMatchContainerContentsTrue, config._allowIndividualItemsMatchContainerContentsFalse), config.allowIndividualItemsMatchContainerContents, (b) -> config.allowIndividualItemsMatchContainerContents = b));

		options.add(SimpleOption.ofBoolean(translationKey("ignore_color"), tooltipFactory(config._ignoreColor, config._ignoreColorTrue, config._ignoreColorFalse), config.ignoreColor, (b) -> config.ignoreColor = b));
		
		return options.toArray(SimpleOption[]::new);
	}
	
	private static TooltipFactory<Boolean> tooltipFactory(String... lines) 
	{
		StringBuffer sb = new StringBuffer();
		sb.append(Stream.of(lines).collect(Collectors.joining("\n")));
		return (b) -> Tooltip.of(Text.of(sb.toString()));
	}

	private static String translationKey(String name)
	{
		return "option.bettercoppergolem." + name;
	}
}
