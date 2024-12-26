package net.mrbt0907.weather2.entity.AI;

import CoroUtil.ai.ITaskInitializer;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.Village;
import net.minecraft.village.VillageDoorInfo;
import net.mrbt0907.weather2.api.weather.WeatherEnum;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Stage;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.event.ServerTickHandler;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.weather.WeatherManager;

/**
 * Based off of EntityAIMoveIndoors
 *
 * If global overcast is on, this probably isnt needed as original task executes on global rain active
 *
 * Inject with same priority as original task, do not override original task
 */
public class EntityAIMoveIndoorsStorm extends EntityAIBase implements ITaskInitializer
{
	private EntityCreature entity;
	protected PathNavigate navigator;
	private VillageDoorInfo doorInfo;
	public boolean isAlert = false;
	private int insidePosX = -1;
	private int insidePosZ = -1;

	public EntityAIMoveIndoorsStorm(EntityCreature entity)
	{
		setMutexBits(1);
		setEntity(entity);
	}

	@Override
	public boolean shouldExecute()
	{
		WeatherManager weatherManager = ServerTickHandler.getWeatherSystemForDim(entity.world.provider.getDimension());
		if (weatherManager == null) return false;

		BlockPos blockpos = entity.getPosition();
		Vec3 pos = new Vec3(blockpos);
		boolean runInside = isAlert || weatherManager.getWorstWeather(pos, ConfigStorm.villager_detection_range, Stage.SEVERE.getStage(), Integer.MAX_VALUE, WeatherEnum.Type.CLOUD) != null;

		if (runInside)
			//if villager is right next to its safe spot, cancel
			if (insidePosX != -1 && entity.getDistanceSq((double)insidePosX, this.entity.posY, (double)insidePosZ) < 4.0D)
				return false;
			else
			{
				Village village = entity.world.getVillageCollection().getNearestVillage(blockpos, 14);

				if (village == null)
					return false;
				else
				{
					doorInfo = village.getDoorInfo(blockpos);
					return doorInfo != null;
				}
			}
		else
			return false;
	}

	@Override
	public boolean shouldContinueExecuting()
	{
		return !navigator.noPath();
	}

	@Override
	public void startExecuting()
	{
		insidePosX = -1;
		BlockPos blockpos = this.doorInfo.getInsideBlockPos();
		int i = blockpos.getX();
		int j = blockpos.getY();
		int k = blockpos.getZ();
	
		if (entity.getDistanceSq(blockpos) > 256.0D)
		{
			Vec3d vec3d = RandomPositionGenerator.findRandomTargetBlockTowards(this.entity, 14, 3, new Vec3d((double)i + 0.5D, (double)j, (double)k + 0.5D));
	
			if (vec3d != null)
				navigator.tryMoveToXYZ(vec3d.x, vec3d.y, vec3d.z, 1.0D);
		}
		else
			navigator.tryMoveToXYZ((double)i + 0.5D, (double)j, (double)k + 0.5D, 1.0D);
	}

	@Override
	public void resetTask()
	{
		insidePosX = doorInfo.getInsideBlockPos().getX();
		insidePosZ = doorInfo.getInsideBlockPos().getZ();
		doorInfo = null;
		isAlert = false;
	}

	@Override
	public void setEntity(EntityCreature entity)
	{
		this.entity = entity;
		navigator = entity.getNavigator();
	}
}