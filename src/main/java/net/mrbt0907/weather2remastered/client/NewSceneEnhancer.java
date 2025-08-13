package net.mrbt0907.weather2remastered.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.mrbt0907.weather2remastered.Weather2Remastered;
import net.mrbt0907.weather2remastered.api.WindReader;
import net.mrbt0907.weather2remastered.api.weather.AbstractStormObject;
import net.mrbt0907.weather2remastered.api.weather.AbstractWeatherObject;
import net.mrbt0907.weather2remastered.api.weather.AbstractWindManager;
import net.mrbt0907.weather2remastered.api.weather.WeatherEnum;
import net.mrbt0907.weather2remastered.client.weather.WeatherManagerClient;
import net.mrbt0907.weather2remastered.config.ConfigClient;
import net.mrbt0907.weather2remastered.config.ConfigMisc;
import net.mrbt0907.weather2remastered.config.ConfigStorm;
import net.mrbt0907.weather2remastered.gui.EZConfigParser;
import net.mrbt0907.weather2remastered.particle.CloudParticle;
import net.mrbt0907.weather2remastered.util.Maths;
import net.mrbt0907.weather2remastered.util.Maths.Vec;
import net.mrbt0907.weather2remastered.util.Maths.Vec3;
import net.mrbt0907.weather2remastered.util.WeatherUtil;
import net.mrbt0907.weather2remastered.util.WeatherUtilBlock;

public class NewSceneEnhancer implements Runnable
{
	/**The instance of the scene enhancer*/
	private static final NewSceneEnhancer INSTANCE = new NewSceneEnhancer();
	private final List<BlockPos> RANDOM_POS;
	
	//----- Internal Variables -----\\
	private volatile boolean run = true;
	private int errors = 0, errorsThreaded = 0;
	
	//----- Local Variables -----\\
	public final Minecraft MC;
	/**The cached result of a weather object if it exists*/
	public volatile AbstractWeatherObject cachedSystem;
	/**The cached result of a weather object's distance to the player*/
	public volatile double cachedSystemDistance = -1.0F;
	/**The cached result of a storm object's funnel distance to the player*/
	public volatile double cachedFunnelDistance = -1.0F;
	public volatile float cachedWindSpeed, cachedWindDirection;
	protected long ticksExisted, ticksThreadExisted;
	/**Used to detect if the client is in the world to initialize the scene enhancer*/
	protected volatile boolean inGame;
	
	//----- External Information -----\\
	/**Hopefully a thread safe list which cannot be written to if canSpawnParticle is false*/
	//public final List<BlockSESnapshot> queue = new ArrayList<BlockSESnapshot>();
	/**Unknown*/
	//public final ParticleBehaviors behavior;
	public float rain, rainTarget;
	public float overcast, overcastTarget;
	/**Used to smoothen fog transitions*/
	public float fogMult;
	/**Determines close the fog will be to the player*/
	public boolean enableFog;
	/**Determines how thick the fog will be at 1.0 fogMult*/
	public float fogDensity;
	/**Determines the red color for fog*/
	public float fogRed = -1.0F, fogRedTarget = -1.0F;
	/**Determines the green color for fog*/
	public float fogGreen = -1.0F, fogGreenTarget = -1.0F;
	/**Determines the blue color for fog*/
	public float fogBlue = -1.0F, fogBlueTarget = -1.0F;
	/**Determines how wet the current environment is*/
	public float dampness;
	/**Determines how far the sky box should be from the player*/
	public float renderDistance;

	private Vec3 playerPos = new Vec3(0, 0, 0);
	NewSceneEnhancer()
	{
		MC = Minecraft.getInstance().getSelf();
		//behavior = new ParticleBehaviors(null);
		RANDOM_POS = new ArrayList<BlockPos>();
		RANDOM_POS.add(new BlockPos(0, -1, 0));
		RANDOM_POS.add(new BlockPos(1, 0, 0));
		RANDOM_POS.add(new BlockPos(-1, 0, 0));
		RANDOM_POS.add(new BlockPos(0, 0, 1));
		RANDOM_POS.add(new BlockPos(0, 0, -1));
	}
	
	public static NewSceneEnhancer instance()
	{
		return INSTANCE;
	}
	
