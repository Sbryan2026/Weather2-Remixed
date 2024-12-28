package net.mrbt0907.weather2.api.weather;

import net.minecraft.world.World;
import net.mrbt0907.weather2.api.WeatherAPI;
import net.mrbt0907.weather2.weather.WeatherManager;

public abstract class AbstractManagerLogic
{
	protected World world;
	protected WeatherManager manager;
	
	public AbstractManagerLogic(WeatherManager manager)
	{
		world = manager.getWorld();
		this.manager = manager;
	}
	
	//-- Abstract Methods --\\
	public abstract void tickClient();
	public abstract void tickServer();
	
	public abstract void onLoad();
	public abstract void onSave();
	public abstract void onNetworkRecieve();
	
	//-- Helper Methods --\\
	public void spawn()
	{
		//WeatherObject weather = null;
		
	}
	public void despawn()
	{
		
	}
	public void despawnAll()
	{
		
	}
	public void load()
	{
		
	}
	public void save()
	{
		
	}
	
	//-- Internal Methods --\\
	public void tick()
	{
		if (world.isRemote)
			tickClient();
		else
			tickServer();
		
		if (WeatherAPI.getWeatherLogicId() != null)
			manager.getWeatherObjects().forEach(weather ->
			{
				if (weather.weatherLogic != null)
					weather.weatherLogic.tick();
			});
	}
}
