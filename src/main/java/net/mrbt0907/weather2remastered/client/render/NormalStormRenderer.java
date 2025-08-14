package net.mrbt0907.weather2remastered.client.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.mrbt0907.weather2remastered.api.weather.AbstractStormObject;
import net.mrbt0907.weather2remastered.api.weather.AbstractStormObject.StormType;
import net.mrbt0907.weather2remastered.api.weather.AbstractWeatherObject;
import net.mrbt0907.weather2remastered.api.weather.WeatherEnum.Stage;
import net.mrbt0907.weather2remastered.client.NewSceneEnhancer;
import net.mrbt0907.weather2remastered.client.weather.WeatherManagerClient;
import net.mrbt0907.weather2remastered.config.ConfigClient;
import net.mrbt0907.weather2remastered.config.ConfigStorm;
import net.mrbt0907.weather2remastered.particle.CloudParticle;
import net.mrbt0907.weather2remastered.registry.ParticleRegistry;
import net.mrbt0907.weather2remastered.util.ChunkUtils;
import net.mrbt0907.weather2remastered.util.Maths;
import net.mrbt0907.weather2remastered.util.Maths.Vec;
import net.mrbt0907.weather2remastered.util.Maths.Vec3;
import net.mrbt0907.weather2remastered.util.WeatherUtil;
import net.mrbt0907.weather2remastered.util.WeatherUtilBlock;
public class NormalStormRenderer extends AbstractWeatherRenderer
{
	/**List of particles that make up the clouds above*/
	@OnlyIn(Dist.CLIENT)
	public List<CloudParticle> listParticlesCloud,
	/**List of funnel particles that a tornado makes*/
	listParticlesFunnel,
	/**List of mesocyclone particles that any supercell generates*/
	listParticlesMeso,
	/**List of mesocytclone base particles that any supercell generates*/
	listParticlesMesoAlt,
	/**List of ground particles that a storm generates*/
	listParticlesGround,
	/**List of custom rain particles that a storm generates*/
	listParticlesRain;
	
	public int particleLimitCloud, particleLimitFunnel, particleLimitGround, particleLimitRain, particleLimitMeso;
	
	public NormalStormRenderer(AbstractWeatherObject system)
	{
		super(system);
		listParticlesCloud = new ArrayList<CloudParticle>();
		listParticlesGround = new ArrayList<CloudParticle>();
		listParticlesRain = new ArrayList<CloudParticle>();
		listParticlesFunnel = new ArrayList<CloudParticle>();
		listParticlesMeso = new ArrayList<CloudParticle>();
		listParticlesMesoAlt = new ArrayList<CloudParticle>();
	}

