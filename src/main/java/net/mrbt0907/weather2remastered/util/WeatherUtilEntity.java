package net.mrbt0907.weather2remastered.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.minecart.MinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.mrbt0907.weather2remastered.api.weather.AbstractWindManager;
import net.mrbt0907.weather2remastered.client.ClientTickHandler;
import net.mrbt0907.weather2remastered.config.ConfigSimulation;
import net.mrbt0907.weather2remastered.config.ConfigStorm;
import net.mrbt0907.weather2remastered.util.Maths.Vec3;
import net.mrbt0907.weather2remastered.util.coro.CoroEntParticle;
import net.mrbt0907.weather2remastered.util.fartsy.FartsyUtil;

public class WeatherUtilEntity {
	//old non multiplayer friendly var, needs resdesign where this is used
	public static int playerInAirTime = 0;

	/**Gets the weight of the object asked for. Returns -1.0F if the object cannot be moved*/
	public static float getWeight(Object entity)
	{
		World world = CoroEntParticle.getWorld(entity);
		if (world == null)
			return -1.0F;

		/*if (entity instanceof IWindHandler)
			return ((IWindHandler) entity).getWindWeight();
		else if (world.isRemote && entity instanceof Particle)
			return WeatherUtilParticle.getParticleWeight((Particle) entity);
		else if (entity instanceof EntityMovingBlock)
			return 7.5F + ((EntityMovingBlock) entity).age * 0.05F;
		else if (entity instanceof EntitySquid)
			return 400F;
		else*/ 
		if (entity instanceof PlayerEntity)
		{
			PlayerEntity player = (PlayerEntity) entity;
			if (player.isOnGround() || player.isSwimming())
				playerInAirTime = 0;
			else
				playerInAirTime++;
			
			if (player.isCreative() || player.isSpectator()) return -1.0F;
			
			float extraWeight = 0.0F;
			if (player.inventory != null)
				for (ItemStack stack : player.inventory.armor)
					if (!stack.isEmpty() && stack.getMaxDamage() > 0)
						extraWeight += stack.getMaxDamage() * 0.0025F;

			return 5.0F + extraWeight + playerInAirTime * 0.0025F;
		}
		else if (entity instanceof LivingEntity)
		{
			LivingEntity livingEnt = (LivingEntity) entity;
			int airTime = livingEnt.getPersistentData().getInt("timeInAir");
			
			if (livingEnt.isOnGround() || livingEnt.isSwimming())
				airTime = 0;
			else
				airTime++;
			
			livingEnt.getPersistentData().putInt("timeInAir", airTime);
			return 5.0F + airTime * 0.0025F;
			
		}
		else if (entity instanceof BoatEntity || entity instanceof ItemEntity || entity instanceof FishingBobberEntity)
			return 4000F;
		else if (entity instanceof MinecartEntity)
			return 80F;
		else if (entity instanceof Entity)
		{
			Entity ent = (Entity) entity;
			if (WeatherUtilData.isWindWeightSet(ent))
				return WeatherUtilData.getWindWeight(ent);
		}

		return 1F;
	}
	
	public static boolean isParticleRotServerSafe(World world, Object obj)
	{
		return world.isClientSide() && isParticleRotClientCheck(obj);
	}
	
	public static boolean isParticleRotClientCheck(Object obj)
	{
		System.out.println("Fartsy says no using isParticleRotClientCheck!");
		//return obj instanceof EntityRotFX;
		return false;
	}
	
	public static boolean canPushEntity(Entity ent)
	{
		AbstractWindManager windMan = ClientTickHandler.weatherManager.windManager;
		
		double speed = 10.0D;
		int startX = (int)(ent.getX() - speed * (double)(-Maths.fastSin(windMan.windAngle / 180.0F * (float)Math.PI) * Maths.fastCos(0F / 180.0F * (float)Math.PI)));
		int startZ = (int)(ent.getZ() - speed * (double)(Maths.fastCos(windMan.windAngle / 180.0F * (float)Math.PI) * Maths.fastCos(0F / 180.0F * (float)Math.PI)));

		//return ent.world.rayTraceBlocks((new Vec3(ent.posX, ent.posY + (double)ent.getEyeHeight(), ent.posZ)).toVec3MC(), (new Vec3(startX, ent.posY + (double)ent.getEyeHeight(), startZ)).toVec3MC()) == null;
		System.out.println("Not running rayTraceBlocks: not implemented");
		return false;
	}
	
