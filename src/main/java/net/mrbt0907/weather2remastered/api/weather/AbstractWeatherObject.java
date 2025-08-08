package net.mrbt0907.weather2remastered.api.weather;

import java.util.UUID;

import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.mrbt0907.weather2remastered.config.ConfigStorm;
import net.mrbt0907.weather2remastered.util.CachedNBTTagCompound;
import net.mrbt0907.weather2remastered.util.Maths.Vec3;

public class AbstractWeatherObject implements IWeatherDetectable
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
	public int ticksSinceNoNearPlayer = 0;
	public AbstractWeatherLogic weatherLogic;
	
	protected World world;
	public AbstractWeatherObject(AbstractFrontObject front) {
		// TODO Auto-generated constructor stub
	}

	public void tick()
	{
		
	}

	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public float getWindSpeed() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getStage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setStage(int stage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTypeName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UUID getUUID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vec3 getPos() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float getAngle() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getSpeed() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isDying() {
		// TODO Auto-generated method stub
		return false;
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

	public void readFromNBT() {
		// TODO Auto-generated method stub
		
	}

	public void tickRender(float partialTick) {
		// TODO Auto-generated method stub
		
	}
	
	public void setDead()
	{
		isDead = true;
		
		//cleanup memory
		if (FMLEnvironment.dist.equals(Dist.CLIENT)) 
			cleanupClient(true);
		
		cleanup();
	}
	public void cleanup() {manager = null;}
	@OnlyIn(Dist.CLIENT)
	public void cleanupClient(boolean wipe) {}
	public void reset() {
		setDead();
		
	}

	public int getNetRate() {return 40;}
}