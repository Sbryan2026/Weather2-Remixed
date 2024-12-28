package net.mrbt0907.weather2.mixin.injection;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundManager;
import net.minecraftforge.common.MinecraftForge;
import net.mrbt0907.weather2.api.event.StopSoundEvent;


@Mixin(SoundManager.class)
public class MixinSoundManager
{
	@Shadow
	private boolean loaded;
	
	@Shadow
	private Map<ISound, String> invPlayingSounds;
	@Shadow
	private Map<String, ISound> playingSounds;
	@Shadow
	public SoundManager.SoundSystemStarterThread sndSystem;

	@Inject(method = "updateAllSounds()V", at = @At("HEAD"))
	public void updateAllSounds(CallbackInfo callback)
    {
		Iterator<Entry<String, ISound>> iterator = playingSounds.entrySet().iterator();

        while (iterator.hasNext())
        {
            Entry<String, ISound> entry = (Entry<String, ISound>)iterator.next();
            if (!sndSystem.playing(entry.getKey()))
    			MinecraftForge.EVENT_BUS.post(new StopSoundEvent((SoundManager)(Object)this, entry.getValue()));
        }
    }
	
	@Inject(method = "stopSound(Lnet/minecraft/client/audio/ISound;)V", at = @At("HEAD"))
	public void stopSound(ISound sound, CallbackInfo callback)
	{
		if (loaded && invPlayingSounds.get(sound) != null)
			MinecraftForge.EVENT_BUS.post(new StopSoundEvent((SoundManager)(Object)this, sound));
	}
	
	@Inject(method = "stopAllSounds()V", at = @At("HEAD"))
	public void stopAllSounds(CallbackInfo callback)
	{
		if (loaded)
			playingSounds.forEach((id, sound) -> {MinecraftForge.EVENT_BUS.post(new StopSoundEvent((SoundManager)(Object)this, sound));});
	}
}