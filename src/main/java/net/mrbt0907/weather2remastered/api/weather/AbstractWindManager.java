package net.mrbt0907.weather2remastered.api.weather;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.mrbt0907.weather2remastered.Weather2Remastered;
import net.mrbt0907.weather2remastered.api.weather.WeatherEnum.Type;
import net.mrbt0907.weather2remastered.config.ConfigStorm;
import net.mrbt0907.weather2remastered.config.ConfigWind;
import net.mrbt0907.weather2remastered.util.Maths;
import net.mrbt0907.weather2remastered.util.Maths.Vec3;
import net.mrbt0907.weather2remastered.util.WeatherUtil;
import net.mrbt0907.weather2remastered.util.WeatherUtilEntity;
import net.mrbt0907.weather2remastered.util.coro.CoroEntParticle;
import net.mrbt0907.weather2remastered.weather.WeatherManagerServer;

public class AbstractWindManager
{
	public AbstractWeatherManager manager;
	
	//global
	public float windAngle = 0.0F;
	public float windSpeed = 0.0F;
	public float windAngleTarget = 0.0F;
	public float windSpeedTarget = 0.0F;
	
	//gusts
	public float windAngleGust = 0.0F;
	public float windSpeedGust = 0.0F;
	public int windTimeGust = 0;
	
	//Other
	private final Map<Vec3, AbstractWeatherObject> cache = new HashMap<Vec3, AbstractWeatherObject>();
	private long nextWindRefresh;

	public AbstractWindManager(AbstractWeatherManager parManager)
	{
		manager = parManager;
		windAngle = Maths.random(360);
		nextWindRefresh = 0L;
	}