	//----- Threaded Methods -----\\
	/**Ran on the scene enhancer thread to deal with computationally heavy tasks<br>
	 *- Find valid particle locations<br>
	 *- Find valid sound locations<br>
	 *- Cache requested fog color<br>
	 *- Cache requested precipitation values<br>
	 *- Cache storm results*/
	@SuppressWarnings("static-access")
	protected void tickThread()
	{
		if (MC.level != null && MC.player != null && EZConfigParser.isEffectsEnabled(MC.level.dimension().location().toString()))
		{
			playerPos.posX = MC.player.getX();
			playerPos.posY = MC.player.getY();
			playerPos.posZ = MC.player.getZ();
			Vec playerPos2D = new Vec(MC.player.getX(), MC.player.getZ());

			if (ticksThreadExisted % 2L == 0L && ClientTickHandler.weatherManager != null)
			{
				cachedSystem = ClientTickHandler.weatherManager != null ? ClientTickHandler.weatherManager.getClosestWeather(playerPos, renderDistance, 0, Integer.MAX_VALUE, WeatherEnum.Type.CLOUD) : null;				
			}
			
			if (cachedSystem != null)
			{
				cachedSystemDistance = cachedSystem.pos.distanceSq(playerPos2D);
				
				if (cachedSystem instanceof AbstractStormObject)
					cachedFunnelDistance = ((AbstractStormObject)cachedSystem).pos_funnel_base.distanceSq(playerPos);
			}
			else
			{
				if (cachedSystemDistance >= 0.0D)
					cachedSystemDistance = -1.0D;
				
				if (cachedFunnelDistance >= 0.0D)
					cachedFunnelDistance = -1.0D;
			}
			cachedWindDirection = WindReader.getWindAngle(MC.level, playerPos);
			cachedWindSpeed = WindReader.getWindSpeed(MC.level, playerPos);
			
			tickQueuePrecipitation();
			tickQueueFog();
			//tickQueueParticles();
			//tickQueueSounds();
		}
	}
	
	/**Finds if fog needs to be rendered and sets the target fog if needed*/
	protected void tickQueueFog()
	{
		if (cachedSystem != null)
		{
			float max = 0.29F;
//			Weather2Remastered.info("fogdensity " + fogDensity + " Was calculated from Maths.clamp (Math.max((Math.abs(" + rain + " 0.125F)) /" + " 0.69F, 0.0F) * " + max + " * " + (float) ConfigClient.fog_mult + "0.0F, " + max);
/*			if (cachedSystem instanceof SandstormObject)
			{
				fogDensity = (float) ((1.0D - Math.min(cachedSystemDistance / 300.0D, 1.0D)) * max * ConfigClient.fog_mult);
				fogRedTarget = 0.35F;
				fogGreenTarget = 0.22F;
				fogBlueTarget = 0.10F;
				return;
			}
			else*/ 
			if (rainTarget != 0.0F)
			{
				fogDensity = Maths.clamp(Math.max((Math.abs(rain + 0.125F)) / 0.69F, 0.0F) * max * (float) ConfigClient.fog_mult, 0.0F, max);
				return;
			}
		}
		fogDensity = 0.0F;
	}

