package net.mrbt0907.weather2.api.weather;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.mrbt0907.weather2.weather.storm.WeatherObject;

public abstract class AbstractWeatherLogic
{
	public String weatherID;
	protected final World world;
	protected final WeatherObject weather;
	
	/**Used to make weather objects do things like create tornados, throw mobs around, etc.*/
	public AbstractWeatherLogic(WeatherObject weather)
	{
		world = weather.manager.getWorld();
		this.weather = weather;
	}
	
	//-- Registry Methods --\\
	public abstract String[] getWeatherIds();
	public abstract String[] getWeatherFlags();
	
	//-- Abstract Methods --\\
	public abstract void readNBT(NBTTagCompound nbt);
	public abstract void writeNBT(NBTTagCompound nbt);
	public abstract void tickWeather();
	
	public abstract boolean onSpawnCommand(String weatherID);
	public abstract boolean onSpawn();
	public abstract void onDespawn();
	
	public abstract boolean canSpawn();
	public abstract String getName();
	public abstract String getDisplayName();
	
	//-- Internal Methods --\\
	public void tick()
	{
		tickWeather();
	}
}