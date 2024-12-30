package net.mrbt0907.weather2remastered;

import net.minecraft.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("weather2remastered")
public class Weather2Remastered
{
	private static final Logger LOG = LogManager.getLogger();

	public Weather2Remastered()
	{
		// Register the setup method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::preInit);
		// Register the doClientStuff method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::preInitClient);

		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);
	}

	private void preInit(final FMLCommonSetupEvent event) {}

	private void preInitClient(final FMLClientSetupEvent event) {}

	@SubscribeEvent
	public void onServerStarting(FMLServerStartingEvent event) {}

	@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
	public static class RegistryEvents {
		@SubscribeEvent
		public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent)
		{
			
		}
	}
	
	public static void info(Object message)
	{
		LOG.info(message);
	}
	
	public static void debug(Object message)
	{
		boolean isDebug = true;//ConfigMisc.debug_mode;
		if (isDebug)
			LOG.info("[DEBUG] " + message);
	}
	
	public static void warn(Object message)
	{
		boolean isDebug = true;//ConfigMisc.debug_mode;
		if (isDebug)
			LOG.warn(message);
	}

	public static void error(Object message)
	{
			Throwable exception;
			
			if (message instanceof Throwable)
				exception = (Throwable) message;
			else
				exception = new Exception(String.valueOf(message));

			exception.printStackTrace();
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