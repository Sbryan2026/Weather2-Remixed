package net.mrbt0907.weather2remastered.config;

import net.mrbt0907.configex.api.ConfigAnnotations.*;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.weather2remastered.Weather2Remastered;

import java.io.File;


public class ConfigSnow implements IConfigEX
{
	//snow
	@ServerSide
	@Comment("Should snow build up in a snowstorm?")
	public static boolean Snow_PerformSnowfall = false;
	@ServerSide
	@IntegerRange(min=1)
	@Comment("Snow layers have a 1 in x chance to spawn durring a snowstorm")
	public static int Snow_RarityOfBuildup = 64;

    @Override
    public String getName()
    {
        return "Weather2 Remastered - Snow";
    }

    @Override
    public String getSaveLocation()
    {
        return Weather2Remastered.MODID + File.separator + "snow";
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