	/**Finds block positions of where particles can spawn and caches the results*/
	protected void tickQueueParticles()
	{
		/*if (ticksThreadExisted % 10L == 0L)
	    {
	        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
	        BlockPos.MutableBlockPos neighborPos = new BlockPos.MutableBlockPos();
	        IBlockState state;
	        Block block;
	        Material material;
	        int areaWidth = 20, areaHeight = (int) (areaWidth * 0.5F);
	        int posX = (int) MC.player.posX, posY = (int) MC.player.posY, posZ = (int) MC.player.posZ;
	        int meta;
	        List<BlockSESnapshot> snapshots = new ArrayList<>();

	        if (ConfigClient.enable_falling_leaves || ConfigClient.enable_waterfall_splash || ConfigClient.enable_fire_particle)
	        {
	            for (int x = posX - areaWidth; x < posX + areaWidth; x++) {
	                for (int y = posY - areaHeight; y < posY + areaHeight; y++) {
	                    for (int z = posZ - areaWidth; z < posZ + areaWidth; z++) {
	                        pos.setPos(x, y, z);
	                        state = getBlockState(pos.getX(), pos.getY(), pos.getZ());
	                        block = state.getBlock();
	                        if (block.equals(Blocks.AIR)) continue;

	                        BlockPos neighborImmutable = getRandomNeighbor(pos);
	                        boolean hasNeighbor = false;
	                        if (neighborImmutable != null) {
	                            neighborPos.setPos(neighborImmutable);
	                            hasNeighbor = true;
	                        }

	                        material = state.getMaterial();
	                        meta = block.getMetaFromState(state);

	                        if (ConfigClient.enable_falling_leaves &&
	                            (material.equals(Material.LEAVES) || material.equals(Material.VINE) || material.equals(Material.PLANTS))
	                            && hasNeighbor) {
	                            snapshots.add(new BlockSESnapshot(state, pos.toImmutable(), neighborPos.toImmutable(), 0));
	                        } else if (ConfigClient.enable_waterfall_splash && material.equals(Material.WATER)) {
	                            if ((meta & 8) != 0) {
	                                IBlockState state2 = getBlockState(x, y - 1, z);
	                                IBlockState state3 = getBlockState(x, y + 10, z);
	                                int meta2 = state2.getBlock().getMetaFromState(state2);

	                                if (((state2 == null || !state2.getMaterial().equals(Material.WATER)) || (meta2 & 8) == 0) &&
	                                    (state3 != null && state3.getMaterial() == Material.WATER)) {
	                                    snapshots.add(new BlockSESnapshot(state, pos.toImmutable(), null, 1));
	                                }
	                            }
	                        } else if (ConfigClient.enable_fire_particle && block == Blocks.FIRE) {
	                            snapshots.add(new BlockSESnapshot(state, pos.toImmutable(), null, 2));
	                        }
	                    }
	                }
	            }

	            queue.clear();
	            queue.addAll(snapshots);
	        } else if (!queue.isEmpty()) {
	            queue.clear();
	        }
	    }*/
	}

	/**Finds if precipitation needs to be rendered and sets the target rain if needed*/
	protected void tickQueuePrecipitation()
	{
		if (ClientTickHandler.weatherManager != null)
		{
			Vec3 pos = new Vec3(MC.player.position());
			rainTarget = ClientTickHandler.weatherManager.getRainTarget(pos, renderDistance + 512F);
			overcastTarget = ClientTickHandler.weatherManager.getOvercastTarget(pos, renderDistance + 512F);

			if (ConfigMisc.overcast_mode && ClientTickHandler.weatherManager.weatherID >= 1)
			{
				rainTarget = Math.max(rainTarget, ConfigStorm.min_overcast_rain);
				overcastTarget = Math.max(overcastTarget, ConfigStorm.min_overcast_rain);
			}

			if (WeatherUtil.getTemperature(MC.level, MC.player.blockPosition()) < 0.0F)
				rainTarget = -rainTarget;

			MC.level.getLevelData().setRaining(rainTarget != 0.0F);
			MC.level.setThunderLevel(overcast * 1.25F);
		}
        else reset();
	}
	//----- Other -----\\
		@Override
		public void run()
		{
			while(true)
			{
				if (run)
					try
					{
						tickThread();
						ticksThreadExisted++;
						errorsThreaded = 0;
						Thread.sleep(ConfigClient.scene_enhancer_thread_delay);
					}
					catch (Throwable e)
					{
						if (errorsThreaded < 5)
							Weather2Remastered.warn("Scene Enhancer tickThread encountered an error. Attempting " + (5 - errorsThreaded) + " more time(s)...");
						else
						{
							Weather2Remastered.warn("Scene Enhancer tickThread has failed to run successfuly. Disaling scene enhancer...");
							if (MC.player != null)
								MC.player.sendMessage(new StringTextComponent("Scene Enhancer has crashed on the scene thread! Disabling scene enhancer..."), MC.player.getUUID());
							run = false;
							reset();
						}
						
						Weather2Remastered.error(e);
						errorsThreaded++;
					}
			}
		}
		
