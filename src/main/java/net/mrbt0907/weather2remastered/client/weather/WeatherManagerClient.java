package net.mrbt0907.weather2remastered.client.weather;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.mrbt0907.weather2remastered.api.weather.AbstractStormObject;
import net.mrbt0907.weather2remastered.api.weather.AbstractWeatherManager;
import net.mrbt0907.weather2remastered.api.weather.AbstractWeatherObject;
import net.mrbt0907.weather2remastered.api.weather.IWeatherRain;
import net.mrbt0907.weather2remastered.api.weather.IWeatherStaged;
import net.mrbt0907.weather2remastered.client.NewSceneEnhancer;
import net.mrbt0907.weather2remastered.config.ConfigClient;
import net.mrbt0907.weather2remastered.util.Maths;
import net.mrbt0907.weather2remastered.util.Maths.Vec3;
@OnlyIn(Dist.CLIENT)
public class WeatherManagerClient extends AbstractWeatherManager {

	private static final net.minecraft.client.Minecraft MC = net.minecraft.client.Minecraft.getInstance(); 
	//data for client, stormfronts synced from server
	//new for 1.10.2, replaces world.weatherEffects use
	public List<Particle> effectedParticles = new ArrayList<Particle>();
	public List<Particle> weatherParticles = new ArrayList<Particle>();
	public static AbstractStormObject closestStormCached;
	public int weatherID = 0;
	public int weatherRainTime = 0;
	private int particleLimit = 0;
	public WeatherManagerClient(World world) {
		super(world);
	}
	@Override
	public World getWorld()
	{
		return MC.level;
	}

	@Override
	public void tick()
	{
		super.tick();
		
		Particle particle;
		for (int i = 0; i < weatherParticles.size(); i++)
		{
			particle = weatherParticles.get(i);
			
			if (!particle.isAlive())
			{
				weatherParticles.remove(i);
				i--;
			}
		}
	}
	public void tickRender(float partialTick)
	{
		if (world != null)
			getWeatherObjects().forEach(wo -> wo.tickRender(partialTick));
	}
	
	@Override
	public void reset(boolean fullReset)
	{
		super.reset(fullReset);
		effectedParticles.clear();
		closestStormCached = null;
	}
	public float getRainTargetValue(Vec3 position)
	{
		float rainTarget = -Float.MAX_VALUE, rain;
		List<AbstractWeatherObject> systems = new ArrayList<AbstractWeatherObject>(this.systems.values());
		
		for (AbstractWeatherObject system : systems)
		{
			if (system instanceof IWeatherRain)
			{
				rain = ((IWeatherRain)system).getDownfall(position) - IWeatherRain.MINIMUM_DRIZZLE;
				if (rain > rainTarget)
					rainTarget = rain;
			}
		}
		return Maths.clamp(rainTarget / IWeatherRain.MINIMUM_HEAVY_RAIN, 0.0F, 1.0F);
	}
	public float getOvercastTargetValue(Vec3 position)
	{
		float overcastTarget = -Float.MAX_VALUE, overcast;
		List<AbstractWeatherObject> systems = new ArrayList<AbstractWeatherObject>(this.systems.values());
		
		for (AbstractWeatherObject system : systems)
		{
			if (system instanceof IWeatherStaged)
			{
				float distance = (float) Maths.distanceSq(position.posX, position.posZ, system.pos.posX, system.pos.posZ);
				float distanceMult = 1.0F - Math.min(Math.max(distance - system.size, 0.0F) / (system.size * 0.25F), 1.0F);
				float stageMult = 0.25F + Math.min(((IWeatherStaged)system).getStage() * 0.25F, 0.5F) + (system instanceof AbstractStormObject && ((AbstractStormObject)system).isViolent ? 0.25F : 0.0F);
				overcast = stageMult * distanceMult;
				if (overcast > overcastTarget)
					overcastTarget = overcast;
			}
		}
		
		return Maths.clamp(overcastTarget, 0.0F, 1.0F);
	}
	
	public void addWeatherParticle(Particle particle)
	{
		weatherParticles.add(particle);
	}
	
	public void addEffectedParticle(Particle particle)
	{
		effectedParticles.add(particle);
	}

	public int getParticleCount()
	{
		return weatherParticles.size();
	}
	
	public void refreshParticleLimit()
	{
		int systems = 0;
		final ClientPlayerEntity player = MC.player;
		if (player == null) return;
		for (AbstractWeatherObject system : this.systems.values())
			if (system.pos.distanceSq(player.getX(), system.pos.posY, player.getZ()) < NewSceneEnhancer.instance().renderDistance)
				systems++;
		
		if (systems == 0) systems = 1;
		
		particleLimit = ConfigClient.max_particles > 0 ? ConfigClient.max_particles / systems : Integer.MAX_VALUE;
		this.systems.forEach((uuid, system) ->
		{
			if (system instanceof AbstractStormObject && ((AbstractStormObject)system).particleRenderer != null)
				((AbstractStormObject)system).particleRenderer.refreshParticleLimit();
		});
	}
	
	public int getParticleLimit()
	{
		return particleLimit;
	}
}