	@Override
	public void onTick(WeatherManagerClient manager)
	{
		if (!(system instanceof AbstractStormObject)) return;
		AbstractStormObject storm = (AbstractStormObject) this.system;
		double gapRadius = storm.stormType == StormType.WATER.ordinal() ? (storm.size / 1.75D) : (storm.size / Maths.random(2.25D, 1.5D)); // Gets a random radius to use as a visual gap
		PlayerEntity entP = Minecraft.getInstance().player;
		BlockState state = ConfigClient.optimizedCloudRendering ? Blocks.AIR.defaultBlockState() : ChunkUtils.getBlockState(manager.getWorld(), (int) storm.pos_funnel_base.posX, (int) storm.pos_funnel_base.posY - 1, (int) storm.pos_funnel_base.posZ);
		Material material = state.getMaterial();
		int layerHeight = storm.getLayerHeight() + 64;
		double maxRenderDistance = NewSceneEnhancer.instance().renderDistance + 128.0D;
		float sizeCloudMult = Math.min(Math.max(storm.size * 0.0011F, 0.45F) * (float) ConfigClient.particle_scale_mult, layerHeight * 0.01F);
		float sizeFunnelMult = Math.min(Math.max(storm.funnelSize * 0.008F, 0.35F) * (float) ConfigClient.particle_scale_mult, layerHeight * 0.0055F);
		float sizeOtherMult = Math.min(Math.max(storm.size * 0.003F, 0.45F) * (float) ConfigClient.particle_scale_mult, layerHeight * 0.03F);
		float heightMult = layerHeight * 0.0055F;
		if (Maths.random(0, 10) >= 5) heightMult = layerHeight * Maths.random(0.0054F, 0.0100F);
		float rotationMult = Math.max(heightMult * 0.55F, 1.0F);
		float r = -1.0F, g = -1.0F, b = -1.0F;
		
		if (ConfigClient.enable_tornado_block_colors)
		{
			if (!ConfigClient.optimizedCloudRendering && state.getBlock().equals(Blocks.AIR))
			{
				state = storm.isSpout ? Blocks.WATER.defaultBlockState() : Blocks.DIRT.defaultBlockState();
				material = state.getMaterial();
			}
				
			if (material.equals(Material.DIRT) || material.equals(Material.GRASS) || material.equals(Material.LEAVES) || material.equals(Material.PLANT))
			{
				r = 0.3F; g = 0.25F; b = 0.15F;
			}
			else if (material.equals(Material.SAND))
			{
				r = 0.5F; g = 0.45F; b = 0.35F;
			}
			else if (material.equals(Material.SNOW))
			{
				r = 0.5F; g = 0.5F; b = 0.7F;
			}
			else if (material.equals(Material.WATER) || storm.isSpout)
			{
				r = 0.3F; g = 0.35F; b = 0.7F;
			}
			else if (material.equals(Material.LAVA))
			{
				r = 1.0F; g = 0.45F; b = 0.35F;
			}
		}
		
		int delay = Math.max(1, (int)(100F / storm.size));
		int loopSize = 1;
		int extraSpawning = 0;
		
		if (storm.isSevere())
		{
			loopSize += 4;
			extraSpawning = (int) Math.min(storm.funnelSize * 2.375F, 1000.0F);
		}
		
		Random rand = new Random();
		Vec3 playerAdjPos = new Vec3(entP.getX(), storm.pos.posY, entP.getZ());
		
		//spawn clouds
		if (ConfigClient.enable_cloud_rendering ? true : storm.isStorm())
			if (ConfigClient.optimizedCloudRendering)
			{
				boolean isStorm = storm.stage >= Stage.RAIN.getStage();
				int height = layerHeight + (isStorm ? -5 : 0);
				int size = (int) (storm.size * 1.2D);
				float finalBright = isStorm ? 0.5F : 0.75F;
				Vec3 tryPos;
				CloudParticle particle;
				for (int i = 0; i < loopSize && shouldSpawn(0); i++)
				{
					tryPos = new Vec3(storm.pos.posX + Maths.random(size), height, storm.pos.posZ + Maths.random(size));
					//position doesnt matter, set by renderer while its invisible still
					if (WeatherUtil.isAprilFoolsDay())
						particle = spawnParticle(tryPos.posX, tryPos.posY, tryPos.posZ, 0, ParticleRegistry.cloudSprite);
					else
						particle = spawnParticle(tryPos.posX, tryPos.posY, tryPos.posZ, 0);
						
					
					if (particle == null) break;
					//offset starting rotation for even distribution except for middle one
					if (i != 0) {
					    double rotPos = (i - 1);
					    float radStart = (float) Math.toRadians((360D / 8D) * rotPos); // convert degrees to radians

					    particle.setRoll(radStart); // set initial roll (rotation around the particle’s center)
					}
	
					particle.setColor(finalBright, finalBright, finalBright);
					particle.scale(400.0F * sizeCloudMult);
					particle.setLifetime(1200);
					listParticlesCloud.add(particle);
				}
			}
			else
			{
				if (manager.getWorld().getGameTime() % (delay + ConfigClient.cloud_particle_delay) == 0) {
					
					for (int i = 0; i < loopSize && shouldSpawn(0); i++)
					{
						
//						if (storm.isStorm() && !storm.isRaining()) storm.rain = 500.0F;
//						if (storm.isStorm() && storm.isRaining()) System.out.println(storm.getPos().posX + " " + storm.type.toString() + " " + storm.getPos().posZ);
						if (listParticlesCloud.size() < (storm.size + extraSpawning))// / 2F)
						{
							double spawnRad = storm.size * 1.2D;
							Vec3 tryPos = new Vec3(storm.pos.posX + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad), layerHeight + (rand.nextDouble() * 40.0F) + (storm.stage >= Stage.RAIN.getStage() ? 30.0F : 60.0D), storm.pos.posZ + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad));
							if (storm.stormType == StormType.WATER.ordinal()) {
								// --- Visual gap in storm base ---
					            // Distance squared from storm center (XZ plane only), makes a visual gap for hurricanes
					            double dx1 = tryPos.posX - storm.pos.posX;
					            double dz1 = tryPos.posZ - storm.pos.posZ;
	
					            if ((dx1 * dx1 + dz1 * dz1) < (gapRadius * gapRadius)) continue;
							}
				            // -----------------
							if (tryPos.distanceSq(playerAdjPos) < maxRenderDistance * 5) {
							
								if (storm.pos.distanceSq(tryPos) > 200.0D || storm.stormType == 0)
								if (storm.getAvoidAngleIfTerrainAtOrAheadOfPosition(storm.getAngle(), tryPos) == 0) {
									CloudParticle particle;
									if (WeatherUtil.isAprilFoolsDay()) {
										particle = spawnParticle(tryPos.posX, tryPos.posY, tryPos.posZ, 0, ParticleRegistry.chicken);
										if (particle == null) break;
										particle.setColor(1F, 1F, 1F);
									}
									else
									{
										
										float finalRed = Math.min(0.8F, 0.6F + (rand.nextFloat() * 0.2F)) + (storm.stage >= Stage.RAIN.getStage() ? -0.3F : 0.0F);
										float finalGreen = finalRed;
										float finalBlue = finalRed;
										if (!storm.isFirenado && !WeatherUtil.isAprilFoolsDay()) finalRed = Maths.clamp(finalRed -= NewSceneEnhancer.instance().overcast, (finalGreen + finalBlue)* ConfigClient.cloud_greenblue_mult, 1.0F);
										particle = spawnParticle(tryPos.posX, tryPos.posY, tryPos.posZ, 0, (storm.stage <= Stage.RAIN.getStage() ? net.mrbt0907.weather2remastered.registry.ParticleRegistry.cloud256_light : net.mrbt0907.weather2remastered.registry.ParticleRegistry.cloudSprite));
										if (particle == null) break;
											particle.setColor(finalRed, finalGreen, finalBlue);
										
										if (storm.isSevere())
											if (storm.isFirenado)
											{
													particle.setSprite(net.mrbt0907.weather2remastered.registry.ParticleRegistry.cloud256_fire);
													particle.setColor(1F, 1F, 1F);
											}
									}
									particle.setRoll(Maths.random(80.0F, 100.0F));
									particle.scale(1750.0F * sizeCloudMult);
									listParticlesCloud.add(particle);
								}
							}
						}
					}
				}
			}
		
		//ground effects
		if (ConfigClient.enable_tornado_debris && !ConfigClient.optimizedCloudRendering && storm.stormType == StormType.LAND.ordinal() && storm.stage > Stage.SEVERE.getStage() && r >= 0.0F && !material.isLiquid())
		{
			if (manager.getWorld().getGameTime() % (delay + ConfigClient.ground_debris_particle_delay) == 0)
			{
				for (int i = 0; i < Math.round(((float) storm.stage / storm.stageMax) * 32) && shouldSpawn(2); i++)
				{
					double spawnRad = storm.funnelSize * 1.75D;
					Vec3 tryPos = new Vec3(storm.pos_funnel_base.posX + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad), storm.pos_funnel_base.posY, storm.pos_funnel_base.posZ + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad));
					double distance = tryPos.distanceSq(playerAdjPos);
					if (distance < maxRenderDistance && distance < 320.0D)
					{
						int groundY = WeatherUtilBlock.getPrecipitationHeightSafe(manager.getWorld(), new BlockPos((int)tryPos.posX, 0, (int)tryPos.posZ)).getY();
						CloudParticle particle;
						if (WeatherUtil.isAprilFoolsDay())
							particle = spawnParticle(tryPos.posX, groundY, tryPos.posZ, 1, ParticleRegistry.potato);
						else
						{
							int reee = Maths.random(3);
							TextureAtlasSprite sprite = reee == 1 ? ParticleRegistry.debris_1 : reee == 2 ? ParticleRegistry.debris_2 : reee == 3 ? ParticleRegistry.debris_3 : ParticleRegistry.leaf;
							particle = spawnParticle(tryPos.posX, groundY, tryPos.posZ, 1, sprite);
						}
						if (particle == null) break;
						particle.setColor(r, g, b);
						particle.setTicksFadeInMax(8);
						particle.setTicksFadeOutMax(80);
						particle.setGravity(0.25F);
						particle.setLifetime(80);
						particle.scale(Maths.random(3.0F, 25.0F));
						particle.setRotYaw(rand.nextInt(360));
						particle.setPitch(30 + rand.nextInt(60));
						//particle.setMotionY(0.9D);
						listParticlesGround.add(particle);
					}
					if (distance < maxRenderDistance && distance < 2048.0D && Maths.random(0, 9) > 7) {
						int groundY = WeatherUtilBlock.getPrecipitationHeightSafe(manager.getWorld(), new BlockPos((int)tryPos.posX, 0, (int)tryPos.posZ)).getY();
						CloudParticle particle;
						if (WeatherUtil.isAprilFoolsDay())
							particle = spawnParticle(tryPos.posX, groundY, tryPos.posZ, 1, ParticleRegistry.potato);
						else
						{
							particle = spawnParticle(tryPos.posX, groundY, tryPos.posZ, 1, ParticleRegistry.cloudSprite);
						}
						particle.setColor(r, g, b);
						particle.setTicksFadeInMax(8);
						particle.setTicksFadeOutMax(50);
						particle.setGravity(0.25F);
						particle.setLifetime(60);
						particle.scale(Maths.random(230.0F, (storm.stage / storm.stageMax) * 425.0F));
						particle.setRotYaw(rand.nextInt(360));
						particle.setPitch(30 + rand.nextInt(60));
						//particle.setMotionY(0.9D);
						listParticlesGround.add(particle);
					}
				}
			}
		}

		
		delay = 1;
		loopSize = 3 + (storm.funnelSize > 300.0F ? 4 : (int)(storm.funnelSize / 80.0F));
		double spawnRad = storm.funnelSize * 0.01F;
		
		if (storm.stage >= Stage.TORNADO.getStage() + 1) 
			spawnRad *= 48.25D;
		
		//spawn funnel - tornado
		if (storm.isDeadly() && storm.stormType == 0 || storm.isSpout)
		{
			//System.out.println(loopSize);
			if (manager.getWorld().getGameTime() % (delay + ConfigClient.funnel_particle_delay) == 0)
			{
				for (int i = 0; i < loopSize && shouldSpawn(1); i++)
				{
					Vec3 tryPos = new Vec3(storm.pos_funnel_base.posX + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad), storm.pos.posY, storm.pos_funnel_base.posZ + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad));

					if (tryPos.distanceSq(playerAdjPos) < maxRenderDistance)
					{
						CloudParticle particle;
						if (!storm.isFirenado)
							if (WeatherUtil.isAprilFoolsDay())
								particle = spawnParticle(tryPos.posX, storm.pos_funnel_base.posY, tryPos.posZ, 2);
							else
								particle = spawnParticle(tryPos.posX, storm.pos_funnel_base.posY, tryPos.posZ, 2, net.mrbt0907.weather2remastered.registry.ParticleRegistry.tornado256);
						else
							particle = spawnParticle(tryPos.posX, storm.pos_funnel_base.posY, tryPos.posZ, 2, net.mrbt0907.weather2remastered.registry.ParticleRegistry.cloud256_fire);
						if (particle == null) break;
							
							//move these to a damn profile damnit!
						particle.setLifetime(120 + ((storm.stage-1) * 10) + rand.nextInt(100));
						//particle.rotationYaw = rand.nextInt(360);
							
						float finalRed = Math.min(0.6F, 0.4F + (rand.nextFloat() * 0.2F));
						float finalGreen = finalRed;
						float finalBlue = finalRed;
						if (!storm.isFirenado && !WeatherUtil.isAprilFoolsDay())
							finalRed = Maths.clamp(finalRed -= NewSceneEnhancer.instance().overcast, (finalGreen + finalBlue)* ConfigClient.funnel_greenblue_mult, 1.0F);

						//highwind aka spout in this current code location
						if (storm.stage == Stage.SEVERE.getStage())
							particle.scale(100.0F * sizeFunnelMult * heightMult);
						else
							particle.scale(450.0F * sizeFunnelMult * heightMult);

						if (r >= 0.0F)
						{
							particle.setColor(r, g, b);
							////particle.setFinalColor(0.0F, finalRed, finalGreen, finalBlue);
							////particle.setColorFade(0.75F);
						}
						else
							particle.setColor(finalRed, finalGreen, finalBlue);
						
						if (storm.isFirenado)
						{
							particle.setColor(1F, 1F, 1F);
							particle.scale(particle.getScale() * 0.7F);
						}
							
						listParticlesFunnel.add(particle);
					}
					
				}
			}
		}
		// Distant downfall
		if (ConfigClient.enable_distant_downfall && storm.ticks % 20 == 0 && ConfigClient.distant_downfall_particle_rate > 0.0F && listParticlesRain.size() < 1000 && storm.stage > Stage.THUNDER.getStage() && storm.isRaining() && storm.temperature > 0.0F)
		{
			int particleCount = (int)Math.min(Math.ceil(storm.rain * storm.stage * ConfigClient.distant_downfall_particle_rate * 0.005F), 50.0F);
			
			for (int i = 0; i < particleCount && shouldSpawn(3); i++)
			{
				double spawnRad2 = storm.size;
				Vec3 tryPos = new Vec3(storm.pos.posX + (rand.nextDouble()*spawnRad2) - (rand.nextDouble()*spawnRad2), storm.pos.posY + rand.nextInt(50), storm.pos.posZ + (rand.nextDouble()*spawnRad2) - (rand.nextDouble()*spawnRad2));
				if (tryPos.distanceSq(playerAdjPos) < maxRenderDistance)
				{
					CloudParticle particle;
					float finalBright = Math.min(0.7F, 0.5F + (rand.nextFloat() * 0.2F)) + (storm.stage >= Stage.RAIN.getStage() ? -0.2F : 0.0F );
					particle = spawnParticle(tryPos.posX, tryPos.posY, tryPos.posZ, 2, net.mrbt0907.weather2remastered.registry.ParticleRegistry.distant_downfall);
					if (particle == null) break;
					particle.setLifetime(200 + rand.nextInt(100));
					particle.scale(550.0F * sizeOtherMult);
					particle.setAlpha(0.0F);
					particle.setColor(finalBright, finalBright, finalBright);
					particle.facePlayer = false;
					listParticlesRain.add(particle);
				}
			}
		}
		for (int i = 0; i < listParticlesFunnel.size(); i++)
		{/*
			CloudParticle ent = listParticlesFunnel.get(i);
			if (!ent.isAlive() || ent.getY() > layerHeight + 200)
			{
				ent.remove();
				listParticlesFunnel.remove(ent);
			}
			else
			{
				 double var16 = storm.pos.posX - ent.getX();
				 double var18 = storm.pos.posZ - ent.getZ();
				 //ent.rotationYaw = (float)(Maths.fastATan2(var18, var16) * 180.0D / Math.PI) - 90.0F;
				 ent.setRotYaw(ent.getRotYaw() + ent.getEntityId() % 90);
				 ent.setPitch(-30);
				 
				 storm.spinEntity(ent);
			}*/
		}
		if (storm.getStage() > Stage.THUNDER.getStage()&& manager.getWorld().getGameTime() % (delay + ConfigClient.cloud_particle_delay) == 0) {
			for (int i = 0; i < loopSize * 10 && shouldSpawn(4); i++)
			{
				if ((listParticlesMeso.size() < (storm.size + extraSpawning) * 30F))// && (listParticlesMesoAlt.size() < (storm.size + extraSpawning) * 30F))
				{
					//System.out.println(listParticlesMeso.size());
					int cloud = Maths.random(0, 2);
					double stormRad = storm.size * (storm.stormType ==  StormType.WATER.ordinal() ? 1.2 : cloud !=0 ? 1.1D : 0.95D); //Used for main meso clouds, alternative texture for cumulonimbus cloud. We spawn it 0.95D instead so it looks more realistic.
					double stormRad2 = storm.size * 0.80D; //Used for base cloud. Base cloud is currently spawned using tryPos2.
					double defMesoHeight = layerHeight + (rand.nextDouble() * 40.0F);

				//	Vec3 tryPos = new Vec3(storm.pos.posX + (rand.nextDouble()*stormRad) - (rand.nextDouble()*stormRad), storm.stormType == StormType.WATER.ordinal() ? defMesoHeight : defMesoHeight * ConfigClient.meso_height , storm.pos.posZ + (rand.nextDouble()*stormRad) - (rand.nextDouble()*stormRad));
					//Vec3 tryPos2 = new Vec3(storm.pos.posX + (rand.nextDouble()*stormRad2) - (rand.nextDouble()*stormRad2), storm.stormType == StormType.WATER.ordinal() ? defMesoHeight : defMesoHeight * ConfigClient.meso_height , storm.pos.posZ + (rand.nextDouble()*stormRad2) - (rand.nextDouble()*stormRad2));
					Vec3 tryPos = new Vec3(
			            storm.pos.posX + (rand.nextDouble() * stormRad) - (rand.nextDouble() * stormRad),
			            storm.stormType == StormType.WATER.ordinal() ? defMesoHeight : defMesoHeight * ConfigClient.meso_height,
			            storm.pos.posZ + (rand.nextDouble() * stormRad) - (rand.nextDouble() * stormRad)
			        );

			        Vec3 tryPos2 = new Vec3(
			            storm.pos.posX + (rand.nextDouble() * stormRad2) - (rand.nextDouble() * stormRad2),
			            storm.stormType == StormType.WATER.ordinal() ? defMesoHeight + 250.0F : defMesoHeight * ConfigClient.meso_height,
			            storm.pos.posZ + (rand.nextDouble() * stormRad2) - (rand.nextDouble() * stormRad2)
			        );

			        // --- Visual gap in storm base ---
			        // Distance squared from storm center (XZ plane only), makes a visual gap instead of clipping the funnel with our particles
			        double dx1 = tryPos.posX - storm.pos.posX;
			        double dz1 = tryPos.posZ - storm.pos.posZ;
			        double dx2 = tryPos2.posX - storm.pos.posX;
			        double dz2 = tryPos2.posZ - storm.pos.posZ;

			        if ((dx1 * dx1 + dz1 * dz1) < (gapRadius * gapRadius)) continue;
			        if ((dx2 * dx2 + dz2 * dz2) < (gapRadius * gapRadius)) continue;
			        // -----------------

					if (tryPos.distanceSq(playerAdjPos) < maxRenderDistance) {
						if (storm.stormType == 1 && storm.pos.distanceSq(tryPos) > 350.0D || storm.stormType == 0)
						if (storm.getAvoidAngleIfTerrainAtOrAheadOfPosition(storm.getAngle(), tryPos) == 0) {
							CloudParticle particle;
							CloudParticle particle2;

							if (WeatherUtil.isAprilFoolsDay()) {
								particle = spawnParticle(tryPos.posX, tryPos.posY, tryPos.posZ, 0, ParticleRegistry.chicken);
								particle2 = spawnParticle(tryPos2.posX, tryPos.posY - 60D, tryPos2.posZ, 0, ParticleRegistry.chicken);
								if (particle == null || particle2 == null) break;
								particle.setColor(1F, 1F, 1F);
								particle2.setColor(1F, 1F, 1F);
							}
							else
							{
								float finalRed = Math.min(0.8F, 0.65F + (rand.nextFloat() * 0.2F) -0.3F);
								float finalGreen = finalRed;
								float finalBlue = finalRed;
								 
								if (!storm.isFirenado && !WeatherUtil.isAprilFoolsDay())
									finalRed = Maths.clamp(finalRed -= NewSceneEnhancer.instance().overcast, (finalGreen + finalBlue)* ConfigClient.meso_greenblue_mult, 1.0F);
								particle = spawnParticle(tryPos.posX, tryPos.posY, tryPos.posZ, 1,
										(cloud != 0 ? net.mrbt0907.weather2remastered.registry.ParticleRegistry.cloud256_meso : net.mrbt0907.weather2remastered.registry.ParticleRegistry.cloud256_meso_wall));
								//particle2 = spawnParticle(tryPos2.posX, tryPos.posY - 60D, tryPos2.posZ, 1, net.mrbt0907.weather2remastered.registry.ParticleRegistry.cloud256_meso);
								if (particle == null) break; //|| particle2 == null) break;
								particle.setColor(finalRed, finalGreen, finalBlue);
								//particle2.setColor(finalRed, finalGreen, finalBlue);
								if (storm.isFirenado)
								{
										particle.setSprite(net.mrbt0907.weather2remastered.registry.ParticleRegistry.cloud256_fire);
										//particle2.setSprite(net.mrbt0907.weather2remastered.registry.ParticleRegistry.cloud256_fire);
										particle.setColor(1F, 1F, 1F);
										//particle2.setColor(1F, 1F, 1F);
								}									
							}
							particle.setRotationState(true);
							particle.setRotation(new Vec (storm.pos.posX, storm.pos.posZ), (int)((spawnRad+1) * 1.5), storm.getSpeed() * 20);
							particle.setPitch(Maths.random(70, 110));
							particle.scale(1250.0F * sizeCloudMult);
							particle.setLifetime(12000);
							//particle2.scale(2200.0F * sizeCloudMult);
							listParticlesMeso.add(particle);
							//listParticlesMesoAlt.add(particle2);
							}
						}
					}
				}
			}
		for (int i = 0; i < listParticlesMeso.size(); i++)
		{
			CloudParticle ent = listParticlesMeso.get(i);
			if (!ent.isAlive())
			{
				ent.remove();
				listParticlesMeso.remove(ent);
			}
			else
			{/*
				double speed = storm.spin + (rand.nextDouble() * 0.04D) * rotationMult;
				double curSpeed = Math.sqrt(ent.getMotionX() * ent.getMotionX() + ent.getMotionY() * ent.getMotionY() + ent.getMotionZ() * ent.getMotionZ());
				if (storm.stormType == 1)
					speed *= 2.0D;
				
				double vecX = ent.getX() - storm.pos.posX;
				double vecZ = ent.getZ() - storm.pos.posZ;
				float angle = (float)(Maths.fastATan2(vecZ, vecX) * 180.0D / Math.PI);
				double curDist = 1000.0D;
				//fix speed causing inner part of formation to have a gap
				angle += speed * 2D;
				
				angle -= (ent.getEntityId() % 10) * 3D;
				
				//random addition
				angle += rand.nextInt(10) - rand.nextInt(10);
					if (ent.getY() > layerHeight + 10.0D)
						ent.setPos(ent.getX(), layerHeight + 10.0D, ent.getZ());
					if (storm.stage >= Stage.TORNADO.getStage())
					{
						if (storm.stormType == StormType.WATER.ordinal())
						{
							ent.setPos(ent.getX(), layerHeight+40.0D, ent.getZ());
							angle += 30 + ((ent.getEntityId() % 5) * 4);
							if (curDist > 150 + ((storm.stage-Stage.TORNADO.getStage()+1) * 30)) 
								angle += 10;
						}
						else
							angle += 30 + ((ent.getEntityId() % 5) * 4);
						
					}
					else if (curDist > 150) //make a wider spinning lower area of cloud, for high wind
							angle += 10 + ((ent.getEntityId() % 5) * 4);
					
					double var16 = storm.pos.posX - ent.getX();
					double var18 = storm.pos.posZ - ent.getZ();
					//ent.setRotYaw((int)(Maths.fastATan2(var18, var16) * 180.0D / Math.PI) - 90);
					//ent.setPitch(-30 - (ent.getEntityId() % 10)); //meso clouds
					ent.scale((storm.stormType == StormType.WATER.ordinal() ? 1500.0F : 900.0F) * sizeCloudMult);
				if (curSpeed < speed * 20D)
				{
					//ent.setMotionX(ent.getMotionX() + -Maths.fastSin(Math.toRadians(angle)) * speed);
					//ent.setMotionZ(ent.getMotionZ() + Maths.fastCos(Math.toRadians(angle)) * speed);
				}
				ent.setRotationState(true);
				ent.setRotation(new Vec(storm.pos.posX, storm.pos.posZ), (int)spawnRad * 20, storm.getSpeed() * 2000);*/
			}
		}
		for (int i = 0; i < listParticlesMesoAlt.size(); i++)
		{/*
			CloudParticle ent = listParticlesMesoAlt.get(i);
			if (!ent.isAlive())
			{
				ent.remove();
				listParticlesMesoAlt.remove(ent);
			}
			else
			{
				double speed = storm.spin + (rand.nextDouble() * 0.04D) * rotationMult;
				double curSpeed = Math.sqrt(ent.getMotionX() * ent.getMotionX() + ent.getMotionY() * ent.getMotionY() + ent.getMotionZ() * ent.getMotionZ());
				if (storm.stormType == 1)
					speed *= 2.0D;
				
				double vecX = ent.getX() - storm.pos.posX;
				double vecZ = ent.getZ() - storm.pos.posZ;
				float angle = (float)(Maths.fastATan2(vecZ, vecX) * 180.0D / Math.PI);
				double var16 = storm.pos.posX - ent.getX();
				double var18 = storm.pos.posZ - ent.getZ();
				ent.setRotYaw((int)(Maths.fastATan2(var18, var16) * 180.0D / Math.PI) - 90);
				ent.setPitch(90);// = 90F; //rotate with meso clouds
				ent.setSprite(net.mrbt0907.weather2remastered.registry.ParticleRegistry.cloud256_meso);
				if (curSpeed < speed * 20D)
				{
					//ent.setMotionX(ent.getMotionX() + -Maths.fastSin(Math.toRadians(angle)) * speed);
					//ent.setMotionZ(ent.getMotionZ() + Maths.fastCos(Math.toRadians(angle)) * speed);
				}
			}*/
		}
		for (int i = 0; i < listParticlesCloud.size(); i++)
		{
			CloudParticle ent = listParticlesCloud.get(i);
			if (!ent.isAlive() || storm.stormType == StormType.WATER.ordinal() && true)// && ent.getDistance(storm.pos.posX, ent.getY(), storm.pos.posZ) < storm.funnelSize)
			{
				ent.remove();
				listParticlesCloud.remove(ent);
			}
			else
			{/*
				double curSpeed = Math.sqrt(ent.getMotionX() * ent.getMotionX() + ent.getMotionY() * ent.getMotionY() + ent.getMotionZ() * ent.getMotionZ());
				double curDist = 10D;
				
				if (storm.isSevere())
				{
					double speed = storm.spin + (rand.nextDouble() * 0.04D) * rotationMult;
					double distt = Math.max(ConfigStorm.max_storm_size, storm.funnelSize + 20.0D);//300D;
					
					if (storm.stormType == 1)
					{
						speed *= 2.0D;
						distt *= 2.0D;
					}
					
					double vecX = ent.getX() - storm.pos.posX;
					double vecZ = ent.getZ() - storm.pos.posZ;
					float angle = (float)(Maths.fastATan2(vecZ, vecX) * 180.0D / Math.PI);
					
					//fix speed causing inner part of formation to have a gap
					angle += speed * 10D;
					
					angle -= (ent.getEntityId() % 10) * 3D;
					
					//random addition
					angle += rand.nextInt(10) - rand.nextInt(10);
					
					if (curDist > distt)
					{
						angle += 40;
					}
					
					ent.setPitch((int) (90.0F - (90.0f * Math.min(ent.getY() / (layerHeight + ent.getScale() * 0.75F), 1.0F))));
					if (ConfigClient.enable_extended_render_distance)
						ent.scale(1000.0F * sizeCloudMult);
					
					if (curSpeed < speed * 20D)
					{
						//ent.setMotionX(ent.getMotionX() + -Maths.fastSin(Math.toRadians(angle)) * speed);
					//	ent.setMotionZ(ent.getMotionZ() + Maths.fastCos(Math.toRadians(angle)) * speed);
					}
				}
				else
				{
					float cloudMoveAmp = 0.65F * (1 + storm.layer);
					
					float speed = storm.getSpeed() * cloudMoveAmp;
					float angle = storm.getAngle();

					//TODO: prevent new particles spawning inside or near solid blocks

					if ((manager.getWorld().getGameTime()) % 40 == 0) {
						ent.avoidTerrainAngle = storm.getAvoidAngleIfTerrainAtOrAheadOfPosition(angle, new Vec3(ent.getX(), ent.getY(), ent.getZ()));
					}

					angle += ent.avoidTerrainAngle;

					if (ent.avoidTerrainAngle != 0)
						speed *= 0.5D;
					
					
					if (curSpeed < speed * 1D)
					{
						//ent.setMotionX(ent.getMotionX() + -Maths.fastSin(Math.toRadians(angle)) * speed);
						//ent.setMotionZ(ent.getMotionZ() + Maths.fastCos(Math.toRadians(angle)) * speed);
					}
				}
				
				
				float dropDownSpeedMax = 0.5F;
				
				if (storm.stormType == 1 || storm.stage > 8)
					dropDownSpeedMax = 1.9F;
				
				if (ent.getMotionY() < -dropDownSpeedMax) {}
					//ent.setMotionY(-dropDownSpeedMax);
				
				
				if (ent.getMotionY() > dropDownSpeedMax) {} 
					//ent.setMotionY(dropDownSpeedMax);
				*/
			}
		}
		
		
		
		for (int i = 0; i < listParticlesRain.size(); i++)
		{
			CloudParticle ent = listParticlesRain.get(i);
			if (!ent.isAlive())
				listParticlesRain.remove(ent);
			
			if (ent.getMotionY() > -heightMult) {}
				//ent.setMotionY(ent.getMotionY() - (0.1D));
			
			double speed = Math.sqrt(ent.getMotionX() * ent.getMotionX() + ent.getMotionY() * ent.getMotionY() + ent.getMotionZ() * ent.getMotionZ());
			double spin = storm.spin + 0.04F;
			double vecX = ent.getX() - storm.pos.posX;
			double vecZ = ent.getZ() - storm.pos.posZ;
			float angle = (float)(Maths.fastATan2(vecZ, vecX) * 180.0D / Math.PI);
			ent.setPitch(0);
			ent.setRotYaw((int)angle + 90);
			
			if (ent.getAlpha() < 0.01F)
				ent.setAlpha(ent.getAlpha() + 0.01F);
			if (speed < spin * 15.0D * rotationMult)
			{
				//ent.setMotionX(ent.getMotionX() + -Maths.fastSin(Math.toRadians(angle)) * spin);
				//ent.setMotionZ(ent.getMotionZ() + Maths.fastCos(Math.toRadians(angle)) * spin);
			}
		}
		
		for (int i = 0; i < listParticlesGround.size(); i++)
		{
			CloudParticle ent = listParticlesGround.get(i);
			
			if (!ent.isAlive())
				listParticlesGround.remove(ent);
			else
				storm.spinEntity(ent);
		}
		if (storm.strength != 100.0F)
			storm.strength = 100.0F;
	}
	//Configure particle limits here
	@Override
	public void onParticleLimitRefresh(WeatherManagerClient manager, int newParticleLimit)
	{
		particleLimitCloud = (int) (newParticleLimit * 0.05F);
		particleLimitGround = (int) (newParticleLimit * 0.25F);
		particleLimitRain = (int) (newParticleLimit * 0.1F);
		particleLimitFunnel = (int) (newParticleLimit * 0.5F);
		particleLimitMeso = (int) (newParticleLimit * 0.1F);
	}
	
	@Override
	public void cleanupRenderer()
	{
		//for (EntityRotFX particle : listParticlesCloud) particle.setExpired();
			listParticlesCloud.clear();
		//for (EntityRotFX particle : listParticlesFunnel) particle.setExpired();
			listParticlesFunnel.clear();
		//for (EntityRotFX particle : listParticlesGround) particle.setExpired();
			listParticlesGround.clear();
		//for (EntityRotFX particle : listParticlesRain) particle.setExpired();
			listParticlesRain.clear();
			listParticlesMeso.clear();
	}
	
	private boolean shouldSpawn(int type)
	{
		switch(type)
		{
			case 0:
				return listParticlesCloud.size() < particleLimitCloud;
			case 1:
				return listParticlesFunnel.size() < particleLimitFunnel;
			case 2:
				return listParticlesGround.size() < particleLimitGround;
			case 3:
				return listParticlesRain.size() < particleLimitRain;
			case 4:
				return listParticlesMeso.size() < particleLimitMeso;
			default:
				return false;
		}
	}

	@Override
	public List<String> onDebugInfo()
	{
		List<String> debugInfo = new ArrayList<String>();
		debugInfo.add("Default Renderer - Version 2.0");
		debugInfo.add("");
		debugInfo.add("Cloud Particles: " + listParticlesCloud.size() + "/" + particleLimitCloud);
		debugInfo.add("Funnel Particles: " + listParticlesFunnel.size() + "/" + particleLimitFunnel);
		debugInfo.add("Ground Particles: " + listParticlesGround.size() + "/" + particleLimitGround);
		debugInfo.add("Rain Particles: " + listParticlesRain.size() + "/" + particleLimitRain);
		return debugInfo;
	}
}