package net.mrbt0907.weather2.api.event;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundManager;
import net.minecraftforge.client.event.sound.SoundEvent;

/***
 * Raised when the SoundManager tries to stop a normal sound.
 *
 * Cannot cancel this event
 */
public class StopSoundEvent extends SoundEvent
{
	private final String name;
	private final ISound sound;

	public StopSoundEvent(SoundManager manager, ISound sound)
	{
		super(manager);
		this.sound = sound;
		name = sound.getSoundLocation().getPath();
	}

	public String getName()
	{
		return name;
	}

	public ISound getSound()
	{
		return sound;
	}
}
