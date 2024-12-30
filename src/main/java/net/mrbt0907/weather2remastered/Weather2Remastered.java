package net.mrbt0907.weather2remastered;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.mrbt0907.weather2remastered.registry.BlockRegistry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("weather2remastered")
public class Weather2Remastered
{
	public static final String MODID = "weather2remaster";
	public static final ItemGroup TAB = new ItemGroup(MODID) {@Override public ItemStack makeIcon(){return new ItemStack(BlockRegistry.tornado_sensor);}};
	private static final Logger LOG = LogManager.getLogger();
	
	public Weather2Remastered()
	{
		FMLJavaModLoadingContext context = FMLJavaModLoadingContext.get();
		IEventBus MOD_BUS = context.getModEventBus();
		MOD_BUS.addListener(this::init);
		MOD_BUS.addListener(this::initClient);
		MOD_BUS.addListener(this::postInit);
		MinecraftForge.EVENT_BUS.addListener(this::onServerStarting);
	
		CommonProxy.preInit();
		DistExecutor.safeRunWhenOn(Dist.CLIENT,() -> ClientProxy::preInit);
	}

	private void init(final FMLCommonSetupEvent event)
	{
		CommonProxy.init();
	}
	
	private void initClient(final FMLClientSetupEvent event)
	{
		ClientProxy.init();
	}
	
	private void postInit (final FMLLoadCompleteEvent event)
	{
		CommonProxy.postInit();
		DistExecutor.safeRunWhenOn(Dist.CLIENT,() -> ClientProxy::postInit);
	}
	
	public void onServerStarting(FMLServerStartingEvent event)
	{
		
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