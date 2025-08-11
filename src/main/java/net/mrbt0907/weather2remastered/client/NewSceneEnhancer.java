package net.mrbt0907.weather2remastered.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.mrbt0907.weather2remastered.Weather2Remastered;
import net.mrbt0907.weather2remastered.api.weather.AbstractWeatherObject;
import net.mrbt0907.weather2remastered.config.ConfigClient;

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
	protected void tickThread()
	{
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
				{/*
					tickFog();
					tickPrecipitation();
					tickAmbiance();
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
					Weather2Remastered.error("Trying to tick with nothing to tick!");
				}
			}
			
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
				MC.level.setRainLevel(Math.abs(overcast));
				MC.level.setThunderLevel(overcast);
			}
		}
}
