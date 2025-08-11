package net.mrbt0907.weather2remastered.api.weather;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.mrbt0907.weather2remastered.Weather2Remastered;
import net.mrbt0907.weather2remastered.config.ConfigSimulation;
import net.mrbt0907.weather2remastered.util.Maths;
import net.mrbt0907.weather2remastered.util.Maths.Vec3;

public class AbstractWeatherManager
{
	public long ticks;
	protected String dimension;
	protected World world;
	public AbstractWindManager windManager;
	//0 = none, 1 = usual max overcast
	public float cloudIntensity = 1F;
	protected HashSet<Long> listWeatherBlockDamageDeflector = new HashSet<>();
	
	//fronts
	protected AbstractFrontObject globalFront;
	protected Map<UUID, AbstractFrontObject> fronts = new LinkedHashMap<UUID, AbstractFrontObject>();
	protected Map<UUID, AbstractWeatherObject> systems = new LinkedHashMap<UUID, AbstractWeatherObject>();

	public AbstractWeatherManager(@Nonnull World world)
	{
		if (world == null)
			Weather2Remastered.fatal(new NullPointerException("World provided was null when constructing weather manager"));
		else
			Weather2Remastered.debug("Creating new WeatherSystem for dimension: " + world.dimension().location().toString());
		this.world = world;
		dimension = world.dimension().location().toString();
		windManager = new AbstractWindManager(this);
	}

	public void reset(boolean fullReset)
	{
		//Remove weather objects
		fronts.forEach((uuid, front) -> front.reset());
		fronts.clear();
		globalFront = null;
		
		//Remove volcanos
		//volcanoObjects.forEach(vo -> vo.reset());
		//volcanoObjects.clear();
		//volcanoUUIDS.clear();
		if (fullReset)
		{
		//	Reset wind manager
			windManager.reset();
		}
	}

	public void tick()
	{
		if (world != null)
		{
			fronts.forEach((uuid, front) -> {if (!front.isDead) {front.tick();}});
//			systems.forEach((uuid, system) -> system.tick()); // Why were we ticking systems here??? FrontObjects and StormObjects are the only thing that should do this!
			//volcanoObjects.forEach(vo -> vo.tick());
			windManager.tick();
			ticks++;
		}
	}

	public AbstractStormObject createStorm(double posX, double posZ, int layer, int stage, Map<String, Boolean> flags)
	{
		return globalFront.createStorm(posX, posZ, stage, flags);
	}

	public AbstractStormObject createNaturalStorm(int layer)
	{
		return globalFront.createNaturalStorm();
	}

	/**Creates a weather object in the world*/
	public AbstractWeatherObject createWeatherObject(Class<? extends AbstractWeatherObject> clazz)
	{
		return globalFront.createWeatherObject(clazz);
	}

	public AbstractWeatherObject addWeatherObject(AbstractWeatherObject wo)
	{
		if (!systems.containsKey(wo.getUUID()))
			systems.put(wo.getUUID(), wo);
		return wo;
	}

	public void removeWeatherObject(UUID uuid)
	{
		AbstractWeatherObject system = systems.get(uuid);
		
		if (system != null)
		{
			systems.remove(uuid);
			Weather2Remastered.debug("Weather " + uuid + " was removed from manager in " + world.dimension().location().toString());
		}
		else
			Weather2Remastered.error("Manager for dimension " + world.dimension().location().toString() + " tried to remove a non-existent weather object with uuid " + uuid);
	}

	public AbstractFrontObject createNaturalFront(int layer, PlayerEntity player)
	{
		AbstractFrontObject front = createFront(layer, player.getX() + Maths.random(-ConfigSimulation.max_storm_spawning_distance, ConfigSimulation.max_storm_spawning_distance), player.getZ() + Maths.random(-ConfigSimulation.max_storm_spawning_distance, ConfigSimulation.max_storm_spawning_distance));
		fronts.put(front.getUUID(), front);
		return front;
	}

	public AbstractFrontObject createFront(int layer, double posX, double posZ)
	{
		AbstractFrontObject front = new AbstractFrontObject(this, new Vec3(posX, 0, posZ), layer);
		return front;
	}

	public void removeFront(AbstractFrontObject front)
	{
		removeFront(front.getUUID());
	}
	
	public void removeFront(UUID uuid)
	{
		AbstractFrontObject front = fronts.get(uuid);
		
		if (front != null)
		{
			front.reset();
			fronts.remove(uuid);
			Weather2Remastered.debug("Front " + uuid.toString() + " was removed from manager in: " + world.dimension());
		}
		else
			Weather2Remastered.error("Front " + uuid.toString() + " does not exist on this side. Skipping...");
	}

	public AbstractFrontObject getFront(@Nonnull UUID uuid)
	{
		return fronts.get(uuid);
	}

	public List<AbstractFrontObject> getFronts()
	{
		return new ArrayList<AbstractFrontObject>(fronts.values());
	}

	public List<AbstractFrontObject> getFronts(int layer)
	{
		List<AbstractFrontObject> fronts = new ArrayList<AbstractFrontObject>();
		for(AbstractFrontObject front : this.fronts.values())
			if (front.layer == layer)
				fronts.add(front);
		return fronts;
	}

