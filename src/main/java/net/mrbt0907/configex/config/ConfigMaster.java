package net.mrbt0907.configex.config;

import net.mrbt0907.configex.ConfigModEX;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.configex.api.ConfigAnnotations.Comment;
import net.mrbt0907.configex.api.ConfigAnnotations.Hidden;
import net.mrbt0907.configex.api.ConfigAnnotations.Name;
import net.mrbt0907.weather2remastered.Weather2Remastered;

public class ConfigMaster implements IConfigEX
{
	@Hidden
	@Name("Enable Debug Mode")
	@Comment("Enables the displaying of various debugging information in the console")
	public static boolean debug_mode = false;
	
	@Override
	public String getName()
	{
		return "ConfigEX - Master";
	}

	@Override
	public String getDescription()
	{
		return "This is the master config file for the config mod.";
	}

	@Override
	public String getSaveLocation()
	{
		return ConfigModEX.MODID + "/master";
	}

	@Override
	public void onConfigChanged(Phase phase, int variables)
	{
		Weather2Remastered.info("onConfigChanged: " + phase + ", " + variables);
	}

	@Override
	public void onValueChanged(String variable, Object oldValue, Object newValue)
	{
		Weather2Remastered.info("onValueChanged [" + variable + "]: " + oldValue + ", " + newValue);
	}

}
