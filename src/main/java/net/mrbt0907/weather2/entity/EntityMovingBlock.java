package net.mrbt0907.weather2.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.mrbt0907.weather2.api.WeatherDamageSource;
import net.mrbt0907.weather2.config.ConfigGrab;
import net.mrbt0907.weather2.util.ChunkUtils;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.WeatherUtilBlock;
import net.mrbt0907.weather2.weather.storm.StormObject;

public class EntityMovingBlock extends Entity implements IEntityAdditionalSpawnData
{	
	private static final IBlockState AIR = Blocks.AIR.getDefaultState();
	protected static final Map<Integer, List<Entity>> loadedEntities = new HashMap<>();
	public Block block;
	public IBlockState state;
	public Class<? extends TileEntity> tileClass;
	public NBTTagCompound tileEntityNBT;
	public Material material;
	public StormObject storm;
	public int metadata;
	/**Mode 0: Conduct Collision Checks, Mode 1: No Collision Checks*/
	public int mode;
	public int age;
	public boolean noCollision;
	public boolean collideFalling = false;
	public double vecX;
	public double vecY;
	public double vecZ;
	public int gravityDelay;
	public EntityMovingBlock(World world)
	{
		this(world, 0, 0, 0, EntityMovingBlock.AIR, null);
		noCollision = true;
	}

	public EntityMovingBlock(World world, int x, int y, int z, IBlockState state, StormObject storm)
	{
		
		super(world);
		this.state = state;
		block = state.getBlock();
		metadata = state.getBlock().getMetaFromState(state);
		material = state.getMaterial();
		this.storm = storm;
		if (block.hasTileEntity(state))
		{
			TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
			if (tile != null)
			{
				tileClass = tile.getClass();
				tileEntityNBT = tile.writeToNBT(new NBTTagCompound());
			}
			else
			{
				tileClass = null;
				tileEntityNBT = null;
			}
		}
		mode = 1;
		age = 0;
		noCollision = false;
		gravityDelay = 60;
		setSize(0.9F, 0.9F);
		setPosition(x + 0.5D, y + 0.5D, z + 0.5D);
		motionX = 0.0D;
		motionY = 0.0D;
		motionZ = 0.0D;
		prevPosX = (x + 0.5F);
		prevPosY = (y + 0.5F);
		prevPosZ = (double)(z + 0.5F);
		isImmuneToFire = material.getCanBurn();
	}

	@Override
	public boolean isInRangeToRenderDist(double var1)
	{
		return var1 < 256D * 256D;
	}

	@Override
	public boolean canTriggerWalking() {return false;}

	@Override
	public void entityInit() {}

	@Override
	public boolean canBePushed(){return !this.isDead;}
	
	@Override
	public boolean canBeCollidedWith()
	{
		return !this.isDead && !this.noCollision;
	}

	/**Because this causes major slowdowns on servers with many flying blocks, parse it once every server tick*/
	public static void updateEntities(World world)
	{
		EntityMovingBlock.loadedEntities.compute(world.provider.getDimension(), (dimension, loaded) -> (dimension == world.provider.getDimension() ? world.loadedEntityList.stream().filter(entity -> !(entity instanceof EntityMovingBlock) && entity.canBeCollidedWith() && entity.hurtResistantTime == 0).collect(Collectors.toList()) : loaded));
	}

	public static void resetEntities()
	{
		EntityMovingBlock.loadedEntities.clear();
	}