		public void tick()
		{
			if (run)
				try
				{
					tickNonThread();
					ticksExisted++;
					errors = 0;
				}
				catch (Throwable e)
				{
					if (errors < 5)
						Weather2Remastered.warn("Scene Enhancer tickNonThread encountered an error. Attempting " + (5 - errors) + " more time(s)...");
					else
					{
						Weather2Remastered.warn("Scene Enhancer tickNonThread has failed to run successfuly. Disaling scene enhancer...");
						if (MC.player != null)
							MC.player.sendMessage(new StringTextComponent("Scene Enhancer has crashed on the client thread! Disabling scene enhancer..."), MC.player.getUUID());
						run = false;
						reset();
					}
					
					Weather2Remastered.error(e);
					errors++;
				}
		}
		/**Ran every game tick to update values based on given variables*/
		protected void tickNonThread()
		{
			if (inGame && MC.level == null)
			{
				inGame = false;
				reset();
			}
			else if (!inGame && MC.level != null && ClientTickHandler.weatherManager != null)
			{
				inGame = true;
				Weather2Remastered.debug("Scene Enhancer is online!");
			}
			
			if (inGame)
			{
				if (!MC.isPaused())
				{
					tickFog();
					tickPrecipitation();
					/*tickAmbiance();
					tickParticles();
					tickSounds();
					if (ConfigCoroUtil.foliageShaders && EventHandler.queryUseOfShaders())
					{
						if (!FoliageEnhancerShader.useThread)
							if (MC.world.getTotalWorldTime() % 40 == 0)
								FoliageEnhancerShader.tickClientThreaded();

						if (MC.world.getTotalWorldTime() % 5 == 0)
							FoliageEnhancerShader.tickClientCloseToPlayer();
					}
					*/
					//Weather2Remastered.error("Trying to tick with nothing to tick!");
				}
			}
			
		}

		protected void tickFog()
		{
			float mult = (float) ConfigClient.fog_change_rate;
			fogMult = Maths.adjust(fogMult, fogDensity, (fogMult < 0.1F ? 0.00005F : 0.001F) * mult);
				if (fogRed >= 0.0F && fogRed != fogRedTarget)
					fogRed = Maths.adjust(fogRed, fogRedTarget, 0.001F * mult);
				if (fogGreen >= 0.0F && fogGreen != fogGreenTarget)
					fogGreen = Maths.adjust(fogGreen, fogGreenTarget, 0.001F * mult);
				if (fogBlue >= 0.0F && fogBlue != fogBlueTarget)
					fogBlue = Maths.adjust(fogBlue, fogBlueTarget, 0.001F * mult);
		}

		/**Smoothly adjusts precipitation values based on the rain target*/
		protected void tickPrecipitation()
		{
			float rate = 0.0005F * Math.abs((float) ConfigClient.rain_change_mult);
			
			if (rainTarget < 0.0F && rain > 0.0F || rainTarget >= 0.0F && rain < 0.0F)
				rain = -rain;
			
			if (rain != rainTarget)
				rain = Maths.adjust(rain, rainTarget, rate);
			
			if (overcast != overcastTarget)
				overcast = Maths.adjust(overcast, overcastTarget, rate);
		}

