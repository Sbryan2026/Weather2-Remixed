package net.mrbt0907.weather2.weather;

import net.minecraft.world.World;
import net.mrbt0907.weather2.Weather2;

public abstract class NewWeatherManager
{
	public long ticks;
	public final World world;
	public final int dimension;
	/*
	Weather Manager manages weather objects within a dimension
	
	
	
	
	
	
	*/
	public NewWeatherManager(World world)
	{
		if (world == null)
			Weather2.fatal("WeatherSystem recieved a null world upon creation");
		else
			Weather2.debug("Creating new WeatherSystem for dimension #" + world.provider.getDimension());
		
		this.world = world;
		
		dimension = world.provider.getDimension();
	}

	public void tick()
	{

	}

	public void reset()
	{

	}
}


