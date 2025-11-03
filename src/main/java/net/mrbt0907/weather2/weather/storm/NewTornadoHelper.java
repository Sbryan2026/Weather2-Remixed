package net.mrbt0907.weather2.weather.storm;

import java.util.ArrayList;

import CoroUtil.block.TileEntityRepairingBlock;
import CoroUtil.util.CoroUtilBlock;
import CoroUtil.util.UtilMining;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.INpc;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.mrbt0907.weather2.api.WeatherAPI;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Stage;
import net.mrbt0907.weather2.config.ConfigGrab;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.entity.EntityMovingBlock;
import net.mrbt0907.weather2.util.ChunkUtils;
import net.mrbt0907.weather2.util.ConfigList;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.WeatherUtilBlock;
import net.mrbt0907.weather2.util.WeatherUtilEntity;
import net.mrbt0907.weather2.weather.storm.StormObject.StormType;

public class NewTornadoHelper
{
	private static final IBlockState AIR = Blocks.AIR.getDefaultState();
	private static final IBlockState FIRE = Blocks.FIRE.getDefaultState();
	
	public static void tick(StormObject storm, World world)
	{
		if (storm == null || world == null) return;

		float size = NewTornadoHelper.getTornadoBaseSize(storm), radius = size * 0.5F;
		NewTornadoHelper.forceRotate(storm, world, size * 0.85F + 32.0F);
		
		if (!world.isRemote)
		{
			// Grab and Replace block queue
			if (ConfigGrab.grab_blocks && world.getTotalWorldTime() % ConfigGrab.grab_process_delay == 0L)
			{
				//BlockPos funnelPosA = new BlockPos(storm.pos_funnel_base.posX + radius, storm.pos_funnel_base.posY, storm.pos_funnel_base.posZ + radius);
				//BlockPos funnelPosB = new BlockPos(storm.pos_funnel_base.posX - radius, storm.pos_funnel_base.posY, storm.pos_funnel_base.posZ - radius);
				//ChunkCache cache = new ChunkCache(world, funnelPosA, funnelPosB, 0);
				int maxHeight = Math.min(world.getActualHeight(), (int) storm.pos.posY);
				int loopAmount = ConfigGrab.max_replaced_blocks + ConfigGrab.max_grabbed_blocks;
				int x, z, grabbed = 0, replaced = 0;
				BlockPos blockPos;
				
				for (int i = 0; i < loopAmount; i++)
				{
					x = (int) (storm.pos_funnel_base.posX + Maths.random(-radius, radius));
					z = (int) (storm.pos_funnel_base.posZ + Maths.random(-radius, radius));
					
					
					blockPos = new BlockPos(x, maxHeight, z);
					blockPos = WeatherUtilBlock.getHeightSafe(world, blockPos).down();
					
					if (grabbed <= ConfigGrab.max_grabbed_blocks && NewTornadoHelper.grabBlock(storm, world, blockPos))
						grabbed++;
					else if (replaced <= ConfigGrab.max_replaced_blocks && NewTornadoHelper.replaceBlock(storm, world, blockPos))
						replaced++;
					
				}
			}
			
			//Firenado
			if (storm.isFirenado)
			{
				if (storm.stage >= Stage.TORNADO.getStage() + 1)
				for (int i = 0; i < 1; i++)
				{
					BlockPos posUp = new BlockPos(storm.posGround.posX, storm.posGround.posY + Maths.random(30), storm.posGround.posZ);
					IBlockState state = ChunkUtils.getBlockState(world, posUp);
					if (CoroUtilBlock.isAir(state.getBlock())) {
						EntityMovingBlock mBlock = new EntityMovingBlock(world, posUp.getX(), posUp.getY(), posUp.getZ(), NewTornadoHelper.FIRE, storm);
						mBlock.metadata = 15;
						double speed = 2D;
						mBlock.motionX += (Maths.random(1.0D) - Maths.random(1.0D)) * speed;
						mBlock.motionZ += (Maths.random(1.0D) - Maths.random(1.0D)) * speed;
						mBlock.motionY = 1D;
						mBlock.mode = 0;
						world.spawnEntity(mBlock);
					}
				}

				int randSize = 10;
				int tryX = (int)storm.pos.posX + Maths.random(randSize) - randSize/2;
				int tryZ = (int)storm.pos.posZ + Maths.random(randSize) - randSize/2;
				int tryY = world.getHeight(tryX, tryZ) - 1;
				double d0 = storm.pos.posX - tryX;
				double d2 = storm.pos.posZ - tryZ;
				double dist = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);

				if (dist < size/2 + randSize/2)
				{
					BlockPos pos = new BlockPos(tryX, tryY, tryZ);
					Block block = ChunkUtils.getBlockState(world, pos).getBlock();
					BlockPos posUp = new BlockPos(tryX, tryY+1, tryZ);
					Block blockUp = ChunkUtils.getBlockState(world, posUp).getBlock();

					if (!CoroUtilBlock.isAir(block) && CoroUtilBlock.isAir(blockUp))
						ChunkUtils.setBlockState(world, posUp, NewTornadoHelper.FIRE);
				}
			}
		}
	}
	
	public static void forceRotate(StormObject storm, World world, float size)
	{
		new ArrayList<Entity>(world.loadedEntityList).forEach(entity -> {
			if (NewTornadoHelper.canGrabEntity(entity) && Maths.distanceSq(storm.pos_funnel_base.posX, storm.pos_funnel_base.posZ, entity.posX, entity.posZ) < size && entity.posY < storm.pos.posY)
			{
				if (entity instanceof EntityMovingBlock && !((EntityMovingBlock)entity).collideFalling || WeatherUtilEntity.isEntityOutside(entity, !(entity instanceof EntityPlayer)))
					storm.spinEntity(entity);
			}
		});
	}
	
	public static boolean grabBlock(StormObject storm, World world, BlockPos pos)
	{
		if (storm.flyingBlocks >= (ConfigGrab.max_flying_blocks < 0 ? Integer.MAX_VALUE : ConfigGrab.max_flying_blocks) || ConfigGrab.enable_list_sharing && Maths.chance(50))
			return false;
		
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		if (!WeatherUtilBlock.canGrabBlock(storm, pos, state))
			return false;
		
		String id = block.getRegistryName().toString();
		String metaID = id + "#" + state.getBlock().getMetaFromState(state);
		
		if (ConfigGrab.enable_grab_list ? (WeatherAPI.getGrabList().exists(metaID) || WeatherAPI.getGrabList().exists(id)) : !ConfigGrab.enable_replace_list)
		{
			if (ConfigGrab.grab_list_strength_match ? !(WeatherUtilBlock.checkResistance(storm, metaID) || WeatherUtilBlock.checkResistance(storm, id)) : false)
				return false;
			
			if (ConfigGrab.enable_repair_block_mode)
			{
				if (state != NewTornadoHelper.AIR && UtilMining.canConvertToRepairingBlockNew(world, pos, false))
				{
					TileEntityRepairingBlock.replaceBlockAndBackup(world, pos, ConfigGrab.Storm_Tornado_TicksToRepairBlock);
					return true;
				}
			}
			else
			{
				EntityMovingBlock entity = new EntityMovingBlock(world, pos.getX(), pos.getY(), pos.getZ(), state, storm);
				entity.motionX += (world.rand.nextDouble() - world.rand.nextDouble()) * 1.0D;
				entity.motionZ += (world.rand.nextDouble() - world.rand.nextDouble()) * 1.0D;
				entity.motionY = 1.0D;
				if (state.getBlock().hasTileEntity(state))
				{
					TileEntity tile = world.getTileEntity(pos);
					if (tile != null && tile instanceof IInventory)
						((IInventory)tile).clear();
				}

				ChunkUtils.setBlockState(world, pos, NewTornadoHelper.AIR);
				world.spawnEntity(entity);
				storm.flyingBlocks++;
				return true;
			}
		}
		
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public static boolean replaceBlock(StormObject storm, World world, BlockPos pos)
	{
		IBlockState state = world.getBlockState(pos);
		if (!WeatherUtilBlock.canGrabBlock(storm, pos, state))
			return false;
		
		String id = state.getBlock().getRegistryName().toString();
		String metaID = id + "#" + state.getBlock().getMetaFromState(state);
		ConfigList replaceList = WeatherAPI.getReplaceList();

		if (ConfigGrab.enable_replace_list ? (replaceList.exists(metaID) || replaceList.exists(id)) : false)
		{
			if (ConfigGrab.replace_list_strength_match ? !(WeatherUtilBlock.checkResistance(storm, metaID) || WeatherUtilBlock.checkResistance(storm, id)) : false)
				return false;

			Object[] replacements = replaceList.getValues(id);
			if (replacements == null)
				replacements = replaceList.getValues(metaID);
			
			if (replacements != null && replacements.length > 0)
			{
				String replacement = (String) replacements[Maths.random(0, replacements.length - 1)];
				int metadata = 0;
				if (replacement.contains("#"))
				{
					try {metadata = Integer.parseInt(replacement.replaceAll(".*\\#", ""));}
        			catch (Exception e) {metadata = 0;}
					replacement = replacement.replaceAll("\\#.*", "");
				}
				Block block = Block.getBlockFromName(replacement);
				ChunkUtils.setBlockState(world, pos, block.getStateFromMeta(metadata));
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean canGrabEntity(Entity entity)
	{
		if (entity == null) return false;	
		
		EntityEntry entry = EntityRegistry.getEntry(entity.getClass());
		if (entry != null && WeatherAPI.getEntityGrabList().containsKey(entry.getRegistryName().toString()))
			return false;
		if (entity instanceof EntityPlayer)
			return ConfigGrab.grab_players;
		if (entity instanceof INpc)
			return ConfigGrab.grab_villagers;
		if (entity instanceof EntityItem)
			return ConfigGrab.grab_items;
		if (entity instanceof IMob)
			return ConfigGrab.grab_mobs;
		if (entity instanceof EntityAnimal)
			return ConfigGrab.grab_animals;
		
		//for moving blocks, other non livings
		return true;
	}
	
	public static float getTornadoBaseSize(StormObject storm)
	{
		if (storm.stage > Stage.TORNADO.getStage() || storm.stormType == StormType.WATER.ordinal())
			return (float) Math.min(storm.funnelSize * 1.15F, ConfigStorm.max_storm_damage_size);
		else
			return 14.0F;
	}
}