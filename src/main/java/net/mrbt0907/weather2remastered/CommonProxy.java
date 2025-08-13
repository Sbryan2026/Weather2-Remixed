package net.mrbt0907.weather2remastered;

import net.mrbt0907.weather2remastered.api.WeatherAPI;
import net.mrbt0907.weather2remastered.registry.StormNames;

public class CommonProxy
{
	public static void preInit()
	{
		StormNames.refreshNameList();
		//EntityRegistry.init();
	}
	
	public static void init()
	{
		
	}
	
	public static void postInit()
	{
		WeatherAPI.refreshGrabRules();
		WeatherAPI.refreshStages();
	}
}