	@Override
	public void onUpdate()
	{
		if (block.equals(Blocks.AIR))
		{
			setDead();
		}

		motionY -= 0.05000000074505806D;
		motionX *= (double)0.98D;
		motionZ *= (double)0.98D;

		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;
		posX += motionX;
		posY += motionY;
		posZ += motionZ;
		setPosition(this.posX, this.posY, this.posZ);

		++age; vecX++; vecY++; vecZ++;
		if (posY < -64.0D)
            outOfWorld();
		if (age > gravityDelay)
		{
			mode = 0;
			if (!world.isRemote && tileEntityNBT == null && (ConfigGrab.Storm_Tornado_rarityOfDisintegrate < 0 || rand.nextInt((ConfigGrab.Storm_Tornado_rarityOfDisintegrate + 1) * 20) == 0))
				setDead();
		}
		if (mode == 1)
		{
			fallDistance = 0.0F;
			collidedHorizontally = false;
		}

		if(!world.isRemote)
		{
			BlockPos pos = getPosition();
			// Despawn the block if it enters unloaded chunks
			if (ticksExisted % 20 == 0)
			{
				if (tileEntityNBT == null && !world.isBlockLoaded(pos, false))
				{
					setDead();
					return;
				}
			}

			// Begin checking for any collisions
			if (!noCollision)
			{
				float speed = (float) Maths.speedSq(motionX, motionY, motionZ), dampening;
				Vec3d start_point = new Vec3d(posX, posY, posZ);
				Vec3d end_point = new Vec3d(posX + motionX * 1.3D, posY + motionY * 1.3D, posZ + motionZ * 1.3D);
				RayTraceResult hit;
				// Scan for any entities that are close enough to be hit
				if (ticksExisted % 5 == 0 && EntityMovingBlock.loadedEntities.containsKey(dimension))
					for (Entity entity : EntityMovingBlock.loadedEntities.get(dimension))
					{
							hit = entity.getEntityBoundingBox().grow(width).calculateIntercept(start_point, end_point);
							if (hit != null)
							{
								if (ConfigGrab.grabbed_blocks_hurt)
								{
									// Set entity on fire if block is hot
									if (this.isBurning() || Material.LAVA.equals(material))
										entity.setFire(15);

									// Additional damage based on blocks thrown
									if (block == Blocks.CACTUS)
										try {entity.attackEntityFrom(DamageSource.CACTUS, 1);} catch(Exception e) {}

									// Actual flying block damage
									try
									{
										entity.attackEntityFrom(WeatherDamageSource.FLYING_BLOCK, speed * (block.blockHardness >= 0.0F ? block.blockHardness * 3.0F : 10.0F));
										if (state != null)
											block.onEntityCollision(world, pos, state, entity);
									}
									catch(Exception e) {}
								}

								if (entity.canBePushed())
								{
									dampening = 1.0F / ((entity.height > entity.width ? entity.height : entity.width) * 0.25F + 1);
									dampening = dampening > 1.0F ? 1.0F : dampening;
									entity.motionX += motionX;
									entity.motionY += motionY;
									entity.motionZ += motionZ;	
									motionX *= dampening;
									motionY *= dampening;
									motionZ *= dampening;
									speed = (float) Maths.speedSq(motionX, motionY, motionZ);
									SoundType sound = block.getSoundType(state, world, pos, null);
									if (sound != null)
										world.playSound(null, posX, posY, posZ, sound.getFallSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
										
									break;
								}
							}
					}
				// Scan for any blocks that this will collide with
				if (mode == 0)
				{
					end_point = new Vec3d(posX + motionX * 1.3D, posY + motionY * 1.3D, posZ + motionZ * 1.3D);
					RayTraceResult raytrace = world.rayTraceBlocks(new Vec3d(posX, posY, posZ), end_point);

					if (raytrace != null)
					{
						end_point = new Vec3d(raytrace.hitVec.x, raytrace.hitVec.y, raytrace.hitVec.z);

						if (Type.BLOCK.equals(raytrace.typeOfHit))
						{
							// Check what side we hit and get the position we intend to blockify
							switch(raytrace.sideHit)
							{
								case UP: end_point = end_point.add(0, -1, 0); break;
								case DOWN: end_point = end_point.add(0, 1, 0); break;
								case NORTH: end_point = end_point.add(0, 0, 1); break;
								case WEST: end_point = end_point.add(-1, 0, 0); break;
								case SOUTH: end_point = end_point.add(0, 0, -1); break;
								case EAST: end_point = end_point.add(1, 0, 0); break;
							}

							// Conduct collisions
							BlockPos target_pos = raytrace.getBlockPos();
							IBlockState target = world.getBlockState(target_pos);
							Block target_block = target.getBlock();
							String tool_type = block.getHarvestTool(state);
							float speed_penalty = target_block.isToolEffective(tool_type != null ? tool_type : "", target) ? 0.0F : 0.3F;

							// If the flying block is fast enough: break the block and slow us down
							if (target_block.blockHardness >= 0.0F && target_block.blockHardness < speed * speed_penalty)
							{
								dampening = 1.0F / (target_block.blockHardness + 1.0F);
								dampening = dampening > 1.0F ? 1.0F : dampening;
								motionX *= dampening;
								motionY *= dampening;
								motionZ *= dampening;
								speed = (float) Maths.speedSq(motionX, motionY, motionZ);
								if (speed < 0.01F)
									blockify(target_pos.getX(), target_pos.getY(), target_pos.getZ());
								else
								{
									world.setBlockState(target_pos, EntityMovingBlock.AIR, 2|16);
									world.playEvent(2001, target_pos, Block.getStateId(state));
								}
							}
							// If not, then check if we are hitting the bottom of a block. If so, cause this block to stop ascending
							else if (EnumFacing.DOWN.equals(raytrace.sideHit))
							{
								motionY = motionY < 0.0D ? motionY : 0.0D;
								SoundType sound = block.getSoundType(state, world, pos, null);
								if (sound != null)
									world.playSound(null, posX, posY, posZ, sound.getHitSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
							}
							// If all else fails, blockify where we landed at.
							else
							{
								while(end_point.y < 255.0D)
								{
									target_pos = new BlockPos((int) end_point.x, (int) end_point.y, (int) end_point.z);
									target = world.getBlockState(target_pos);
									target_block = target.getBlock();

									if (WeatherUtilBlock.isReplacable(target, true))
									{
										blockify((int) end_point.x, (int) end_point.y, (int) end_point.z);
										break;
									}
									else
										end_point = end_point.add(0, 1, 0);
								}
							}
						}
					}
				}
				
			}
		}
		firstUpdate = false;
	}

	public boolean canEntityBeSeen(Entity par1Entity)
	{
		return this.world.rayTraceBlocks(new Vec3d(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ), new Vec3d(par1Entity.posX, par1Entity.posY + (double)par1Entity.getEyeHeight(), par1Entity.posZ)) == null;
	}

	private void blockify(int x, int y, int z)
	{
		try
		{
			if (ConfigGrab.Storm_Tornado_rarityOfBreakOnFall < 0 || rand.nextInt(ConfigGrab.Storm_Tornado_rarityOfBreakOnFall + 1) != 0)
			{
				if (ChunkUtils.isValidPos(world, y))
				{
					BlockPos pos = new BlockPos(x, y, z);
					ChunkUtils.setBlockState(world, pos, state);
					world.playEvent(2001, pos, Block.getStateId(state));
					if (tileEntityNBT != null)
					{
						TileEntity tile = block.createTileEntity(world, state);
						if (tile != null)
						{
							tile.readFromNBT(tileEntityNBT);
							world.setTileEntity(pos, tile);
						}
					}
				}
			}
		}
		catch (Exception e) {}
		setDead();
	}

	@Override
	public void setDead()
    {
		super.setDead();
		if (!world.isRemote && storm != null)
			storm.flyingBlocks = Math.max(storm.flyingBlocks - 1, 0);
    }
	
	@Override
	public boolean attackEntityFrom(@Nonnull DamageSource source, float amount)
	{
		setDead();
		return false;
	}
	
	@Override
	protected void setSize(float width, float height)
    {
		super.setSize(width, height);
	}

	@Override
	protected void writeEntityToNBT(@Nonnull NBTTagCompound nbt)
	{
		nbt.setString("Tile", Block.REGISTRY.getNameForObject(block).toString());
		nbt.setByte("Metadata", (byte)metadata);
		
		if (tileClass != null)
			nbt.setString("TileClass", tileClass.getName());
		if (tileEntityNBT != null)
			nbt.setTag("TileEntity", tileEntityNBT);
	}

	@Override
	public boolean writeToNBTOptional(@Nonnull NBTTagCompound compound)
    {
        return tileEntityNBT == null;
    }
	
	@SuppressWarnings({ "deprecation", "unchecked" })
	@Override
	protected void readEntityFromNBT(@Nonnull NBTTagCompound nbt)
	{
		block = Block.REGISTRY.getObject(new ResourceLocation(nbt.getString("Tile")));
		metadata = nbt.getByte("Metadata") & 15;
		state = block.getStateFromMeta(metadata);
		material = state.getMaterial();
		
		if (nbt.hasKey("TileClass"))
			try {tileClass = (Class<? extends TileEntity>) Class.forName(nbt.getString("TileClass"));}
			catch (Exception e) {tileClass = null;}
		else
			tileClass = null;
		
		if (nbt.hasKey("TileEntity"))
			tileEntityNBT = nbt.getCompoundTag("TileEntity");
		else
			tileEntityNBT = null;
	}

	@Override
	public void writeSpawnData(ByteBuf data)
	{
		ResourceLocation id = block != null ? Block.REGISTRY.getNameForObject(block) : null;
		String thing = tileClass == null ? "" : tileClass.getName();
		ByteBufUtils.writeUTF8String(data, id == null ? "" : id.toString());
		ByteBufUtils.writeUTF8String(data, thing);
		data.writeInt(metadata);
	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	@Override
	public void readSpawnData(ByteBuf data)
	{
		String str = ByteBufUtils.readUTF8String(data);
		String tileClass = ByteBufUtils.readUTF8String(data);
		if (!"".equals(str))
		{
			block = Block.REGISTRY.getObject(new ResourceLocation(str));
			metadata = data.readInt();
		}
		else
		{
			block = EntityMovingBlock.AIR.getBlock();
			metadata = 0;
		}
		
		if (!"".equals(tileClass))
			try {this.tileClass = (Class<? extends TileEntity>) Class.forName(tileClass);}
			catch (Exception e) {this.tileClass = null;}
		
		state = block.getStateFromMeta(metadata);
		material = state.getMaterial();
		isImmuneToFire = material.getCanBurn();
	}
}
