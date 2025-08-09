package net.mrbt0907.weather2remastered;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
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
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.mrbt0907.configex.ConfigManager;
import net.mrbt0907.weather2remastered.config.*;
import net.mrbt0907.weather2remastered.event.EventsForge;
import net.mrbt0907.weather2remastered.event.ServerTickHandler;
import net.mrbt0907.weather2remastered.network.PacketNBT;
import net.mrbt0907.weather2remastered.registry.BlockRegistry;
import net.mrbt0907.weather2remastered.registry.SoundRegistry;
import net.mrbt0907.weather2remastered.util.UtilPlayerData;
import net.mrbt0907.weather2remastered.weather.WeatherManagerServer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Weather2Remastered.MODID)
public class Weather2Remastered
{
	public static final String MODID = "weather2remastered";
	public static final ItemGroup TAB = new ItemGroup(MODID) {@Override public ItemStack makeIcon(){return new ItemStack(BlockRegistry.tornado_sensor);}};
	private static final Logger LOG = LogManager.getLogger();
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Weather2Remastered.MODID, "main"),
            () -> "1.0",
            s -> true,
            s -> true
        );

        private static int id = 0;
	public Weather2Remastered()
	{
		info("Starting Weather2 - Remastered...");
		IEventBus MOD_BUS = FMLJavaModLoadingContext.get().getModEventBus();
		ConfigManager.register(new ConfigClient());
		ConfigManager.register(new ConfigMisc());
		ConfigManager.register(new ConfigVolume());
		ConfigManager.register(new ConfigGraphics());
		ConfigManager.register(new ConfigFront());
		ConfigManager.register(new ConfigStorm());
		ConfigManager.register(new ConfigGrab());
		ConfigManager.register(new ConfigSeason());
		ConfigManager.register(new ConfigSimulation());
		ConfigManager.register(new ConfigWind());
		ConfigManager.register(new ConfigSand());
		ConfigManager.register(new ConfigSnow());
		ConfigManager.register(new ConfigFoliage());
	    SoundRegistry.register(MOD_BUS);
		MOD_BUS.addListener(this::init);
		MOD_BUS.addListener(this::initClient);
		MOD_BUS.addListener(this::postInit);
		MOD_BUS.addListener(this::setup);
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
	private void setup(final FMLCommonSetupEvent event) {
        CHANNEL.registerMessage(id++, PacketNBT.class, PacketNBT::encode, PacketNBT::decode, PacketNBT::handle);
        MinecraftForge.EVENT_BUS.register(EventsForge.class);
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

	public static void writeOutData(boolean unloadInstances)
	{
		//write out overworld only, because only dim with volcanos planned
		try {
			WeatherManagerServer wm = ServerTickHandler.dimensionSystems.get("minecraft:overworld");
			if (wm != null) {
				wm.writeToFile();
			}
			UtilPlayerData.writeAllPlayerNBT(unloadInstances);
			//doesnt cover all needs, client connected to server needs this called from gui close too
			//maybe dont call this from here so client connected to server doesnt override what a client wants his 'server' settings to be in his singleplayer world
			//factoring in we dont do per world settings for this
			//WeatherUtilConfig.nbtSaveDataAll();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}