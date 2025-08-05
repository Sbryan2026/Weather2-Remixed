package net.mrbt0907.weather2remastered.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraftforge.eventbus.api.Event;
import net.mrbt0907.weather2remastered.Weather2Remastered;
import net.mrbt0907.weather2remastered.util.TriMapEx;

public class EventRegisterEZGUIOption extends Event
{
	private TriMapEx<String, List<String>, Integer> options;
	private Map<String, Integer> optionCategory;
	
	/**Event used to register custom EZ Gui entries to the game.*/
	public EventRegisterEZGUIOption(TriMapEx<String, List<String>, Integer> options, Map<String, Integer> categories)
	{
		this.options = options;
		this.optionCategory = categories;
	}
	
	/**Registers an EZ Gui option to the EZ Gui.*/
	public void register(String id, EnumEZCategory category, int defaultOption, String... options)
	{
		List<String> settings = new ArrayList<String>();
		
		if (options.length > 0)
			for (String option : options)
				settings.add(option);
		
		register(id, category, defaultOption, settings);
	}
		
	public void register(String id, EnumEZCategory category, int defaultOption, List<String> options)
	{
		if (id == null)
			Weather2Remastered.debug("Failed to register EZ Gui option as the id was null. Skipping...");
		else if (this.options.contains(id))
			Weather2Remastered.debug("Failed to register EZ Gui option " + id + " as the id was already used. Skipping...");
		else if (category == null)
			Weather2Remastered.debug("Failed to register EZ Gui option " + id + " as the category was invalid. Skipping...");
		else if (options.size() < 2)
			Weather2Remastered.debug("Failed to register EZ Gui option " + id + " as there was not enough options. Skipping...");
		else if (defaultOption > options.size() || defaultOption < 0)
			Weather2Remastered.debug("Failed to register EZ Gui option " + id + " as the default option is out of range. Skipping...");
		else
		{
			List<String> settings = new ArrayList<String>();
			for (String option : options)
				settings.add(option);
			this.options.put(id, settings, defaultOption);
			optionCategory.put(id, category.ordinal());
		}
	}
	
	public TriMapEx<String, List<String>, Integer> getOptions()
	{
		return options;
	}
	
	public Map<String, Integer> getOptionCategories()
	{
		return optionCategory;
	}
	
	public static enum EnumEZCategory
	{
		GRAPHICS, SYSTEM, STORMS;
	}
}
