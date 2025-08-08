package net.mrbt0907.weather2remastered.api.weather;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.particle.Particle;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.mrbt0907.weather2remastered.Weather2Remastered;
import net.mrbt0907.weather2remastered.api.WeatherAPI;
import net.mrbt0907.weather2remastered.client.ClientTickHandler;
import net.mrbt0907.weather2remastered.client.weather.WeatherManagerClient;
import net.mrbt0907.weather2remastered.config.ConfigClient;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractWeatherRenderer
{
	//protected ParticleBehaviorFog particleBehaviorFog;
	/**The storm that the renderer is attached to*/
	public AbstractWeatherObject system;
	/**The amount of particles that can spawn*/
	public int particlesLeft;
	
	private final List<Particle> particles = new ArrayList<Particle>();
	private int particleLimit = 1;
	private static long delta, worldDelta;
	public static final List<String> renderDebugInfo = new ArrayList<String>();
	
	/**Used to spawn particles based on various variables in a storm. Examples are found in net.mrbt0907.weather2.client.weather.tornado*/
	public AbstractWeatherRenderer(AbstractWeatherObject system)
	{
		refreshParticleLimit();
		this.system = system;
	}
	
	public final void tick()
	{
		int attempts = 0;
		/*
		if (particleBehaviorFog == null)
			particleBehaviorFog = new ParticleBehaviorFog(system.pos.toVec3Coro());
		else if (!Minecraft.getMinecraft().isSingleplayer() || !(Minecraft.getMinecraft().currentScreen instanceof GuiIngameMenu))
				particleBehaviorFog.tickUpdateList();
		*/
		Weather2Remastered.error("ParticleBehaviorFog does not exist! FIX THIS, FARTSY!");
		Iterator<Particle> particles = this.particles.iterator();
		Particle particle;		
		while (particles.hasNext())
		{
			particle = particles.next();
			
			if (!particle.isAlive())
				particles.remove();
		}
		particles = null;
		
		particlesLeft = particleLimit - this.particles.size();
		
		if (system != null)
		{
			delta = System.nanoTime();
			while (attempts > -1)
				try
				{
					onTick(ClientTickHandler.weatherManager);
					attempts = -1;
				}
				catch(Exception e)
				{
					attempts++;
					
					if (attempts < 3)
					{
						Weather2Remastered.warn("Particle renderer's onTick() has encountered an error. Retrying...");
						e.printStackTrace();
					}
					else
					{
						Weather2Remastered.warn("Particle renderer's onTick() has failed to run correctly. Disabling particle renderer...");
						e.printStackTrace();
						ConfigClient.particle_renderer = "-1";
						WeatherAPI.refreshRenders(false);
						attempts = -1;
					}
				}
			delta = (long)((System.nanoTime() - delta) * 0.001F);
			
			if (worldDelta != ClientTickHandler.weatherManager.getWorld().getGameTime())
			{
				worldDelta = ClientTickHandler.weatherManager.getWorld().getGameTime();
				renderDebugInfo.clear();
				renderDebugInfo.add("Renderer: " + String.valueOf(WeatherAPI.getParticleRendererId()));
				renderDebugInfo.add("Delta: " + delta + "si");
				if (ConfigClient.max_particles > 0)
				{
					renderDebugInfo.add("Particle Count: " + this.particles.size() + "/" + particleLimit);
					renderDebugInfo.add("Global Particle Count: " + ClientTickHandler.weatherManager.getParticleCount() + "/" + ConfigClient.max_particles);
				}
				else
				{
					renderDebugInfo.add("Particle Count: " + this.particles.size());
					renderDebugInfo.add("Global Particle Count: " + ClientTickHandler.weatherManager.getParticleCount());
				}
				List<String> extraDebugInfo = null;
				
				attempts = 0;
				while (attempts > -1)
					try
					{
						extraDebugInfo = onDebugInfo();
						attempts = -1;
					}
					catch(Exception e)
					{
						attempts++;
						
						if (attempts < 3)
						{
							Weather2Remastered.warn("Particle renderer's onDebugInfo() has encountered an error. Retrying...");
							e.printStackTrace();
						}
						else
						{
							Weather2Remastered.warn("Particle renderer's onTick() has failed to run correctly. Disabling particle renderer...");
							e.printStackTrace();
							ConfigClient.particle_renderer = "-1";
							WeatherAPI.refreshRenders(false);
							attempts = -1;
						}
					}
				
				if (extraDebugInfo != null)
				{
					renderDebugInfo.add("-------   --------");
					renderDebugInfo.addAll(extraDebugInfo);
				}
			}
		}
		
	}
	
	/**Used to spawn particles and control particles each tick.*/
	public abstract void onTick(WeatherManagerClient manager);
	/**Used to split the particle limit evenly between each aspect of this renderer. Does not need to be used*/
	public abstract void onParticleLimitRefresh(WeatherManagerClient manager, int newParticleLimit);
	/**Used to add extra information to the debug renderer. Null is acceptable*/
	public abstract List<String> onDebugInfo();
	/**Used when the particle renderer is being removed.*/
	public abstract void cleanupRenderer();
	
	public final void cleanup()
	{
		particles.clear();
		cleanupRenderer();
		/*
		if (particleBehaviorFog != null)
		{
			if (particleBehaviorFog.particles != null)
				particleBehaviorFog.particles.clear();
			
			particleBehaviorFog = null;
		}*/
		Weather2Remastered.error("particleBehaviorFog does not exist, fartsy!!!!");
	}
	
	/**Spawns a storm particle at the specified location.*/
	/*
	public final ExtendedEntityRotFX spawnParticle(double x, double y, double z, int parRenderOrder)
	{
		return spawnParticle(x, y, z, parRenderOrder, ConfigClient.optimizedCloudRendering ? net.mrbt0907.weather2remastered.registry.ParticleRegistry.cloud32 : net.mrbt0907.weather2remastered.registry.ParticleRegistry.cloud256);
	}*/
	
	/**Spawns a storm particle at the specified location with a texture.*/
	/*
	public final ExtendedEntityRotFX spawnParticle(double x, double y, double z, int parRenderOrder, TextureAtlasSprite tex)
	{
		if (!canSpawnParticle()) return null;
		
		double speed = 0D;
		Random rand = new Random();
		ExtendedEntityRotFX entityfx = new ExtendedEntityRotFX(ClientTickHandler.weatherManager.getWorld(), x, y, z, (rand.nextDouble() - rand.nextDouble()) * speed, 0.0D, (rand.nextDouble() - rand.nextDouble()) * speed, tex);
		entityfx.pb = particleBehaviorFog;
		entityfx.renderOrder = 0;
		particleBehaviorFog.initParticle(entityfx);
		
		entityfx.setCanCollide(false);
		entityfx.callUpdatePB = false;
		entityfx.setMaxAge((system.size/2) + rand.nextInt(100));

		//temp?
		if (false)//(ConfigCoroUtil.optimizedCloudRendering)
			entityfx.setMaxAge(400);
		else {
			entityfx.setMaxAge(480);
			entityfx.setTicksFadeInMax(80);
			entityfx.setTicksFadeOutMax(400);
			Weather2Remastered.error("Not checking if optimizedCloudRendering is enabled because fartsy didn't move optimizedCloudRendering to ConfigClient!!!");
		}
		
		entityfx.particleScale = (float) (entityfx.particleScale * ConfigClient.particle_scale_mult);
		ExtendedRenderer.rotEffRenderer.addEffect(entityfx);
		particleBehaviorFog.particles.add(entityfx);
		particles.add(entityfx);
		if (ClientTickHandler.weatherManager != null)
			ClientTickHandler.weatherManager.addWeatherParticle(entityfx);
		
		particlesLeft--;
		return entityfx;
	}*/
	
	/**Refreshes the particle limit. Do not use.*/
	public final void refreshParticleLimit()
	{
		particleLimit = ClientTickHandler.weatherManager.getParticleLimit();
		int attempts = 0;
		while (attempts > -1)
			try
			{
				onParticleLimitRefresh(ClientTickHandler.weatherManager, particleLimit);
				attempts = -1;
			}
			catch(Exception e)
			{
				attempts++;
				
				if (attempts < 3)
				{
					Weather2Remastered.warn("Particle renderer's onParticleLimitRefresh() has encountered an error. Retrying...");
					e.printStackTrace();
				}
				else
				{
					Weather2Remastered.warn("Particle renderer's onParticleLimitRefresh() has failed to run correctly. Disabling particle renderer...");
					e.printStackTrace();
					ConfigClient.particle_renderer = "-1";
					WeatherAPI.refreshRenders(false);
					attempts = -1;
				}
			}
		
	}
	
	/**Checks whether the renderer can spawn another particle.*/
	public final boolean canSpawnParticle()
	{
		return particles.size() < particleLimit;
	}
}
