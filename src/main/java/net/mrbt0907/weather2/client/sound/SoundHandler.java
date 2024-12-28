package net.mrbt0907.weather2.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.ISoundEventListener;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.event.StopSoundEvent;
import paulscode.sound.SoundSystemConfig;

@SideOnly(Side.CLIENT)
public class SoundHandler
{
	private static final Minecraft MC = Minecraft.getMinecraft();
	private static ISound[] soundsStreaming;
	
	@SubscribeEvent
	public static void onWorldUnload(WorldEvent.Unload event)
	{
		
	}
	
	@SubscribeEvent
	public static void onSoundLoad(SoundLoadEvent event)
	{
		soundsStreaming = new ISound[SoundSystemConfig.getNumberStreamingChannels()];
	}
	
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void onSoundStop(StopSoundEvent event)
	{
		ISound sound = event.getSound();
		
		if (sound == null)
		{
			//Weather2.info("Weather2 SoundManager recieved a null sound event in StopSoundEvent");
			return;
		}

		//Weather2.info("Stopped sound: " + sound.getSoundLocation());
		if (soundsStreaming != null)
		{
			ISound cachedSound;
			for (int i = 0; i < soundsStreaming.length; i++)
			{
				cachedSound = soundsStreaming[i];
				if (sound.equals(cachedSound))
				{
					//Weather2.info("Stopped streaming sound: " + sound.getSoundLocation());
					soundsStreaming[i] = null;
					return;
				}
			}
		}
		
	}
	
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void onSoundPlay(PlaySoundEvent event)
	{
		ISound sound = event.getResultSound();
		int priority = getSoundPriority(sound);
		if (sound == null) return;
		
		if (sound.getSound() == null)
		{
			SoundManager manager = event.getManager();
			SoundEventAccessor soundeventaccessor = sound.createAccessor(manager.sndHandler);
			if (soundeventaccessor == null) return;
			
			if (!manager.listeners.isEmpty())
                for (ISoundEventListener isoundeventlistener : manager.listeners)
                    isoundeventlistener.soundPlay(sound, soundeventaccessor);
			
			if (sound.getSound() == null || sound.getSound().equals(net.minecraft.client.audio.SoundHandler.MISSING_SOUND) || manager.sndSystem.getMasterVolume() <= 0.0F || MathHelper.clamp(sound.getVolume() * getVolume(sound.getCategory()), 0.0F, 1.0F) == 0.0F) return;
		}
		
		if (sound.getSound().isStreaming())
		{
			ISound cachedSound;
			int cachedPriority;
			int index = -1;
			
			for (int i = 0; i < soundsStreaming.length; i++)
			{
				cachedSound = soundsStreaming[i];
				if (cachedSound == null)
				{
					//Weather2.info("Added streaming new sound[" + i + "]: " + sound.getSoundLocation());
					soundsStreaming[i] = sound;
					return;
				}
				
				cachedPriority = getSoundPriority(cachedSound);
				
				if (priority > cachedPriority)
					index = i;
			}
			
			if (index > -1)
			{
				//Weather2.info("Replacing streaming sound[" + index + "]: " + sound.getSoundLocation());
				soundsStreaming[index] = sound;
				return;
			}
			else
			{
				//Weather2.info("Canceling streaming sound: " + sound.getSoundLocation());
				event.setResultSound(null);
			}
		}
	}
	
	private static float getVolume(SoundCategory category)
    {
        return category != null && category != SoundCategory.MASTER ? MC.gameSettings.getSoundLevel(category) : 1.0F;
    }
	
	public static int getSoundPriority(ISound sound)
	{
		return sound instanceof MovingSoundEX ? ((MovingSoundEX)sound).priority : 0;
	}
	
	public static ISound getSound(SoundEvent sound, int priority)
	{
		if (soundsStreaming == null) return null;
		ISound cachedSound;
		for (int i = 0; i < soundsStreaming.length; i++)
		{
			cachedSound = soundsStreaming[i];
			if (cachedSound != null && cachedSound.getSoundLocation().toString().equals(sound.getSoundName().toString()) && getSoundPriority(cachedSound) == priority)
			{
				return cachedSound;
			}
		}
		return null;
	}
	
	public static boolean contains(SoundEvent sound, int priority)
	{
		return getSound(sound, priority) != null; 
	}
	
	public static boolean canPlaySound(SoundEvent sound, int priority)
	{
		if (soundsStreaming == null || contains(sound, priority)) return false;
		ISound cachedSound;
		
		for (int i = 0; i < soundsStreaming.length; i++)
		{
			cachedSound = soundsStreaming[i];
			if (cachedSound == null || getSoundPriority(cachedSound) < priority)
				return true;
		}
		return false;
	}
	
	public static MovingSoundEX playStaticSound(SoundEvent sound, SoundCategory category, int priority, float volume, float pitch)
	{
		return playSound(null, sound, category, priority, volume, pitch, 0.0D);
	}
	
	public static MovingSoundEX playStaticSound(SoundEvent sound, SoundCategory category, int priority, float volume, float pitch, double range)
	{
		return playSound(null, sound, category, priority, volume, pitch, range);
	}
	
	public static MovingSoundEX playMovingSound(Object obj, SoundEvent sound, SoundCategory category, int priority, float volume, float pitch)
	{
		return playSound(obj, sound, category, priority, volume, pitch, 0.0D);
	}
	
	public static MovingSoundEX playMovingSound(Object obj, SoundEvent sound, SoundCategory category, int priority, float volume, float pitch, double range)
	{
		return playSound(obj, sound, category, priority, volume, pitch, range);
	}
	
	private static MovingSoundEX playSound(Object obj, SoundEvent sound, SoundCategory category, int priority, float volume, float pitch, double range)
	{
		if (!canPlaySound(sound, priority)) return null;
		MovingSoundEX streamingSound = new MovingSoundEX(obj, sound, category, priority, volume, pitch, range);
		MC.getSoundHandler().playSound(streamingSound);
		return streamingSound;
	}
}