package net.mrbt0907.weather2.block.tile;

import CoroUtil.util.CoroUtilPhysics;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Stage;
import net.mrbt0907.weather2.block.BlockSiren;
import net.mrbt0907.weather2.client.event.ClientTickHandler;
import net.mrbt0907.weather2.client.sound.MovingSoundEX;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.config.ConfigSand;
import net.mrbt0907.weather2.config.ConfigVolume;
import net.mrbt0907.weather2.entity.AI.EntityAITakeCover;
import net.mrbt0907.weather2.registry.BlockRegistry;
import net.mrbt0907.weather2.registry.SoundRegistry;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.util.WeatherUtilSound;
import net.mrbt0907.weather2.weather.storm.WeatherObject;
import net.mrbt0907.weather2.weather.storm.SandstormObject;

import java.util.ArrayList;
import java.util.List;

public class TileSiren extends TileEntity implements ITickable
{
	MovingSoundEX sound;
	
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
    	if (sound == null || sound.isDonePlaying())
        {
            Vec3 pos = new Vec3(getPos().getX(), getPos().getY(), getPos().getZ());
    		WeatherObject so = ClientTickHandler.weatherManager.getWorstWeather(pos, ConfigMisc.siren_scan_range, Stage.TORNADO.getStage(), Integer.MAX_VALUE);

            if (so != null)
            	sound = WeatherUtilSound.playForcedSound(SoundRegistry.siren, SoundCategory.RECORDS, pos, (float) ConfigVolume.sirens, 1.0F, 120.0F, true, false);
            else
            {
                if (!ConfigSand.disable_darude_sandstorm_plz) {
                    SandstormObject sandstorm = ClientTickHandler.weatherManager.getClosestSandstormByIntensity(pos);

                    if (sandstorm != null) {
                        List<CoroUtil.util.Vec3> points = sandstorm.getSandstormAsShape();

                        float distMax = 75F;

                        //double scale = sandstorm.getSandstormScale();
                        boolean inStorm = CoroUtilPhysics.isInConvexShape(pos.toVec3Coro(), points);
                        double dist = Math.min(distMax, CoroUtilPhysics.getDistanceToShape(pos.toVec3Coro(), points));

                        if (inStorm || dist < distMax)
                        	sound = WeatherUtilSound.playForcedSound(SoundRegistry.sirenDarude, SoundCategory.RECORDS, pos, (float) ConfigVolume.sirens, 1.0F, 120.0F, true, false);
                    }
                }
            }
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
}
