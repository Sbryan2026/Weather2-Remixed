package net.mrbt0907.weather2.util;

import CoroUtil.api.weather.IWindHandler;
import CoroUtil.util.CoroUtilEntOrParticle;
import extendedrenderer.particle.entity.EntityRotFX;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.mrbt0907.weather2.api.WeatherUtilData;
import net.mrbt0907.weather2.client.event.ClientTickHandler;
import net.mrbt0907.weather2.entity.EntityMovingBlock;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.weather.WindManager;

public class WeatherUtilEntity {
	//old non multiplayer friendly var, needs resdesign where this is used
	public static int playerInAirTime = 0;

	/**Gets the weight of the object asked for. Returns -1.0F if the object cannot be moved*/
	public static float getWeight(Object obj)
	{
		World world = CoroUtilEntOrParticle.getWorld(obj);
		if (world == null)
			return -1.0F;

		if (obj instanceof IWindHandler)
			return ((IWindHandler) obj).getWindWeight();
		else if (world.isRemote && obj instanceof Particle)
			return WeatherUtilParticle.getParticleWeight((Particle) obj);
		else if (obj instanceof EntityMovingBlock)
		{
			EntityMovingBlock block = (EntityMovingBlock) obj;
			return 12F + (block.block.isToolEffective("axe", block.state) ? block.block.blockHardness : block.block.isToolEffective("shovel", block.state) ? block.block.blockHardness * 13 : block.block.blockHardness * 20);
		}
		else if (obj instanceof EntitySquid)
			return 400F;
		else if (obj instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) obj;
			if (player.onGround || player.handleWaterMovement())
				WeatherUtilEntity.playerInAirTime = 0;
			else
				WeatherUtilEntity.playerInAirTime++;
			
			if (player.isCreative() || player.isSpectator()) return -1.0F;
			
			float extraWeight = 0.0F;
			if (player.inventory != null)
				for (ItemStack stack : player.inventory.armorInventory)
					if (!stack.isEmpty() && stack.getMaxDamage() > 0)
						extraWeight += stack.getMaxDamage() * 0.0025F;

			return 5.0F + extraWeight + WeatherUtilEntity.playerInAirTime * 0.0025F;
		}
		else if (obj instanceof EntityLivingBase)
		{
			EntityLivingBase livingEnt = (EntityLivingBase) obj;
			int airTime = livingEnt.getEntityData().getInteger("timeInAir");
			
			if (livingEnt.onGround || livingEnt.handleWaterMovement())
				airTime = 0;
			else
				airTime++;
			
			livingEnt.getEntityData().setInteger("timeInAir", airTime);
			return 5.0F + airTime * 0.0025F;
			
		}
		else if (obj instanceof EntityBoat || obj instanceof EntityItem || obj instanceof EntityFishHook)
			return 4000F;
		else if (obj instanceof EntityMinecart)
			return 80F;
		else if (obj instanceof Entity)
		{
			Entity ent = (Entity) obj;
			if (WeatherUtilData.isWindWeightSet(ent))
				return WeatherUtilData.getWindWeight(ent);
		}

