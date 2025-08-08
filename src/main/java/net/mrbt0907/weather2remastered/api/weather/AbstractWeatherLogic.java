package net.mrbt0907.weather2remastered.api.weather;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;

public abstract class AbstractWeatherLogic
{
	public String weatherID;
	protected final World world;
	protected final AbstractWeatherObject weather;
	
	/**Used to make weather objects do things like create tornados, throw mobs around, etc.*/
	public AbstractWeatherLogic(AbstractWeatherObject weather)
	{
		world = weather.manager.getWorld();
		this.weather = weather;
	}
	
	//-- Registry Methods --\\
	public abstract String[] getWeatherIds();
	public abstract String[] getWeatherFlags();
	
	//-- Abstract Methods --\\
	public abstract void readNBT(CompoundNBT nbt);
	public abstract void writeNBT(CompoundNBT nbt);
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
