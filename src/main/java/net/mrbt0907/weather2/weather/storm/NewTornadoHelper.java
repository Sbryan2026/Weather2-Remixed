package net.mrbt0907.weather2.weather.storm;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Random;

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
import net.minecraft.util.math.BlockPos.MutableBlockPos;
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

	private static final class PickupTarget
	{
		private final BlockPos pos;
		private final IBlockState state;
		private final String id;
		private final String metaID;

		private PickupTarget(BlockPos pos, IBlockState state, String id, String metaID)
		{
			this.pos = pos;
			this.state = state;
			this.id = id;
			this.metaID = metaID;
		}
	}
	
	public static void tick(StormObject storm, World world)
	{
		if (storm == null || world == null) return;

		float size = NewTornadoHelper.getTornadoBaseSize(storm), radius = size * 0.5F;
		NewTornadoHelper.forceRotate(storm, world, size * 0.85F + 32.0F);
		
		if (!world.isRemote)
		{
			if (ConfigGrab.grab_blocks && world.getTotalWorldTime() % ConfigGrab.grab_process_delay == 0L)
			{
				processBlockPickupQueue(storm, world, radius);
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

	private static void processBlockPickupQueue(StormObject storm, World world, float radius)
	{
		int maxGrabbed = Math.max(0, ConfigGrab.max_grabbed_blocks);
		int maxReplaced = Math.max(0, ConfigGrab.max_replaced_blocks);
		int loopAmount = maxGrabbed + maxReplaced;
		if (loopAmount <= 0)
			return;

		int maxHeight = Math.min(world.getActualHeight(), (int) storm.pos.posY);
		int grabbed = 0;
		int replaced = 0;
		int attempts = 0;
		int maxAttempts = Math.max(loopAmount * 3, 8);
		int baseX = MathHelper.floor(storm.pos_funnel_base.posX);
		int baseZ = MathHelper.floor(storm.pos_funnel_base.posZ);
		int intRadius = Math.max(1, MathHelper.ceil(radius));
		int maxFlyingBlocks = ConfigGrab.max_flying_blocks < 0 ? Integer.MAX_VALUE : ConfigGrab.max_flying_blocks;
		boolean enableGrabList = ConfigGrab.enable_grab_list;
		boolean enableReplaceList = ConfigGrab.enable_replace_list;
		boolean grabStrengthMatch = ConfigGrab.grab_list_strength_match;
		boolean replaceStrengthMatch = ConfigGrab.replace_list_strength_match;
		boolean enableRepairMode = ConfigGrab.enable_repair_block_mode;
		boolean enableListSharing = ConfigGrab.enable_list_sharing;
		ConfigList grabList = enableGrabList ? WeatherAPI.getGrabList() : null;
		ConfigList replaceList = enableReplaceList ? WeatherAPI.getReplaceList() : null;
		Random rand = world.rand;
		Set<Long> sampledColumns = new HashSet<Long>((maxAttempts * 4 / 3) + 1);
		MutableBlockPos samplePos = new MutableBlockPos();

		while (attempts++ < maxAttempts && (grabbed < maxGrabbed || replaced < maxReplaced))
		{
			int x = baseX + rand.nextInt(intRadius * 2 + 1) - intRadius;
			int z = baseZ + rand.nextInt(intRadius * 2 + 1) - intRadius;
			long key = (((long) x) << 32) ^ (z & 0xFFFFFFFFL);
			if (!sampledColumns.add(key))
				continue;

			samplePos.setPos(x, maxHeight, z);
			if (!world.isBlockLoaded(samplePos))
				continue;

			BlockPos blockPos = WeatherUtilBlock.getHeightSafe(world, samplePos).down();
			if (blockPos.getY() < 0)
				continue;

			PickupTarget target = resolvePickupTarget(storm, world, blockPos);
			if (target == null)
				continue;

			if (grabbed < maxGrabbed && grabBlock(storm, world, target, grabList, maxFlyingBlocks, enableGrabList, enableReplaceList, grabStrengthMatch, enableRepairMode, enableListSharing, rand))
			{
				grabbed++;
				continue;
			}

			if (replaced < maxReplaced && replaceBlock(storm, world, target, replaceList, enableReplaceList, replaceStrengthMatch))
				replaced++;
		}
	}
	
	public static void forceRotate(StormObject storm, World world, float size)
	{
		List<Entity> entities = world.loadedEntityList;
		for (int i = 0; i < entities.size(); i++)
		{
			Entity entity = entities.get(i);
			if (NewTornadoHelper.canGrabEntity(entity) && Maths.distanceSq(storm.pos_funnel_base.posX, storm.pos_funnel_base.posZ, entity.posX, entity.posZ) < size)
			{
				if (entity instanceof EntityMovingBlock && !((EntityMovingBlock)entity).collideFalling || WeatherUtilEntity.isEntityOutside(entity, !(entity instanceof EntityPlayer)))
					storm.spinEntity(entity);
			}
		}
	}
	
	public static boolean grabBlock(StormObject storm, World world, BlockPos pos)
	{
		ConfigList grabList = ConfigGrab.enable_grab_list ? WeatherAPI.getGrabList() : null;
		PickupTarget target = resolvePickupTarget(storm, world, pos);
		if (target == null)
			return false;
		return grabBlock(storm, world, target, grabList, ConfigGrab.max_flying_blocks < 0 ? Integer.MAX_VALUE : ConfigGrab.max_flying_blocks, ConfigGrab.enable_grab_list, ConfigGrab.enable_replace_list, ConfigGrab.grab_list_strength_match, ConfigGrab.enable_repair_block_mode, ConfigGrab.enable_list_sharing, world.rand);
	}

	private static boolean grabBlock(StormObject storm, World world, PickupTarget target, ConfigList grabList, int maxFlyingBlocks, boolean enableGrabList, boolean enableReplaceList, boolean grabStrengthMatch, boolean enableRepairMode, boolean enableListSharing, Random rand)
	{
		if (storm.flyingBlocks >= maxFlyingBlocks || enableListSharing && rand.nextBoolean())
			return false;
		
		boolean canGrab = enableGrabList
			? matchesConfiguredList(grabList, target.id, target.metaID)
			: !enableReplaceList;

		if (!canGrab)
			return false;

		if (grabStrengthMatch && !matchesStormResistance(storm, target.id, target.metaID))
			return false;
		
		if (enableRepairMode)
		{
			if (target.state != NewTornadoHelper.AIR && UtilMining.canConvertToRepairingBlockNew(world, target.pos, false))
			{
				TileEntityRepairingBlock.replaceBlockAndBackup(world, target.pos, ConfigGrab.Storm_Tornado_TicksToRepairBlock);
				return true;
			}
		}
		else
		{
			EntityMovingBlock entity = new EntityMovingBlock(world, target.pos.getX(), target.pos.getY(), target.pos.getZ(), target.state, storm);
			entity.motionX += (rand.nextDouble() - rand.nextDouble()) * 1.0D;
			entity.motionZ += (rand.nextDouble() - rand.nextDouble()) * 1.0D;
			entity.motionY = 1.0D;
			if (target.state.getBlock().hasTileEntity(target.state))
			{
				TileEntity tile = world.getTileEntity(target.pos);
				if (tile instanceof IInventory)
					((IInventory) tile).clear();
			}

			ChunkUtils.setBlockState(world, target.pos, NewTornadoHelper.AIR);
			world.spawnEntity(entity);
			storm.flyingBlocks++;
			return true;
		}
		
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public static boolean replaceBlock(StormObject storm, World world, BlockPos pos)
	{
		ConfigList replaceList = ConfigGrab.enable_replace_list ? WeatherAPI.getReplaceList() : null;
		PickupTarget target = resolvePickupTarget(storm, world, pos);
		if (target == null)
			return false;
		return replaceBlock(storm, world, target, replaceList, ConfigGrab.enable_replace_list, ConfigGrab.replace_list_strength_match);
	}

	@SuppressWarnings("deprecation")
	private static boolean replaceBlock(StormObject storm, World world, PickupTarget target, ConfigList replaceList, boolean enableReplaceList, boolean replaceStrengthMatch)
	{
		if (!enableReplaceList || replaceList == null)
			return false;

		if (!matchesConfiguredList(replaceList, target.id, target.metaID))
			return false;

		if (replaceStrengthMatch && !matchesStormResistance(storm, target.id, target.metaID))
			return false;

		Object[] replacements = replaceList.getValues(target.id);
		if (replacements == null)
			replacements = replaceList.getValues(target.metaID);
		
		if (replacements == null || replacements.length == 0)
			return false;

		String replacement = (String) replacements[Maths.random(0, replacements.length - 1)];
		int metadata = 0;
		int metadataIndex = replacement.indexOf('#');
		if (metadataIndex >= 0)
		{
			metadata = parseMetaOrZero(replacement, metadataIndex + 1);
			replacement = replacement.substring(0, metadataIndex);
		}

		Block replacementBlock = Block.getBlockFromName(replacement);
		if (replacementBlock == null)
			return false;

		ChunkUtils.setBlockState(world, target.pos, replacementBlock.getStateFromMeta(metadata));
		return true;
	}
	
	private static PickupTarget resolvePickupTarget(StormObject storm, World world, BlockPos pos)
	{
		IBlockState state = world.getBlockState(pos);
		if (!WeatherUtilBlock.canGrabBlock(storm, pos, state))
			return null;

		Block block = state.getBlock();
		if (block.getRegistryName() == null)
			return null;

		String id = block.getRegistryName().toString();
		String metaID = id + "#" + block.getMetaFromState(state);
		return new PickupTarget(pos, state, id, metaID);
	}

	private static boolean matchesConfiguredList(ConfigList list, String id, String metaID)
	{
		return list != null && (list.exists(metaID) || list.exists(id));
	}

	private static boolean matchesStormResistance(StormObject storm, String id, String metaID)
	{
		return WeatherUtilBlock.checkResistance(storm, metaID) || WeatherUtilBlock.checkResistance(storm, id);
	}

	private static int parseMetaOrZero(String value, int fromIndex)
	{
		if (fromIndex >= value.length())
			return 0;

		int result = 0;
		for (int i = fromIndex; i < value.length(); i++)
		{
			char c = value.charAt(i);
			if (c < '0' || c > '9')
				return 0;
			result = result * 10 + (c - '0');
		}
		return result;
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