		return 1F;
	}
	
	public static boolean isParticleRotServerSafe(World world, Object obj)
	{
		return world.isRemote && WeatherUtilEntity.isParticleRotClientCheck(obj);
	}
	
	public static boolean isParticleRotClientCheck(Object obj)
	{
		return obj instanceof EntityRotFX;
	}
	
	public static boolean canPushEntity(Entity ent)
	{
		WindManager windMan = ClientTickHandler.weatherManager.windManager;
		
		double speed = 10.0D;
		int startX = (int)(ent.posX - speed * (double)(-Maths.fastSin(windMan.windAngle / 180.0F * (float)Math.PI) * Maths.fastCos(0F / 180.0F * (float)Math.PI)));
		int startZ = (int)(ent.posZ - speed * (double)(Maths.fastCos(windMan.windAngle / 180.0F * (float)Math.PI) * Maths.fastCos(0F / 180.0F * (float)Math.PI)));

		return ent.world.rayTraceBlocks((new Vec3(ent.posX, ent.posY + (double)ent.getEyeHeight(), ent.posZ)).toVec3MC(), (new Vec3(startX, ent.posY + (double)ent.getEyeHeight(), startZ)).toVec3MC()) == null;
	}
	
	public static boolean isEntityOutside(Entity parEnt) {
		return WeatherUtilEntity.isEntityOutside(parEnt, false);
	}
	
	public static boolean isEntityOutside(Entity parEnt, boolean cheapCheck) {
		return WeatherUtilEntity.isPosOutside(parEnt.world, new Vec3(parEnt.posX, parEnt.posY, parEnt.posZ), cheapCheck);
	}
	
	public static boolean isPosOutside(World parWorld, Vec3 parPos) {
		return WeatherUtilEntity.isPosOutside(parWorld, parPos, false);
	}
	
	public static boolean isPosOutside(World parWorld, Vec3 parPos, boolean cheapCheck)
	{
		int rangeCheck = 5;
		int yOffset = 1;
		
		if (WeatherUtilBlock.getPrecipitationHeightSafe(parWorld, new BlockPos(MathHelper.floor(parPos.posX), 0, MathHelper.floor(parPos.posZ))).getY() < parPos.posY+1) return true;
		
		if (cheapCheck) return false;
		
		Vec3 vecTry = new Vec3(parPos.posX + EnumFacing.NORTH.getXOffset()*rangeCheck, parPos.posY+yOffset, parPos.posZ + EnumFacing.NORTH.getZOffset()*rangeCheck);
		if (WeatherUtilEntity.checkVecOutside(parWorld, parPos, vecTry)) return true;
		
		vecTry = new Vec3(parPos.posX + EnumFacing.SOUTH.getXOffset()*rangeCheck, parPos.posY+yOffset, parPos.posZ + EnumFacing.SOUTH.getZOffset()*rangeCheck);
		if (WeatherUtilEntity.checkVecOutside(parWorld, parPos, vecTry)) return true;
		
		vecTry = new Vec3(parPos.posX + EnumFacing.EAST.getXOffset()*rangeCheck, parPos.posY+yOffset, parPos.posZ + EnumFacing.EAST.getZOffset()*rangeCheck);
		if (WeatherUtilEntity.checkVecOutside(parWorld, parPos, vecTry)) return true;
		
		vecTry = new Vec3(parPos.posX + EnumFacing.WEST.getXOffset()*rangeCheck, parPos.posY+yOffset, parPos.posZ + EnumFacing.WEST.getZOffset()*rangeCheck);
		if (WeatherUtilEntity.checkVecOutside(parWorld, parPos, vecTry)) return true;
		
		return false;
	}
	
	public static boolean checkVecOutside(World parWorld, Vec3 parPos, Vec3 parCheckPos)
	{
		return parWorld.rayTraceBlocks(parPos.toVec3MC(), parCheckPos.toVec3MC()) == null && WeatherUtilBlock.getPrecipitationHeightSafe(parWorld, new BlockPos(MathHelper.floor(parCheckPos.posX), 0, MathHelper.floor(parCheckPos.posZ))).getY() < parCheckPos.posY;
	}

	/**Gets the closest player - rewritten by Miyu, also now uses sqrtf instead of just standard sqrt**/
	public static EntityPlayer getClosestPlayer(World world, double posX, double posY, double posZ, double radius)
	{
		double min_radius = 9999;
		EntityPlayer player = null;

	    for (EntityPlayer entity : world.playerEntities)
	    {
	        double player_distance = FartsyUtil.sqrtf((float) entity.getDistanceSq(posX, posY, posZ));

	        if (player_distance <= radius && (player_distance < min_radius || player == null))
	        {
	            player = entity;
	            min_radius = player_distance;
	            //System.out.println("Closest returns " + min_radius);
	        }
	    }

	    return player;
	}
	
	public static boolean hasAITask(EntityCreature creature, Class<? extends EntityAIBase> clazz)
	{
		for (EntityAITasks.EntityAITaskEntry entry : creature.tasks.taskEntries)
			if (clazz.isAssignableFrom(entry.action.getClass()))
				return true;
		return false;
	}
}