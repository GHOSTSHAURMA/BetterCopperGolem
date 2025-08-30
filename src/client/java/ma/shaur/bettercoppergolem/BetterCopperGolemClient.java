package ma.shaur.bettercoppergolem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;

public class BetterCopperGolemClient implements ClientModInitializer 
{
	public static final String MOD_ID = "bettercoppergolem";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	
	@Override
	public void onInitializeClient() 
	{
		LOGGER.info("Hello there (;");
	}
}