	public static boolean isEntityOutside(Entity parEnt) {
		return isEntityOutside(parEnt, false);
	}
	
	public static boolean isEntityOutside(Entity parEnt, boolean cheapCheck) {
		return isPosOutside(parEnt.level, new Vec3(parEnt.getX(), parEnt.getY(), parEnt.getZ()), cheapCheck);
	}
	
	public static boolean isPosOutside(World parWorld, Vec3 parPos) {
		return isPosOutside(parWorld, parPos, false);
	}
	
	public static boolean isPosOutside(World parWorld, Vec3 parPos, boolean cheapCheck)
	{
		int rangeCheck = 5;
		int yOffset = 1;
		
		if (WeatherUtilBlock.getPrecipitationHeightSafe(parWorld, new BlockPos(MathHelper.floor(parPos.posX), 0, MathHelper.floor(parPos.posZ))).getY() < parPos.posY+1) return true;
		
		if (cheapCheck) return false;
		
		Vec3 vecTry = new Vec3(parPos.posX + Direction.NORTH.getStepX()*rangeCheck, parPos.posY+yOffset, parPos.posZ + Direction.NORTH.getStepZ()*rangeCheck);
		if (checkVecOutside(parWorld, parPos, vecTry)) return true;
		
		vecTry = new Vec3(parPos.posX + Direction.SOUTH.getStepX()*rangeCheck, parPos.posY+yOffset, parPos.posZ + Direction.SOUTH.getStepZ()*rangeCheck);
		if (checkVecOutside(parWorld, parPos, vecTry)) return true;
		
		vecTry = new Vec3(parPos.posX + Direction.EAST.getStepX()*rangeCheck, parPos.posY+yOffset, parPos.posZ + Direction.EAST.getStepZ()*rangeCheck);
		if (checkVecOutside(parWorld, parPos, vecTry)) return true;
		
		vecTry = new Vec3(parPos.posX + Direction.WEST.getStepX()*rangeCheck, parPos.posY+yOffset, parPos.posZ + Direction.WEST.getStepZ()*rangeCheck);
		if (checkVecOutside(parWorld, parPos, vecTry)) return true;
		
		return false;
	}
	
	public static boolean checkVecOutside(World parWorld, Vec3 parPos, Vec3 parCheckPos)
	{
		System.out.println("Not running checkVecOutside: not implemented");
		return true;
	//	return parWorld.rayTraceBlocks(parPos.toVec3MC(), parCheckPos.toVec3MC()) == null && WeatherUtilBlock.getPrecipitationHeightSafe(parWorld, new BlockPos(MathHelper.floor(parCheckPos.posX), 0, MathHelper.floor(parCheckPos.posZ))).getY() < parCheckPos.posY;
	}

	/**Gets the closest player - rewritten by Miyu, also now uses sqrtf instead of just standard sqrt**/
	public static PlayerEntity getClosestPlayer(World world, double posX, double posY, double posZ, double radius)
	{
		double min_radius = 9999;
	    PlayerEntity player = null;
	    
	    for (PlayerEntity entity : world.players())
	    {
	    	//System.out.println("Checking distance");
	        double player_distance = FartsyUtil.sqrtf((float) entity.distanceToSqr(posX, posY, posZ));
	        
	        if (player_distance <= radius && (player_distance < min_radius || player == null))
	        {
	            player = entity;
	            min_radius = player_distance;
//	            System.out.println("Closest returns " + player_distance);
	        }
	    }

	    return player;
	}
/* Fix this	
	public static boolean hasAITask(EntityCreature creature, Class<? extends EntityAIBase> clazz)
	{
		for (EntityAITasks.EntityAITaskEntry entry : creature.tasks.taskEntries)
			if (clazz.isAssignableFrom(entry.action.getClass()))
				return true;
		return false;
	}
	*/

	public static String getName(Entity ent) {
		return ent != null ? ent.getDisplayName().toString() : "nullObject";
	}
}
