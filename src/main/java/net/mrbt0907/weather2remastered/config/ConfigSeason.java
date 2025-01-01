package net.mrbt0907.weather2remastered.config;

import java.io.File;

import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.weather2remastered.Weather2Remastered;

public class ConfigSeason implements IConfigEX
{
	//4 Main seasons, each season lowers or raises chances for storms. That is done through the storm itself.
	public static int season_start = 1;
	public static int season_length = 30;
	
	
	@Override
	public String getName()
	{
		return "Weather2 Remastered - Seasons";
	}
	
	@Override
    public String getSaveLocation()
    {
        return Weather2Remastered.MODID + File.separator + "season";
    }
	
	@Override
	public String getDescription()
	{
		return null;
	}

	@Override
	public void onConfigChanged(Phase phase, int variables)
	{
		
	}

	@Override
	public void onValueChanged(String variable, Object oldValue, Object newValue)
	{
		
	}

}
