package net.mrbt0907.weather2remastered.event;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogColors;
import net.minecraftforge.client.event.EntityViewRenderEvent.RenderFogEvent;
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
    public static void worldRender(RenderWorldLastEvent event)
    {
		if (ConfigMisc.toaster_pc_mode) return;
		ClientTickHandler.checkClientWeather();
		ClientTickHandler.weatherManager.tickRender(event.getPartialTicks());
		//FoliageRenderer.radialRange = ConfigFoliage.shader_range;
    }
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onFogRender(RenderFogEvent event)
	{
		if (ConfigMisc.toaster_pc_mode) return;
		NewSceneEnhancer scene = NewSceneEnhancer.instance();
		GlStateManager._fogStart(0);
		GlStateManager._fogEnd(scene.renderDistance - Math.min(scene.renderDistance * scene.fogMult * 3.3F, scene.renderDistance - 1.0F));
		GlStateManager._fogDensity(scene.fogMult);
	}
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
    public void onFogColors(FogColors event)
	{
		if (ConfigMisc.toaster_pc_mode) return;
		NewSceneEnhancer scene = NewSceneEnhancer.instance();
		
        if (scene.shouldChangeFogColor())
        {
			//backup original fog colors that are actively being adjusted based on time of day
        	float red = event.getRed(), green = event.getGreen(), blue = event.getBlue();
        	
        	if (scene.seesWeatherObject() && scene.fogMult > 0.0025F)
        	{
        		if (scene.fogRed < 0.0F)
            		scene.fogRed = red;
            	if (scene.fogGreen < 0.0F)
            		scene.fogGreen = green;
            	if (scene.fogBlue < 0.0F)
            		scene.fogBlue = blue;
        	}
        	else
        	{
        		if (scene.fogRedTarget >= 0.0F && scene.fogRedTarget != red)
            		scene.fogRedTarget = red;
            	if (scene.fogGreenTarget >= 0.0F && scene.fogGreenTarget != green)
            		scene.fogGreenTarget = green;
            	if (scene.fogBlueTarget >= 0.0F && scene.fogBlueTarget != blue)
            		scene.fogBlueTarget = blue;
            	
            	if (scene.fogRed != -1.0F && scene.fogRed == red)
            		scene.fogRed = -1.0F;
				if (scene.fogGreen != -1.0F && scene.fogGreen == green)
					scene.fogGreen = -1.0F;
				if (scene.fogBlue != -1.0F && scene.fogBlue == blue)
					scene.fogBlue = -1.0F;
        	}
        	
        	if (scene.fogRed >= 0.0F && scene.fogGreen >= 0.0F && scene.fogBlue >= 0.0F)
        	{
	        	event.setRed(scene.fogRed);
	        	event.setGreen(scene.fogGreen);
	        	event.setBlue(scene.fogBlue);
        	}
        }
	}
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void onRenderTick(TickEvent.RenderTickEvent event)
	{
		NewSceneEnhancer.instance().tickRender(event);	
	}
}