package net.mrbt0907.weather2.block;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.weather2.client.sound.MovingSoundEX;
import net.mrbt0907.weather2.config.ConfigVolume;
import net.mrbt0907.weather2.entity.AI.EntityAITakeCover;
import net.mrbt0907.weather2.registry.BlockRegistry;
import net.mrbt0907.weather2.registry.SoundRegistry;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.util.WeatherUtilSound;

public class TileEntityTSirenManual extends TileEntity implements ITickable
{
    private MovingSoundEX sound;
    
    @Override
    public void update()
    {
    	int meta = BlockRegistry.emergency_siren.getMetaFromState(this.world.getBlockState(this.getPos()));
    	
    	if (BlockSiren.isEnabled(meta))
    	{
            if (world.isRemote)
                tickClient();
            else
            	tickAlert();
    	}
    	else
    	{
    		if (world.isRemote && sound != null)
    		{
	    		sound.setDone();
	    		sound = null;
    		}
    	}
    }
    
    @SideOnly(Side.CLIENT)
    public void tickClient()
    {
    	if (sound == null || sound.isDonePlaying() || !Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(sound))
    	{
    		Vec3 pos = new Vec3(getPos().getX(), getPos().getY(), getPos().getZ());
        	sound = WeatherUtilSound.playForcedSound(SoundRegistry.siren, SoundCategory.RECORDS, pos, (float) ConfigVolume.sirens, 1.0F, 356.0F, true, true);
    	}
    }
    
    private void tickAlert()
    {
        if (!world.isRemote && world.getTotalWorldTime() % 5L == 0L)
        {
            List<Entity> entities = new ArrayList<Entity>(world.loadedEntityList);
            for (Entity entity : entities)
            	if (entity instanceof EntityLiving && entity.getDistanceSq(pos) < 120.0D)
                	((EntityLiving)entity).tasks.taskEntries.forEach(task -> {
                		if (task.action instanceof EntityAITakeCover) ((EntityAITakeCover)task.action).isAlert = true;
                	});
        }
    }
    
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState)
    {
    	return oldState.getBlock() != newState.getBlock();
    }
}
