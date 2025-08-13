package net.mrbt0907.weather2.client.weather;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.weather.IWeatherRain;
import net.mrbt0907.weather2.api.weather.IWeatherStaged;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Type;
import net.mrbt0907.weather2.client.NewSceneEnhancer;
import net.mrbt0907.weather2.config.ConfigClient;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.config.ConfigVolume;
import net.mrbt0907.weather2.entity.EntityLightningEX;
import net.mrbt0907.weather2.registry.SoundRegistry;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.weather.WeatherManager;
import net.mrbt0907.weather2.weather.storm.FrontObject;
import net.mrbt0907.weather2.weather.storm.StormObject;
import net.mrbt0907.weather2.weather.storm.WeatherObject;
import net.mrbt0907.weather2.weather.storm.SandstormObject;
import net.mrbt0907.weather2.weather.volcano.VolcanoObject;

@SideOnly(Side.CLIENT)
public class WeatherManagerClient extends WeatherManager
{
	private static final Minecraft MC = Minecraft.getMinecraft(); 
	//data for client, stormfronts synced from server
	//new for 1.10.2, replaces world.weatherEffects use
	public List<Particle> effectedParticles = new ArrayList<Particle>();
	public List<Particle> weatherParticles = new ArrayList<Particle>();
	public static StormObject closestStormCached;
	public int weatherID = 0;
	public int weatherRainTime = 0;
	private int particleLimit = 0;

	public WeatherManagerClient(World world)
	{
		super(world);
	}
	
