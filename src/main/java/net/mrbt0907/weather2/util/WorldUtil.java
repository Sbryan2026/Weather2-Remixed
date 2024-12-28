package net.mrbt0907.weather2.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class WorldUtil
{
	private static final FMLCommonHandler HANDLER = FMLCommonHandler.instance();
	private static String cachedWorldName;
	
	public static boolean isSinglePlayer()
	{
		return HANDLER.getMinecraftServerInstance() == null || HANDLER.getMinecraftServerInstance().isSinglePlayer();
	}
			
	public static String getSaveFolder()
	{
		return isSinglePlayer() ? net.minecraft.client.Minecraft.getMinecraft().gameDir.getPath() + File.separator + "saves" + File.separator : new File(".").getAbsolutePath() + File.separator;
	}
	
	public static String getWorldFolder()
	{
		WorldServer world = DimensionManager.getWorld(0);
		if (world != null) cachedWorldName = world.getChunkSaveLocation().getName();
		return cachedWorldName;
	}
	
	public static String getWorldFile()
	{
		String worldFolder = getWorldFolder();
		return worldFolder == null ? null : getSaveFolder() + worldFolder + File.separator;
	}
	
	public static Entity getNearestEntity(World world, double x, double y, double z, double maxDistance)
	{
		return getNearestEntity(world, x, y, z, maxDistance, null);
	}
	
	public static Entity getNearestEntity(World world, double x, double y, double z, double maxDistance, Predicate<Entity> predicate)
	{
		Entity target = null; double distance;
		List<Entity> entities = new ArrayList<Entity>(world.loadedEntityList);
		for (Entity entity : entities)
		{
			distance = entity.getDistance(x, y, z);
			if (distance < maxDistance && predicate != null ? predicate.test(entity) : true)
			{
				maxDistance = distance;
				target = entity;
			}
		}
		return target;
	}
	
	public static List<Entity> getNearestEntities(World world, double x, double y, double z, double maxDistance)
	{
		return getNearestEntities(world, x, y, z, maxDistance, null);
	}
	
	public static List<Entity> getNearestEntities(World world, double x, double y, double z, double maxDistance, Predicate<Entity> predicate)
	{
		List<Entity> targets = new ArrayList<Entity>(), entities = new ArrayList<Entity>(world.loadedEntityList);
		for (Entity entity : entities)
			if (entity.getDistance(x, y, z) < maxDistance && (predicate != null ? predicate.test(entity) : true))
				targets.add(entity);
		return targets;
	}
}