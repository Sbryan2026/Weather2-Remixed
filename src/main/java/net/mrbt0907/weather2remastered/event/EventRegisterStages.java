package net.mrbt0907.weather2remastered.event;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import net.mrbt0907.weather2remastered.util.ConfigList;

@Cancelable
public class EventRegisterStages extends Event
{
	public final ConfigList tornadoStageList;
	public final ConfigList hurricaneStageList;
	
	/**Event used to register custom tornado chances to the game.*/
	public EventRegisterStages(ConfigList tornadoStageList, ConfigList hurricaneStageList)
	{
		this.tornadoStageList = tornadoStageList;
		this.hurricaneStageList = hurricaneStageList;
	}
}
