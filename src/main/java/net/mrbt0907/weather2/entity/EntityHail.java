package net.mrbt0907.weather2.entity;

import javax.annotation.Nonnull;

import CoroUtil.api.weather.IWindHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.mrbt0907.weather2.api.WeatherDamageSource;
import net.mrbt0907.weather2.util.Maths;

public class EntityHail extends Entity implements IWindHandler
{
	protected static final IBlockState AIR = Blocks.AIR.getDefaultState();
	protected static final IBlockState ICE = Blocks.ICE.getDefaultState();
	public double size;

	public EntityHail(World world)
	{
		this(world, 0.3F);
	}

	public EntityHail(World world, float size)
	{
		super(world);
		setSize(size, size);
	}
	
	@Override
	protected void entityInit() {}

	@Override
	protected void readEntityFromNBT(@Nonnull NBTTagCompound nbt)
	{
		if (nbt.hasKey("size"))
			size = nbt.getDouble("size");
	}

	@Override
	protected void writeEntityToNBT(@Nonnull NBTTagCompound nbt)
	{
		nbt.setDouble("size", size);
	}

	@Override
	public boolean writeToNBTOptional(@Nonnull NBTTagCompound compound)
    {
        return false;
    }

	@Override
	public void onUpdate()
    {
		if (motionY > -3.0D)
			motionY -= 0.1D;

		motionX = Maths.clamp(motionX + Maths.random(-0.05D, 0.05D), -3.0D, 3.0D);
		motionZ = Maths.clamp(motionZ + Maths.random(-0.05D, 0.05D), -3.0D, 3.0D);
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;
		posX += motionX;
		posY += motionY;
		posZ += motionZ;
		setPosition(posX, posY, posZ);
		if (posY < -64.0D)
            outOfWorld();

		if (!world.isRemote)
        {
			if (isInWater())
			{
				setDead();
				return;
			}
				

			// If we are moving fast enough, scan for any players to damage
			Vec3d start_point = new Vec3d(posX, posY, posZ);
			Vec3d end_point = new Vec3d(posX + motionX * 1.3D, posY + motionY * 1.3D, posZ + motionZ * 1.3D);
			RayTraceResult hit;

			double speed = Maths.speedSq(motionX, motionY, motionZ);
			if (speed > 0.2F)
			{
				// Scan for any entities that are close enough to be hit
			if (ticksExisted % 5 == 0 && EntityMovingBlock.loadedEntities.containsKey(dimension))
				for (Entity entity : EntityMovingBlock.loadedEntities.get(dimension))
				{
					hit = entity.getEntityBoundingBox().grow(width).calculateIntercept(start_point, end_point);
					if (hit != null)
					{
							// Actual flying block damage
							try
							{
								entity.attackEntityFrom(WeatherDamageSource.HAIL, (float)speed * getWeight());
							}
							catch(Exception e) {}

							world.playSound(null, new BlockPos(posX, posY, posZ), SoundEvents.BLOCK_GLASS_HIT, SoundCategory.AMBIENT, 1F, 5F - width * 5.0F);
							setDead();
							return;
					}
				}
			}

			// Scan for any blocks that this will collide with
			end_point = new Vec3d(posX + motionX * 1.3D, posY + motionY * 1.3D, posZ + motionZ * 1.3D);
			RayTraceResult raytrace = world.rayTraceBlocks(new Vec3d(posX, posY, posZ), end_point);

			if (raytrace != null)
			{
				end_point = new Vec3d(raytrace.hitVec.x, raytrace.hitVec.y, raytrace.hitVec.z);

				if (Type.BLOCK.equals(raytrace.typeOfHit))
				{
					// Conduct collisions
					double dampening;
					BlockPos target_pos = raytrace.getBlockPos();
					IBlockState target = world.getBlockState(target_pos);
					Block target_block = target.getBlock();
					String tool_type = Blocks.ICE.getHarvestTool(EntityHail.ICE);
					float speed_penalty = target_block.isToolEffective(tool_type != null ? tool_type : "", target) ? 0.20F : 0.06F;

					// If the flying block is fast enough: break the block and slow us down
					if (target_block.blockHardness >= 0.0F && target_block.blockHardness < getWeight() * speed * speed_penalty)
					{
						dampening = 1.0F / (target_block.blockHardness + 1.0F);
						dampening = dampening > 1.0F ? 1.0F : dampening;
						motionX *= dampening;
						motionY *= dampening;
						motionZ *= dampening;
						speed = (float) Maths.speedSq(motionX, motionY, motionZ);
						if (speed < 0.01F)
						{
							if (Maths.chance())
								world.playSound(null, new BlockPos(posX, posY, posZ), SoundEvents.BLOCK_STONE_STEP, SoundCategory.AMBIENT, 1F, 5F - width * 5.0F);
							setDead();
						}
							
						else
						{
							world.setBlockState(target_pos, EntityHail.AIR, 2|16);
							world.playEvent(2001, target_pos, Block.getStateId(target));
						}
					}
					else
					{
						if (Maths.chance())
							world.playSound(null, new BlockPos(posX, posY, posZ), SoundEvents.BLOCK_STONE_STEP, SoundCategory.AMBIENT, 1F, 5F - width * 5.0F);
						setDead();
					}
				}
			}
        }
    }
	
	@Override
	public boolean attackEntityFrom(@Nonnull DamageSource source, float amount)
	{
		world.playSound(null, new BlockPos(posX, posY, posZ), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.AMBIENT, 1F, 5F - width * 5.0F);
		setDead();
		return false;
	}

	public float getWeight()
	{
		return height + width;
	}

	@Override
	protected void setSize(float width, float height)
    {
		size = width;
		super.setSize(width, height);
	}

	@Override
	public float getWindWeight() {return 4.0F + getWeight();}

	@Override
	public int getParticleDecayExtra() {return 0;}
}
