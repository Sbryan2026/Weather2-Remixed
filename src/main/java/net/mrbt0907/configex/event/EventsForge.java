package net.mrbt0907.configex.event;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.Loading;
import net.minecraftforge.fml.config.ModConfig.Reloading;
import net.mrbt0907.configex.ConfigManager;

public class EventsForge
{
	@SubscribeEvent
	public static void onConfigLoaded(Loading event)
	{
		ModConfig config = event.getConfig();
		ConfigManager.onConfigLoaded(config.getSpec());
	}
	
	@SubscribeEvent
	public static void onConfigReloaded(Reloading event)
	{
		ModConfig config = event.getConfig();
		ConfigManager.onConfigLoaded(config.getSpec());
	}
}