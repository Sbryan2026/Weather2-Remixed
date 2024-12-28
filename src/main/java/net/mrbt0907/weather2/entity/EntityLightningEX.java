package net.mrbt0907.weather2.entity;

import net.minecraft.block.BlockFire;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.weather2.config.ConfigClient;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.config.ConfigVolume;
import net.mrbt0907.weather2.registry.SoundRegistry;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.WorldUtil;

public class EntityLightningEX extends EntityLightningBolt
{
	private static final IBlockState FIRE = Blocks.FIRE.getDefaultState();
    public int fireLifeTime;
	public int fireChance;
	
	public EntityLightningEX(World world)
	{
		this(world, 0, 0, 0);
	}
	
	public EntityLightningEX(World world, double x, double y, double z)
	{
		super(world, x, y, z, false);
		fireLifeTime = ConfigStorm.lightning_bolt_fire_lifetime;
		fireChance = ConfigStorm.lightning_bolt_sets_fire_10_in_x;
	}
	
	@Override
	public void onUpdate()
	{
		if (!world.isRemote)
			setFlag(6, isGlowing());
		onEntityUpdate();
		
		if (world.isRemote && lightningState == 2)
			onSoundTick();

		--lightningState;

		if (lightningState < 0)
		{
			if (boltLivingTime == 0)
				setDead();
			else if (lightningState < -rand.nextInt(10))
			{
				--boltLivingTime;
				lightningState = 1;

				if (!world.isRemote && ConfigStorm.enable_lightning_bolt_fires && world.getGameRules().getBoolean("doFireTick") && Maths.chance(fireChance))
				{
					boltVertex = rand.nextLong();
					BlockPos blockpos = getPosition();

					if (world.isAreaLoaded(blockpos, 10) && world.getBlockState(blockpos).getMaterial() == Material.AIR && Blocks.FIRE.canPlaceBlockAt(world, blockpos))
						world.setBlockState(blockpos, FIRE.withProperty(BlockFire.AGE, fireLifeTime));
				}
			}
		}
		else
		{
			if (world.isRemote)
			{
				if (ConfigClient.enable_sky_lightning)
					onLightSky();
			}
			else
				WorldUtil.getNearestEntities(world, posX, posY, posZ, 3.0D).forEach(entity -> {
					if (!net.minecraftforge.event.ForgeEventFactory.onEntityStruckByLightning(entity, this))
						entity.onStruckByLightning(this);
				});
		}
	}
	
	@SideOnly(Side.CLIENT)
	protected void onLightSky()
	{
		if (net.minecraft.client.Minecraft.getMinecraft().player.getDistance(this) < ConfigStorm.max_lightning_bolt_distance)
			world.setLastLightningBolt(2);
	}
	
	@SideOnly(Side.CLIENT)
	protected void onSoundTick()
	{
		net.minecraft.client.Minecraft MC = net.minecraft.client.Minecraft.getMinecraft();
		if (MC.player == null) return;
		double distance = MC.player.getDistance(this);
		if (distance < ConfigStorm.max_lightning_bolt_distance)
		{
			if (distance > 200.0D)
				world.playSound(posX, posY, posZ, SoundRegistry.thunderNear, SoundCategory.WEATHER, 10000.0F * ConfigVolume.lightning, 0.8F + rand.nextFloat() * 0.2F, true);
			else
			{
				world.playSound(posX, posY, posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.WEATHER, 64.0F * ConfigVolume.lightning, 0.8F + rand.nextFloat() * 0.2F, true);
				world.playSound(posX, posY, posZ, SoundEvents.ENTITY_LIGHTNING_IMPACT, SoundCategory.WEATHER, 2.0F, 0.5F + rand.nextFloat() * 0.2F, false);
			}
		}
		else if (distance < ConfigStorm.max_lightning_bolt_distance * 1.5D)
		{
			world.playSound(posX, posY, posZ, SoundRegistry.thunderFar, SoundCategory.WEATHER, 10000.0F * ConfigVolume.lightning, 0.8F + rand.nextFloat() * 0.2F, false);
		}
	}
}