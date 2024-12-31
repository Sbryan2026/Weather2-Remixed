package net.mrbt0907.configex;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.mrbt0907.configex.command.CommandConfigEX;
import net.mrbt0907.configex.config.ConfigMaster;

@Mod("configex")
public class ConfigModEX {
	public static final String MODID = "configex";
	public static final String MOD = "Config Manager - Expanded";
	public static final String VERSION = "2.0";
	private static final Logger LOGGER = LogManager.getLogger();
	public static boolean enableDebug = false;
	
	public ConfigModEX() {
	    enableDebug = false;
	    FMLJavaModLoadingContext context = FMLJavaModLoadingContext.get();
		IEventBus MOD_BUS = context.getModEventBus();
		ConfigMaster.preInit();
		MOD_BUS.addListener(this::init);
		MOD_BUS.addListener(this::initClient);
		MOD_BUS.addListener(this::postInit);
		MinecraftForge.EVENT_BUS.addListener(this::onServerStarting);
	}
	
	private void init(final FMLCommonSetupEvent event)
	{
		MinecraftForge.EVENT_BUS.register(CommandConfigEX.class);
	}
	
	private void initClient(final FMLClientSetupEvent event)
	{

	}
	
	private void postInit (final FMLLoadCompleteEvent event)
	{

	}
	
	public void onServerStarting(FMLServerStartingEvent event)
	{
		LOGGER.info("HELLO from server starting, got game folder at" + getGameFolder());
	}

	public static String getGameFolder()
	{
		Minecraft mc = Minecraft.getInstance();
		if (mc.hasSingleplayerServer())
			return mc.gameDirectory.getPath() + File.separator;
		else
			return new File(".").getAbsolutePath() + File.separator;
	}
	
	public static void info(Object message)
	{
		LOGGER.info(message);
	}
	
	public static void debug(Object message)
	{
		boolean isDebug = ConfigMaster.debug_mode.get() || enableDebug;	
		if (isDebug)
			LOGGER.info("[DEBUG] " + message);
	}
	
	public static void warn(Object message)
	{
		boolean isDebug = ConfigMaster.debug_mode.get() || enableDebug;
		if (isDebug)
			LOGGER.warn(message);
	}

	public static void error(Object message)
	{
		boolean isDebug = ConfigMaster.debug_mode.get() || enableDebug;
		if (isDebug)
		{
			Throwable exception;
			
			if (message instanceof Throwable)
				exception = (Throwable) message;
			else
				exception = new Exception(String.valueOf(message));

			exception.printStackTrace();
		}
	}
	
	public static void fatal(Object message)
	{
		Error error;
		
		if (message instanceof Error)
			error = (Error) message;
		else
			error = new Error(String.valueOf(message));
		
		throw error;
	}
}