		/**Processes all spawned particles and adds motion to each one*/
		protected void tickParticles()
		{
			WeatherManagerClient weatherMan = ClientTickHandler.weatherManager;
				if (weatherMan == null) return;
			AbstractWindManager windMan = weatherMan.windManager;
				if (windMan == null) return;

			Random rand = MC.level.random;
			//Weather Effects
			for (int i = 0; i < ClientTickHandler.weatherManager.effectedParticles.size(); i++)
			{
				Particle particle = ClientTickHandler.weatherManager.effectedParticles.get(i);
				
				if (particle == null || !particle.isAlive())
				{
					ClientTickHandler.weatherManager.effectedParticles.remove(i--);
					continue;
				}
					
				if (WindReader.getWindSpeed(MC.level, new Vec3(MC.player.getX(), MC.player.getY(), MC.player.getZ())) > 0.0)
				{
					if (particle instanceof CloudParticle)
					{
						CloudParticle entity1 = (CloudParticle) particle;
		
						if ((WeatherUtilBlock.getPrecipitationHeightSafe(MC.level, new BlockPos(MathHelper.floor(entity1.getX()), 0, MathHelper.floor(entity1.getZ()))).getY() - 1 < (int)entity1.getY() + 1)) //|| (entity1 instanceof ParticleTexFX))
						{
							/*if (entity1 instanceof IWindHandler)
							{
								if (((IWindHandler)entity1).getParticleDecayExtra() > 0 && WeatherUtilParticle.getParticleAge(entity1) % 2 == 0)
								{
									WeatherUtilParticle.setParticleAge(entity1, WeatherUtilParticle.getParticleAge(entity1) + ((IWindHandler)entity1).getParticleDecayExtra());
								}
							}
							else if (WeatherUtilParticle.getParticleAge(entity1) % 2 == 0)
								WeatherUtilParticle.setParticleAge(entity1, WeatherUtilParticle.getParticleAge(entity1) + 1);
		
							if ((entity1 instanceof ParticleTexFX) && ((ParticleTexFX)entity1).getParticleTexture() == ParticleRegistry.leaf)
							{
								if (entity1.getMotionX() < 0.01F && entity1.getMotionZ() < 0.01F)
									entity1.setMotionY(entity1.getMotionY() + rand.nextDouble() * 0.02 * ((ParticleTexFX) entity1).particleGravity);
								entity1.setMotionY(entity1.getMotionY() - 0.01F * ((ParticleTexFX) entity1).particleGravity);
		
							}*/
						}
		
						windMan.getEntityWindVectors(entity1, 0.05F, 5.0F);
					}
				}
			}
			//if (WeatherUtilParticle.fxLayers == null)
				//WeatherUtilParticle.getFXLayers();
			
			//Particles
		/*	for (int layer = 0; layer < WeatherUtilParticle.fxLayers.length; layer++)
			{
				for (int i = 0; i < WeatherUtilParticle.fxLayers[layer].length; i++)
				{
					for (Particle entity1 : WeatherUtilParticle.fxLayers[layer][i])
					{
						String className = entity1.getClass().getName();
						if (className.equals("net.minecraft.client.particle.Barrier") || ConfigClient.enable_vanilla_rain && className.equals("net.minecraft.client.particle.ParticleRain"))
							continue;
		
						if ((WeatherUtilBlock.getPrecipitationHeightSafe(MC.world, new BlockPos(MathHelper.floor(CoroUtilEntOrParticle.getPosX(entity1)), 0, MathHelper.floor(CoroUtilEntOrParticle.getPosZ(entity1)))).getY() - 1 < (int)CoroUtilEntOrParticle.getPosY(entity1) + 1) || (entity1 instanceof ParticleTexFX))
						{
							if ((entity1 instanceof ParticleFlame))
							{
								if (windMan.windSpeed >= 0.20) {
									entity1.particleAge += 1;
								}
							}
							else if (entity1 instanceof IWindHandler)
							{
								if (((IWindHandler)entity1).getParticleDecayExtra() > 0 && WeatherUtilParticle.getParticleAge(entity1) % 2 == 0)
								{
									entity1.particleAge += ((IWindHandler)entity1).getParticleDecayExtra();
								}
							}
							//rustle!
							if (!(entity1 instanceof EntityWaterfallFX))
							{
								if (CoroUtilEntOrParticle.getMotionX(entity1) < 0.01F && CoroUtilEntOrParticle.getMotionZ(entity1) < 0.01F)
									CoroUtilEntOrParticle.setMotionY(entity1, CoroUtilEntOrParticle.getMotionY(entity1) + rand.nextDouble() * 0.02);
							}
							windMan.getEntityWindVectors(entity1, 1F/20F, 0.5F);
						}
					}
				}
			}*/
		}
		public synchronized void reset()
		{
			cachedSystem = null;
			errors = 0;
			errorsThreaded = 0;
			rain = rainTarget = overcast = overcastTarget = fogDensity = fogMult = 0.0F;
			fogRed = fogRedTarget = fogGreen = fogGreenTarget = fogBlue = fogBlueTarget = -1.0F;
		//	if (WeatherUtilParticle.fxLayers == null)
			//	WeatherUtilParticle.getFXLayers();
			Weather2Remastered.debug("Scene Enhancer has been reset but WeatherUtilParticle is missing at the moment.");
		}
		
		public synchronized void enable()
		{
			if (!run)
			{
				run = true;
				reset();
				Weather2Remastered.debug("Scene Enhancer has been re-enabled");
			}
			else
				Weather2Remastered.warn("Scene Enhancer is already running, skipping enable...");
		}

		//----- Non Threaded Methods -----\\
		public void tickRender(RenderTickEvent event)
		{
			if (event.phase.equals(Phase.START) && MC.level != null)
			{
				MC.level.setRainLevel(Math.abs(rain)*1.25F);
				MC.level.setThunderLevel(overcastTarget * 1.25F);
//				System.out.println("Overcast "+ overcast);
			}
		}
}
