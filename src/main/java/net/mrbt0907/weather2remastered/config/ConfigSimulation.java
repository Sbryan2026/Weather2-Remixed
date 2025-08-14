package net.mrbt0907.weather2remastered.config;

import net.mrbt0907.configex.api.ConfigAnnotations.*;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.weather2remastered.Weather2Remastered;

import java.io.File;


public class ConfigSimulation implements IConfigEX
{
	@ServerSide
	@Enforce
	@Permission(4)
	@IntegerRange(min=200, max=6000)
	@Comment("The delay in ticks before the weather managers, storms, fronts, etc initialize. There are 20 ticks in a second. Max 5 minutes (6000 ticks).")
	public static int core_init_delay = 200;
	@ServerSide
	@IntegerRange(min=0)
	@Comment("Distance storms can go to from players before they are deleted")
	public static int max_storm_distance = 3072;
	@ServerSide
	@IntegerRange(min=0)
	@Comment("Distance storms can spawn away from players")
	public static int max_storm_spawning_distance = 768;
	
    @Override
    public String getName()
    {
        return "Weather2 Remastered - Simulation";
    }

    @Override
    public String getSaveLocation()
    {
        return Weather2Remastered.MODID + File.separator + "simulation";
    }

	@Override
	public String getDescription()
	{
		return null;
	}

	@Override
	public void onConfigChanged(Phase phase, int variables) {}

	@Override
	public void onValueChanged(String variable, Object oldValue, Object newValue) {}
}