	public Map<Integer, List<AbstractFrontObject>> getLayeredFronts()
	{
		Map<Integer, List<AbstractFrontObject>> fronts = new HashMap<Integer, List<AbstractFrontObject>>();
		
		for(AbstractFrontObject front : this.fronts.values())
			fronts.get(front.layer).add(front);
		return fronts;
	}

	public boolean hasDownfall()
	{
		List<AbstractWeatherObject> systems = getWeatherObjects();
		
		for(AbstractWeatherObject system : systems)
			if (system instanceof IWeatherRain && ((IWeatherRain)system).hasDownfall())
				return true;
		return false;
	}
	
	public boolean hasDownfall(BlockPos pos)
	{
		return hasDownfall(new Vec3(pos));
	}

	/**
	 * TODO: Heavy on the processing, consider caching the result by location for 20 ticks
	 *
	 * @param parPos
	 * @return
	 */
	public boolean hasDownfall(Vec3 pos)
	{
		List<AbstractWeatherObject> systems = getWeatherObjects();
		
		for(AbstractWeatherObject system : systems)
			if (system instanceof IWeatherRain && ((IWeatherRain)system).hasDownfall(pos))
				return true;
		
		return false;
	}

	public AbstractWeatherObject getClosestWeather(Vec3 pos, double distance)
	{
		return getClosestWeather(pos, distance, 0, Integer.MAX_VALUE, WeatherEnum.Type.BLIZZARD, WeatherEnum.Type.CLOUD, WeatherEnum.Type.SANDSTORM);
	}
	
	public AbstractWeatherObject getClosestWeather(Vec3 pos, double distance, int minStage, int maxStage, WeatherEnum.Type... excludedTypes)
	{
		Map<AbstractWeatherObject, Integer> list = getWeatherSystems(pos, distance, minStage, maxStage, excludedTypes);
		AbstractWeatherObject result = null;
		double dist = Double.MAX_VALUE, curDist;
		
		for (AbstractWeatherObject weather : list.keySet())
		{
			curDist = weather.pos.distanceSq(pos) - weather.size;
			if (curDist < dist)
			{
				dist = curDist;
				result = weather;
			}
		}
		
		return result;
	}
	
	public AbstractWeatherObject getWorstWeather(Vec3 pos, double distance)
	{
		return getWorstWeather(pos, distance, 0, Integer.MAX_VALUE, WeatherEnum.Type.BLIZZARD, WeatherEnum.Type.CLOUD, WeatherEnum.Type.SANDSTORM);
	}
	
	public AbstractWeatherObject getWorstWeather(Vec3 pos, double distance, int minStage, int maxStage, WeatherEnum.Type... excludedTypes)
	{
		Map<AbstractWeatherObject, Integer> list = getWeatherSystems(pos, distance, minStage, maxStage, excludedTypes);
		AbstractWeatherObject result = null;
		int stage = -1, curStage;
		
		for (Entry<AbstractWeatherObject, Integer> entry : list.entrySet())
		{
			curStage = entry.getValue();
			if (curStage > stage)
			{
				stage = curStage;
				result = entry.getKey();
			}
		}
		
		return result;
	}

	public Map<AbstractWeatherObject, Integer> getWeatherSystems(Vec3 pos, double distance)
	{
		return getWeatherSystems(pos, distance, 0, Integer.MAX_VALUE, WeatherEnum.Type.BLIZZARD, WeatherEnum.Type.CLOUD, WeatherEnum.Type.SANDSTORM);
	}
	
	public Map<AbstractWeatherObject, Integer> getWeatherSystems(Vec3 pos, double distance, int minStage, int maxStage, WeatherEnum.Type... excludedTypes)
	{
		boolean truth;
		int stage;
		Map<AbstractWeatherObject, Integer> list = new HashMap<AbstractWeatherObject, Integer>();
		List<AbstractWeatherObject> curList = new ArrayList<AbstractWeatherObject>(systems.values());
		
		for (AbstractWeatherObject weather : curList)
		{
			truth = true;
			stage = 0;
			
			for (WeatherEnum.Type type : excludedTypes)
			{
				if (weather.type.equals(type))
				{
					truth = false;
					break;
				}
			}
			
			if (truth && weather.pos.distanceSq(pos) - weather.size < distance)
			{
				if (weather instanceof IWeatherStaged)
					stage = ((IWeatherStaged)weather).getStage();
				
				if (stage >= minStage && stage <= maxStage)
					list.put(weather, stage);
			}
		}
		
		return list;
	}

	public List<AbstractWeatherObject> getWeatherObjects()
	{
		return new ArrayList<AbstractWeatherObject>(systems.values());
	}
	
	public HashSet<Long> getListWeatherBlockDamageDeflector()
	{
		return listWeatherBlockDamageDeflector;
	}

	public void setListWeatherBlockDamageDeflector(HashSet<Long> listWeatherBlockDamageDeflector)
	{
		this.listWeatherBlockDamageDeflector = listWeatherBlockDamageDeflector;
	}

	public World getWorld()
	{
		return world;
	}

	public AbstractFrontObject getGlobalFront()
	{
		return globalFront;
	}

	public String getDimension()
	{
		return dimension;
	}

	public boolean isClient()
	{
		return getWorld().isClientSide();
	}
}