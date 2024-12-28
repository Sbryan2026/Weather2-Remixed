package net.mrbt0907.weather2.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.weather.storm.WeatherObject;

public class MovingSoundEX extends MovingSound
{
	private static final Minecraft MC = Minecraft.getMinecraft();
	public Object obj;
	public float maxVolume;
	public double range;
	public int priority;
	
	public MovingSoundEX(SoundEvent sound, SoundCategory category, int priority, float volume, float pitch)
	{
		this(null, sound, category, priority, volume, pitch, 0.0D);
	}
	
	public MovingSoundEX(SoundEvent sound, SoundCategory category, int priority, float volume, float pitch, double range)
	{
		this(null, sound, category, priority, volume, pitch, range);
	}
	
	public MovingSoundEX(Object obj, SoundEvent sound, SoundCategory category, int priority, float volume, float pitch)
	{
		this(obj, sound, category, priority, volume, pitch, 0.0D);
	}
	
	public MovingSoundEX(Object obj, SoundEvent sound, SoundCategory category, int priority, float volume, float pitch, double range)
	{
		super(sound, category);
		this.obj = obj;
		this.volume = Maths.clamp(volume, 0.0F, 1.0F);
		maxVolume = this.volume;
		this.pitch = pitch;
		this.range = range;
		this.priority = priority;
	}

	public void update()
	{
		Vec3 pos;
		
		if (MC.player == null || MC.world == null)
		{
			donePlaying = true;
			Weather2.error("Unable to play sound " + getSoundLocation().toString() + " as the world is null");
			return;
		}

		xPosF = (float) (MC.player.posX + MC.player.motionX);
		yPosF = (float) (MC.player.posY + MC.player.motionY);
		zPosF = (float) (MC.player.posZ + MC.player.motionZ);
		
		if (obj != null)
		{
			if (obj instanceof Vec3)
				pos = (Vec3)obj;
			else if (obj instanceof BlockPos)
				pos = new Vec3(((BlockPos)obj));
			else if (obj instanceof WeatherObject)
				pos = ((WeatherObject)obj).pos;
			else if (obj instanceof Entity)
			{
				Entity entity = ((Entity)obj);
				pos = new Vec3(entity.posX, entity.posY, entity.posZ);
			}
			else
			{
				donePlaying = true;
				Weather2.error("Unable to play sound " + getSoundLocation().toString() + " as the provided object is not supported: " + String.valueOf(obj));
				return;
			}
			
			//if locked to player, don't dynamically adjust volume
			if (range > 0.0F)
			{
				float multiplier = (float)Maths.clamp((range - pos.distanceSq(MC.player.posX, MC.player.posY, MC.player.posZ)) / range, 0.0F, 1.0F);
				volume = maxVolume * multiplier;
				
				xPosF = (float) Maths.clamp(pos.posX,xPosF - 6.0D, xPosF + 6.0D);
				yPosF = (float) Maths.clamp(pos.posY, yPosF - 6.0D, yPosF + 6.0D);
				zPosF = (float) Maths.clamp(pos.posZ, zPosF - 6.0D, zPosF + 6.0D);
			}
		}
	}
	
	public void setRepeat(boolean shouldRepeat)
	{
		repeat = shouldRepeat;
	}
	
	public void adjustVolume(float volume)
	{
		maxVolume = Maths.clamp(volume, 0.0F, 1.0F);
	}
	
	public void adjustPitch(float pitch)
	{
		this.pitch = Maths.clamp(pitch, 0.0F, 1.0F);
	}
	
	public void setDone()
	{
		donePlaying = true;
	}
}