	@Override
	public World getWorld()
	{
		return FMLClientHandler.instance().getClient().world;
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
	
	/**Gets called when the server sends a network packet<p><b>Network Command List</b><br>
	 *0 - Update Vanilla Weather<br>
	 *1 - Create Weather Object<br>
	 *2 - Update Weather Object<br>
	 *3 - Remove Weather Object<br>
	 *4 - Create Volcano Object<br>
	 *5 - Update Volcano Object<br>
	 *6 - Update Wind Manager<br>
	 *7 - Create Lightning Bolt*/
	public void nbtSyncFromServer(NBTTagCompound mainNBT)
	{
		int command = mainNBT.getInteger("command");
		switch(command)
		{
			case 0:
				weatherID = mainNBT.getInteger("weatherID");
				weatherRainTime = mainNBT.getInteger("weatherRainTime");
				break;
			case 1:
			{
				NBTTagCompound nbt = mainNBT.getCompoundTag("weatherObject");
				FrontObject front = getFront(nbt.getUniqueId("frontUUID"));
				UUID uuid = nbt.getUniqueId("ID");
				
				Type weatherObjectType = Type.get(nbt.getInteger("weatherType"));
				
				WeatherObject wo = null;
				if (weatherObjectType.ordinal() < Type.SANDSTORM.ordinal())
				{
					Weather2.debug("Creating a new storm: " + uuid.toString());
					wo = new StormObject(front);
				}
				else
				{
					Weather2.debug("Creating a new sandstorm: " + uuid.toString());
					wo = new SandstormObject(this);
				}
				
				//StormObject so
				wo.nbt.setNewNBT(nbt);
				wo.nbt.updateCacheFromNew();
				wo.readFromNBT();
				
				front.addWeatherObject(wo);
				refreshParticleLimit();
				break;
			}
			case 2:
			{
				NBTTagCompound nbt = mainNBT.getCompoundTag("weatherObject");
				FrontObject front = getFront(nbt.getUniqueId("frontUUID"));
				if(front == null) break;
					
				WeatherObject wo = front.getWeatherObject(nbt.getUniqueId("ID"));
				if (wo != null)
				{
					wo.nbt.setNewNBT(nbt);
					wo.readFromNBT();
					wo.nbt.updateCacheFromNew();
				}
				break;
			}
			case 3:
			{
				UUID uuidA = mainNBT.getUniqueId("weatherObject"), uuidB = mainNBT.getUniqueId("frontObject");
				FrontObject front = getFront(uuidB);
				WeatherObject system = systems.get(uuidA);
				
				if (front != null)
					front.removeWeatherObject(uuidA);
				else if (system != null)
					removeWeatherObject(uuidA);

				refreshParticleLimit();
				break;
			}
			case 4:
			{
				NBTTagCompound nbt = mainNBT.getCompoundTag("volcanoObject");
				VolcanoObject vo = new VolcanoObject(this);
				Weather2.debug("Creating a new volcano: " + nbt.getUniqueId("ID"));
				vo.nbtSyncFromServer(nbt);
				addVolcanoObject(vo);
				break;
			}
			case 5:
			{
				NBTTagCompound stormNBT = mainNBT.getCompoundTag("volcanoObject");
				UUID uuid = stormNBT.getUniqueId("ID");
				
				if (volcanoUUIDS.contains(uuid))
					getVolcanoObjectByID(uuid).nbtSyncFromServer(stormNBT);
				break;
			}
			case 6:
			{
				NBTTagCompound nbt = mainNBT.getCompoundTag("manager");
				windManager.nbtSyncFromServer(nbt);
				break;
			}
			case 7:
			{
				int posXS = mainNBT.getInteger("posX");
				int posYS = mainNBT.getInteger("posY");
				int posZS = mainNBT.getInteger("posZ");
				if (mainNBT.hasKey("entityID"))
				{
					double posX = (double)posXS;
					double posY = (double)posYS;
					double posZ = (double)posZS;
					Entity ent = new EntityLightningEX(world, posX, posY, posZ);
					ent.serverPosX = posXS;
					ent.serverPosY = posYS;
					ent.serverPosZ = posZS;
					ent.rotationYaw = 0.0F;
					ent.rotationPitch = 0.0F;
					ent.setEntityId(mainNBT.getInteger("entityID"));
					world.addWeatherEffect(ent);
				}
				else
				{
					int x = mainNBT.getInteger("posX"), y = mainNBT.getInteger("posY"), z = mainNBT.getInteger("posZ");
					double distance = Maths.distanceSq(MC.player.posX, MC.player.posY, MC.player.posZ, x, y, z);
					if (MC.player != null)
					{
						if (distance < ConfigStorm.max_lightning_bolt_distance)
						{
							if (ConfigClient.enable_sky_lightning)
								world.setLastLightningBolt(4);
							world.playSound(x, y, z, SoundRegistry.thunderNear, SoundCategory.WEATHER, 10000.0F * (float)ConfigVolume.lightning, Maths.random(0.65F, 0.75F), true);
						}
						else if (distance < ConfigStorm.max_lightning_bolt_distance * 1.5D)
							world.playSound(x, y, z, SoundRegistry.thunderFar, SoundCategory.WEATHER, 10000.0F * (float)ConfigVolume.lightning, Maths.random(0.65F, 0.75F), false);
					}
				}
				break;
			}
			case 11:
			{
				NBTTagCompound nbt = mainNBT.getCompoundTag("frontObject");
				UUID uuid = mainNBT.getUniqueId("uuid");
				if (nbt.hasKey("posX"))
				{
					Weather2.debug("Creating a new front: " + uuid.toString());
					FrontObject front = createFront(nbt.getInteger("layer"), nbt.getDouble("posX"), nbt.getDouble("posZ"));
					front.readNBT(nbt);
					fronts.put(uuid, front);
				}
				else
				{
					Weather2.debug("Creating a new global front: " + uuid.toString());
					globalFront = new FrontObject(this, null, 0);
					globalFront.readNBT(nbt);
					fronts.put(uuid, globalFront);
				}
				break;
			}
			case 12:
			{
				NBTTagCompound nbt = mainNBT.getCompoundTag("frontObject");
				FrontObject front = getFront(nbt.getUniqueId("uuid"));
				if(front != null)
					front.readNBT(nbt);
				break;
			}
			case 13:
			{
				UUID uuid = mainNBT.getUniqueId("frontUUID");
				FrontObject front = getFront(uuid);
				if (front != null)
					if (globalFront.equals(front))
						globalFront.reset();
					else
					{
						front.reset();
						removeFront(front);
					}
				else
					Weather2.error("error removing front, cant find by ID: " + uuid.toString());
				break;
			}
			case 14:
			{
				fronts.forEach((uuid, front) -> front.cleanupClient(false));
				weatherParticles.forEach(particle -> particle.setExpired());
				weatherParticles.clear();
				Weather2.debug("Cleaned up client particles");
				break;
			}
			default:
				Weather2.error("Server sent an invalid network packet");
		}
	}
	
	public float getRainTargetValue(Vec3 position)
	{
		float rainTarget = -Float.MAX_VALUE, rain;
		List<WeatherObject> systems = new ArrayList<WeatherObject>(this.systems.values());
		
		for (WeatherObject system : systems)
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
		List<WeatherObject> systems = new ArrayList<WeatherObject>(this.systems.values());
		
		for (WeatherObject system : systems)
		{
			if (system instanceof IWeatherStaged)
			{
				float distance = (float) Maths.distanceSq(position.posX, position.posZ, system.pos.posX, system.pos.posZ);
				float distanceMult = 1.0F - Math.min(Math.max(distance - system.size, 0.0F) / (system.size * 0.25F), 1.0F);
				float stageMult = 0.25F + Math.min(((IWeatherStaged)system).getStage() * 0.25F, 0.5F) + (system instanceof StormObject && ((StormObject)system).isViolent ? 0.25F : 0.0F);
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
		final EntityPlayerSP player = MC.player;
		if (player == null) return;
		for (WeatherObject system : this.systems.values())
			if (system.pos.distanceSq(player.posX, system.pos.posY, player.posZ) < NewSceneEnhancer.instance().renderDistance)
				systems++;
		
		if (systems == 0) systems = 1;
		
		particleLimit = ConfigClient.max_particles > 0 ? ConfigClient.max_particles / systems : Integer.MAX_VALUE;
		this.systems.forEach((uuid, system) ->
		{
			if (system instanceof StormObject && ((StormObject)system).particleRenderer != null)
				((StormObject)system).particleRenderer.refreshParticleLimit();
		});
	}
	
	public int getParticleLimit()
	{
		return particleLimit;
	}
	
	@Override
	public void reset(boolean fullReset)
	{
		super.reset(fullReset);
		effectedParticles.clear();
		closestStormCached = null;
	}

	/**Gets the target rain value based on the closest raining storm in range**/
	public float getRainTarget(Vec3 pos, float maxDistSq) {
		float rainTarget = 0.0F;
		StormObject storm = getStrongestClosestStormWithRain(pos, maxDistSq);
		if (storm != null) {
			float rain = ((IWeatherRain)storm).getDownfall(pos) - IWeatherRain.MINIMUM_DRIZZLE;
			if (rain > rainTarget) rainTarget = rain;
		}
		return Maths.clamp(rainTarget / IWeatherRain.MINIMUM_HEAVY_RAIN, 0.0F, 1.0F);
	}

	/**Gets the target overcast value based on the closest and worst storm in range**/
	public float getOvercastTarget(Vec3 pos, float maxDistSq) {
		float overcastTarget = 0.0F;
		StormObject storm = getStrongestClosestStorm(pos, maxDistSq);
		if (storm != null) {
			float distance = (float) Maths.distanceSq(pos.posX, pos.posZ, storm.pos.posX, storm.pos.posZ);
			float distanceMult = 1.0F - Math.min(Math.max(distance - storm.size, 0.0F) / (storm.size * 0.25F), 1.0F);
			float stageMult = 0.25F + Math.min(((IWeatherStaged)storm).getStage() * 0.25F, 0.5F) + (storm.isViolent ? 0.25F : 0.0F);
			float overcast = stageMult * distanceMult;
			if (overcast > overcastTarget) overcastTarget = overcast;
		}
		return overcastTarget;
	}
}
