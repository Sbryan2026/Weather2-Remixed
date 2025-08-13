package net.mrbt0907.weather2remastered.api.weather;

import java.util.UUID;

import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.mrbt0907.weather2remastered.config.ConfigStorm;
import net.mrbt0907.weather2remastered.util.CachedNBTTagCompound;
import net.mrbt0907.weather2remastered.util.Maths.Vec3;

public abstract class AbstractWeatherObject implements IWeatherDetectable
{
	private UUID id;
	public AbstractFrontObject front;
	public AbstractWeatherManager manager;
	public CachedNBTTagCompound nbt;
	public WeatherEnum.Type type = WeatherEnum.Type.CLOUD;
	public Vec3 pos = new Vec3(0, 0, 0);
	public Vec3 posGround = new Vec3(0, 0, 0);
	public Vec3 motion = new Vec3(0, 0, 0);
	public boolean isDying = false;
	public boolean isDead = false;
	public long ticks = 0L;
	public int size = ConfigStorm.min_storm_size;
	
	public AbstractWeatherLogic weatherLogic;
	
	protected World world;
	
	/**
	 * used to count up to a threshold to finally remove weather objects,
	 * solves issue of simbox cutoff removing storms for first few ticks as player is joining in singleplayer
	 * helps with multiplayer, requiring 30 seconds of no players near before removal
	 */
	public int ticksSinceNoNearPlayer = 0;

	public AbstractWeatherObject(AbstractFrontObject front) {
		this.front = front;
		manager = front.getWeatherManager();
		nbt = new CachedNBTTagCompound();
		world = front.world;
		init();
	}

	public void init() {
		id = UUID.randomUUID();
	}

	public void tick(boolean isClientTick)
	{
		ticks++;
		if (ticks < 0) ticks = 0;
	}
	
	@OnlyIn(Dist.CLIENT)
	public void tickRender(float partialTick) {}

	public void setDead()
	{
		isDead = true;
		
		//cleanup memory
		if (FMLEnvironment.dist.equals(Dist.CLIENT)) cleanupClient(true);
		cleanup();
	}

	public void reset() { setDead(); }
	public void cleanup() { manager = null; }

	@OnlyIn(Dist.CLIENT)
	public void cleanupClient(boolean wipe) {}

	public void readFromNBT()
	{
		id = nbt.getUUID("ID");
		pos = new Vec3(nbt.getDouble("posX"), nbt.getDouble("posY"), nbt.getDouble("posZ"));
		motion = new Vec3(nbt.getDouble("vecX"), nbt.getDouble("vecY"), nbt.getDouble("vecZ"));
		size = nbt.getInteger("size");
		type = WeatherEnum.Type.get(nbt.getInteger("weatherType"));
		isDying = nbt.getBoolean("isDying");
		isDead = nbt.getBoolean("isDead");
	}

	public CachedNBTTagCompound writeToNBT()
	{
		nbt.setDouble("posX", pos.posX);
		nbt.setDouble("posY", pos.posY);
		nbt.setDouble("posZ", pos.posZ);
		nbt.setDouble("vecX", motion.posX);
		nbt.setDouble("vecY", motion.posY);
		nbt.setDouble("vecZ", motion.posZ);
		nbt.setUUID("ID", id);
		nbt.setUUID("frontUUID", front.getUUID());
		//just blind set ID into non cached data so client always has it, no need to check for forced state and restore orig state
		nbt.getNewNBT().putUUID("ID", id);
		nbt.setInteger("size", size);
		nbt.setInteger("weatherType", type.ordinal());
		nbt.setBoolean("isDying", isDying);
		nbt.setBoolean("isDead", isDead);
		return nbt;
	}

	public int getNetRate() { return 40; }
	public boolean isDangerous() { return type.isDangerous(); }
	public UUID getUUID() { return id; }

	@Override
	public Vec3 getPos() { return pos; }

	@Override
	public boolean isDying() { return isDying; }
}