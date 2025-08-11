package net.mrbt0907.weather2remastered.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.event.world.WorldEvent.Save;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.mrbt0907.weather2remastered.Weather2Remastered;
import net.mrbt0907.weather2remastered.client.ClientTickHandler;
import net.mrbt0907.weather2remastered.client.NewSceneEnhancer;
import net.mrbt0907.weather2remastered.config.ConfigMisc;

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
	@SubscribeEvent
	public static void onWorldSave(WorldEvent.Save event)
	{
		System.out.println("ON WORLD SAVE!!!");
		Weather2Remastered.writeOutData(false);
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
    public void worldRender(RenderWorldLastEvent event)
    {
		if (ConfigMisc.toaster_pc_mode) return;

		ClientTickHandler.checkClientWeather();
		ClientTickHandler.weatherManager.tickRender(event.getPartialTicks());

		//FoliageRenderer.radialRange = ConfigFoliage.shader_range;
    }
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onRenderTick(TickEvent.RenderTickEvent event)
	{
		NewSceneEnhancer.instance().tickRender(event);	
	}
}