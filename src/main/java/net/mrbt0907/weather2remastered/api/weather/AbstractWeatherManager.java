package net.mrbt0907.weather2remastered.api.weather;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;

import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.mrbt0907.weather2remastered.Weather2Remastered;

public abstract class AbstractWeatherManager
{
	public long ticks;
	public final World world;
	public final DimensionType dimension;
	public final boolean isClientSide;
	protected final Map<UUID, AbstractFrontObject> fronts;
	protected final Map<UUID, AbstractWeatherObject> systems;
	
	public AbstractWeatherManager(@Nonnull World world)
	{
		if (world == null)
			Weather2Remastered.fatal(new NullPointerException("World provided was null when constructing weather manager"));
		this.world = world;
		dimension = world.dimensionType();
		isClientSide = world.isClientSide;
		
		fronts = new HashMap<UUID, AbstractFrontObject>();
		systems = new HashMap<UUID, AbstractWeatherObject>();
	}
	
	public void tick()
	{
		fronts.forEach((uuid, front) -> front.tick());
		systems.forEach((uuid, system) -> system.tick());
		
		ticks++;
	}
	
	public AbstractFrontObject getFront(@Nonnull UUID uuid)
	{
		return fronts.get(uuid);
	}
	
	public AbstractWeatherObject getSystem(@Nonnull UUID uuid)
	{
		return systems.get(uuid);
	}
}