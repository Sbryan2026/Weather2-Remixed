package net.mrbt0907.weather2remastered.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.mrbt0907.weather2remastered.api.weather.AbstractWeatherManager;
import net.mrbt0907.weather2remastered.config.ConfigWind;
import net.mrbt0907.weather2remastered.util.Maths.Vec3;
import net.mrbt0907.weather2remastered.util.WeatherUtilEntity;

public class WindReader
{
	/**Gets the current wind speed of the current dimension*/
	public static float getWindSpeed(World world, Vec3 pos)
	{
		AbstractWeatherManager manager = WeatherAPI.getManager(world);
		if (manager != null)
			return manager.windManager.getWindSpeed(pos);
		return 0.0F;
	}
	
	/**Gets the current wind direction of the current dimension*/
	public static float getWindAngle(World world, Vec3 pos)
	{
		AbstractWeatherManager manager = WeatherAPI.getManager(world);
		if (manager != null)
			return manager.windManager.getWindAngle(pos);
		else
			return 0.0F;
	}
	
	/**Forgot what this does*/
	public static Vec3 getWindVectors(World world, Entity entity, float maxSpeed)
	{
		return getWindVectors(world, new Vec3(entity.getX(), entity.getY(), entity.getZ()), new Vec3(entity.getDeltaMovement().x, entity.getDeltaMovement().y, entity.getDeltaMovement().z), (float) (WeatherUtilEntity.getWeight(entity) * 0.1F * (entity instanceof PlayerEntity ? ConfigWind.windPlayerWeightMult : entity instanceof LivingEntity ? ConfigWind.windEntityWeightMult : 1.0F) * (entity.isInWater() ? ConfigWind.windSwimmingWeightMult : 1.0F)), 5.0F);
	}
	
	/**Forgot what this does*/
	public static Vec3 getWindVectors(World world, Vec3 pos, Vec3 motion, float weight, float maxSpeed)
	{
		AbstractWeatherManager manager = WeatherAPI.getManager(world);
		if (manager != null)
			return manager.windManager.getWindVectors(pos, motion, weight, 0.05F, maxSpeed);
		else
			return motion;
	}
}