	public void tick()
	{
		
		if (!manager.isClient())
		{
			if (!ConfigWind.enable)
			{
				windSpeed = 0.0F;
				windSpeedTarget = 0.0F;
				windSpeedGust = 0.0F;
				windTimeGust = 0;
			}
			else
			{
				if (manager.world.getGameTime() % 200L == 0L)
					cache.clear();
				
				if (manager.getWorld().getGameTime() >= nextWindRefresh)
				{
					nextWindRefresh = manager.getWorld().getGameTime() + Maths.random(ConfigWind.windRefreshMin, ConfigWind.windRefreshMax);
					windSpeedTarget = (float) Maths.random(ConfigWind.windSpeedMin, ConfigWind.windSpeedMax);
					windAngleTarget += (float) Maths.random(-ConfigWind.windAngleChangeMax, ConfigWind.windAngleChangeMax);

					windAngleTarget = windAngle % 360.0F;
				}

				tickWindChange();
				
				if (ConfigWind.enableWindAffectsEntities)
				{
					Entity[] entities = manager.getWorld().getEntitiesOfClass(Entity.class, new AxisAlignedBB(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).toArray(new Entity[0]);
					int size = entities.length;
					Entity entity;
					for (int i = 0; i < size; i++)
					{
						entity = entities[i];
						if (entity.isAlive() && entity instanceof LivingEntity && WeatherUtilEntity.isEntityOutside(entity, true))
						{
							Vec3 a = getWindVectors(new Vec3(entity.blockPosition()), new Vec3(entity.getDeltaMovement()), (float) (WeatherUtilEntity.getWeight(entity) * 8F * ConfigWind.windEntityWeightMult * (entity.isInWater() ? ConfigWind.windSwimmingWeightMult : 1.0F)), 0.05F, 5.0F);
							entity.setDeltaMovement(a.posX, a.posY, a.posZ);
						}
					}
				}
			}
		}
		else if (!WeatherUtil.isPaused())
			if (ConfigWind.enable)
				tickClient();
			else
			{
				windSpeed = 0.0F;
				windSpeedTarget = 0.0F;
				windSpeedGust = 0.0F;
				windTimeGust = 0;
			}
	}

	@OnlyIn(Dist.CLIENT)
	public void tickClient()
	{
		Minecraft mc = Minecraft.getInstance();
		tickWindChangeClient();
		
		if (manager.world.getGameTime() % 200L == 0L)
			cache.clear();
		
		if (ConfigWind.enableWindAffectsEntities && mc.player != null && WeatherUtilEntity.isEntityOutside(mc.player, true))
		{
			Vec3 a = getWindVectors(new Vec3(mc.player.blockPosition()), new Vec3(mc.player.getDeltaMovement().x, mc.player.getDeltaMovement().y, mc.player.getDeltaMovement().z), (float) (WeatherUtilEntity.getWeight(mc.player) * 8.0F * ConfigWind.windPlayerWeightMult * (mc.player.isInWater() ? ConfigWind.windSwimmingWeightMult : 1.0F)), 0.05F, 5.0F);

	    	//Weather2.debug(a.toString());
			mc.player.setDeltaMovement(a.posX, a.posY, a.posZ);
		}
	}
	
	public void tickWindChange()
	{
		//Wind angle
		if (windAngle != windAngleTarget)
		{
			float difference = windAngle + -(windAngle > 180 && windAngleTarget <= 180 ? windAngleTarget + 360.0F : windAngle <= 180 && windAngleTarget > 180 ? windAngleTarget + -360.0F : windAngleTarget);
			float change = (float) (1.95F * ConfigWind.windChangeMult);
			if (Math.abs(difference) > change)
				if (difference > 0.0F)
					windAngle -= change;
				else
					windAngle += change;
			else
				windAngle = windAngleTarget;
			
			windAngle = windAngle % 360.0F;
		}
		
		//Wind Speed
		if (windSpeed != windSpeedTarget)
		{
			float difference = windSpeed - windSpeedTarget;
			float change = (float) (0.015F * ConfigWind.windChangeMult);
			if (Math.abs(difference) > change)
				if (windSpeed > windSpeedTarget)
					windSpeed -= change;
				else
					windSpeed += change;
			else
				windSpeed = windSpeedTarget;
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public void tickWindChangeClient()
	{
		//Wind angle
		if (windAngle != windAngleTarget)
		{
			float difference = windAngle + -(windAngle > 180 && windAngleTarget <= 180 ? windAngleTarget + 360.0F : windAngle <= 180 && windAngleTarget > 180 ? windAngleTarget + -360.0F : windAngleTarget);
			float change = (float) (1.95F * ConfigWind.windChangeMult);
			if (Math.abs(difference) > change)
				if (difference > 0.0F)
					windAngle -= change;
				else
					windAngle += change;
			else
				windAngle = windAngleTarget;
			
			windAngle = windAngle % 360.0F;
		}
		
		//Wind Speed
		if (windSpeed != windSpeedTarget)
		{
			float difference = windSpeed - windSpeedTarget;
			float change = (float) (0.015F * ConfigWind.windChangeMult);
			if (Math.abs(difference) > change)
				if (windSpeed > windSpeedTarget)
					windSpeed -= change;
				else
					windSpeed += change;
			else
				windSpeed = windSpeedTarget;
		}
	}
	
	public CompoundNBT nbtSyncForClient() {
		CompoundNBT data = new CompoundNBT();
		
		data.putFloat("windSpeedTarget", windSpeedTarget);
		data.putFloat("windAngleTarget", windAngleTarget);
		data.putFloat("windSpeedGust", windSpeedGust);
		data.putFloat("windAngleGust", windAngleGust);
		data.putInt("windTimeGust", windTimeGust);
		
		return data;
	}
	
	public void nbtSyncFromServer(CompoundNBT parNBT) {
		
		windSpeedTarget = parNBT.getFloat("windSpeedTarget");
		windAngleTarget = parNBT.getFloat("windAngleTarget");
		windSpeedGust = parNBT.getFloat("windSpeedGust");
		windAngleGust = parNBT.getFloat("windAngleGust");
		windTimeGust = parNBT.getInt("windTimeGust");
	}
	
	public void syncData()
	{
		if (manager instanceof WeatherManagerServer)
			//PacketWind.update(manager.dim, this);
			Weather2Remastered.error("I've come to make an announcement. Fartsy is a lazy mf who didn't implmement PacketWind yet!!! That's right! and he even commented out the function!");
	}
	
	public void reset() {
		manager = null;
	}
	
	public void getEntityWindVectors(Object ent)
	{
		getEntityWindVectors(ent, 0.1F, 0.5F);
	}
	
	/**
	 * 
	 * To solve the problem of speed going overkill due to bad formulas
	 * 
	 * end goal: make object move at speed of wind
	 * - object has a weight that slows that adjustment
	 * - conservation of momentum
	 * 
	 * calculate force based on wind speed vs objects speed
	 * - use that force to apply to weight of object
	 * - profit
	 * 
	 * 
	 * @param ent
	 */
	public void getEntityWindVectors(Object ent, float multiplier, float maxSpeed) {

		Vec3 pos = manager.world.isClientSide ? new Vec3(Minecraft.getInstance().player.blockPosition()) : new Vec3(CoroEntParticle.getPosX(ent), CoroEntParticle.getPosY(ent), CoroEntParticle.getPosZ(ent));
		Vec3 motion = getWindVectors(pos, new Vec3(CoroEntParticle.getMotionX(ent), CoroEntParticle.getMotionY(ent), CoroEntParticle.getMotionZ(ent)), WeatherUtilEntity.getWeight(ent), multiplier, maxSpeed);
		
		CoroEntParticle.setMotionX(ent, motion.posX);
    	CoroEntParticle.setMotionZ(ent, motion.posZ);
	}
	
	public Vec3 applyWindForceImpl(Vec3 pos, Vec3 motion, float weight) {
		return getWindVectors(pos, motion, weight, 1F/20F, 0.5F);
	}
	
	/**
	 * Handle generic uses of wind force, for stuff like weather objects that arent entities or paticles
	 * 
	 * @param motion
	 * @param weight
	 * @param multiplier
	 * @param maxSpeed
	 * @return
	 */
	public Vec3 getWindVectors(Vec3 pos, Vec3 motion, float weight, float multiplier, float maxSpeed)
	{
		float windAngle = getWindAngle(pos);
		float windSpeed = getWindSpeed(pos);
		
    	float windX = (float) -Maths.fastSin(Math.toRadians(windAngle)) * windSpeed;
    	float windZ = (float) Maths.fastCos(Math.toRadians(windAngle)) * windSpeed;
    	
    	float objX = (float) motion.posX;
    	float objZ = (float) motion.posZ;
		
    	float windWeight = 1F;
    	float objWeight = weight;
    	
    	
    	//divide by zero protection
    	if (objWeight == 0.0F)
    		objWeight = 0.001F;
    	else if (objWeight < 0.0F)
    		return motion;
    	
    	//TEMP
    	//objWeight = 1F;
    	
    	float weightDiff = windWeight / objWeight;
    	
    	float vecX = (objX - windX) * weightDiff;
    	float vecZ = (objZ - windZ) * weightDiff;
    	
    	vecX *= multiplier;
    	vecZ *= multiplier;
    	
    	//copy over existing motion data
    	Vec3 newMotion = motion.copy();
        newMotion.posX = Maths.clamp(objX - vecX, -maxSpeed, maxSpeed);
        newMotion.posZ = Maths.clamp(objZ - vecZ, -maxSpeed, maxSpeed);
        return newMotion;
	}
	
	public float getWindSpeed(Vec3 pos)
	{
		if (pos == null) return manager.windManager.windSpeed;
		
        AbstractWeatherObject wo = getWeatherObject(pos);
        
        if (wo != null)
        {
        	float size = (wo.size * 0.90F);
			//return Math.max(manager.windManager.windSpeed, (float)((wo instanceof SandstormObject ? 7.5F : ((AbstractStormObject)wo).windSpeed) * Math.min((size - wo.pos.distanceSq(pos) + (wo instanceof SandstormObject ? size : ((AbstractStormObject)wo).funnelSize)) / size, 1.0F)));
        	return Math.max(manager.windManager.windSpeed, (float)(((AbstractStormObject)wo).windSpeed * Math.min((size - wo.pos.distanceSq(pos) + (((AbstractStormObject)wo).funnelSize) / size), 1.0F)));
        }
        else
        	return manager.windManager.windSpeed;
	}
	
	public float getWindAngle(Vec3 pos)
	{
		if (pos == null) return manager.windManager.windAngle;
		
        AbstractWeatherObject wo = getWeatherObject(pos);
    	if (wo != null)
    	{
            float yaw = (-((float)Maths.fastATan2(wo.posGround.posX - pos.posX, wo.posGround.posZ - pos.posZ)) * 180.0F / (float)Math.PI) + 360.0F;
			return yaw % 360.0F;
    	}
    	else
    		return manager.windManager.windAngle;
	}
	
	public Vec3 getWindForce()
	{
		float windX = (float) -Maths.fastSin(Math.toRadians(windAngle)) * windSpeed;
		float windZ = (float) Maths.fastCos(Math.toRadians(windAngle)) * windSpeed;
		return new Vec3(windX, 0, windZ);
	}

	private AbstractWeatherObject getWeatherObject(Vec3 pos)
	{
		for (Entry<Vec3, AbstractWeatherObject> entry : cache.entrySet())
		{
			if (pos.distanceSq(entry.getKey()) < 300.0D)
				return entry.getValue();
		}
		
		AbstractWeatherObject wo = manager.getClosestWeather(pos, ConfigStorm.max_storm_size + 50.0D, 0, Integer.MAX_VALUE, Type.CLOUD);
		cache.put(pos, wo);
		return wo;
	}
	
    public void readFromNBT(CompoundNBT data) {
    	windSpeedTarget = data.getFloat("windSpeedTarget");
    	windAngleTarget = data.getFloat("windAngleTarget");
        windSpeedGust = data.getFloat("windSpeedGust");
        windAngleGust = data.getFloat("windAngleGust");
        windTimeGust = data.getInt("windTimeGust");

    }

    public CompoundNBT writeToNBT(CompoundNBT data) {
        data.putFloat("windSpeedTarget", windSpeedTarget);
        data.putFloat("windAngleTarget", windAngleTarget);

        data.putFloat("windSpeedGust", windSpeedGust);
        data.putFloat("windAngleGust", windAngleGust);
        data.putInt("windTimeGust", windTimeGust);

        return data;
    }
}