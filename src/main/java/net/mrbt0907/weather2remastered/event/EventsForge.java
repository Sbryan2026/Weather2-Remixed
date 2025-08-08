package net.mrbt0907.weather2remastered.event;

import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

public class EventsForge
{
	@SubscribeEvent
	public static void onServerTick(ServerTickEvent event)
	{
		if (event.phase == Phase.START)
			ServerTickHandler.onTickInGame();
	}
	@SubscribeEvent
	public static void serverStarting(FMLServerStartingEvent event)
	{
		//event.registerServerCommand(new CommandWeather2());
		System.out.println("THE SERVER IS STARTING. REFRESH THE DIMENSION RULES.");
		net.mrbt0907.weather2remastered.api.WeatherAPI.refreshDimensionRules();
	}
	
}