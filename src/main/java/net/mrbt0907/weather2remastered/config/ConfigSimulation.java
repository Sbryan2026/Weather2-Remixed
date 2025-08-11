package net.mrbt0907.weather2remastered.config;

import net.mrbt0907.configex.api.ConfigAnnotations.*;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.weather2remastered.Weather2Remastered;

import java.io.File;


public class ConfigSimulation implements IConfigEX
{
	@ServerSide
	@IntegerRange(min=0)
	@Comment("Distance storms can go to from players before they are deleted")
	public static int max_storm_distance = 3600;
	@ServerSide
	@IntegerRange(min=0)
	@Comment("Distance storms can spawn away from players")
	public static int max_storm_spawning_distance = 1500;
	
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
