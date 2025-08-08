package net.mrbt0907.weather2remastered.api;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.registries.ForgeRegistries;
import net.mrbt0907.weather2remastered.Weather2Remastered;
import net.mrbt0907.weather2remastered.api.weather.AbstractStormObject;
import net.mrbt0907.weather2remastered.api.weather.AbstractWeatherManager;
import net.mrbt0907.weather2remastered.api.weather.AbstractWeatherRenderer;
import net.mrbt0907.weather2remastered.config.ConfigGrab;
import net.mrbt0907.weather2remastered.event.EventRegisterGrabLists;
import net.mrbt0907.weather2remastered.gui.EZConfigParser;
import net.mrbt0907.weather2remastered.util.ConfigList;

public class WeatherAPI
{
	private static final ConfigList tornadoStageList = new ConfigList();
	private static final ConfigList hurricaneStageList = new ConfigList();
	private static final ConfigList grabList = new ConfigList();
	private static final ConfigList replaceList = new ConfigList();
	private static final ConfigList entityList = new ConfigList();
	private static final ConfigList windResistanceList = new ConfigList().setReplaceOnly();
	/**Gets the block replace list, which is used to specify what block turns to what when a tornado attempts to replace said block. Feel free to use any block including tile entities*/
	public static ConfigList getEntityGrabList()
	{
		return entityList;
	}
	/**Gets the block grab list, which is used to detect what blocks can be picked up as a falling block entity. Avoid tile entities and things like buttons*/
	public static ConfigList getGrabList()
	{
		return grabList;
	}
	/**Gets the block replace list, which is used to specify what block turns to what when a tornado attempts to replace said block. Feel free to use any block including tile entities*/
	public static ConfigList getReplaceList()
	{
		return replaceList;
	}
	/**Gets the estimated wind speed based on the stage of tornado provided with support for decimals*/
	public static float getEFWindSpeed(float stageMultiplier)
	{
		return 65.0F + 27.0F * stageMultiplier;
	}
	/**Gets the wind resistance list**/
	public static ConfigList getWRList()
	{
		return windResistanceList;
	}
	public static void refreshGrabRules()
	{
		// Clear lists BEFORE firing the event
		grabList.clear();
		replaceList.clear();
		entityList.clear();
		windResistanceList.clear();
		
		// Fire the event to allow others to populate the lists
		EventRegisterGrabLists event = new EventRegisterGrabLists(grabList, replaceList, entityList, windResistanceList);
		MinecraftForge.EVENT_BUS.post(event);
    	
    	event.grabList.parse(ConfigGrab.grab_list_entries);
    	event.replaceList.parse(ConfigGrab.replace_list_entries);
    	event.windResistanceList.parse(ConfigGrab.wind_resistance_entries);
    	event.entityList.parse(ConfigGrab.entity_blacklist_entries);
    	
    	Set<ResourceLocation> blockEntries =  ForgeRegistries.BLOCKS.getKeys(), entityEntries = ForgeRegistries.ENTITIES.getKeys();
    	ConfigList list = processGrabList(blockEntries, event.grabList, ConfigGrab.grab_list_partial_match, 0);
    	grabList.clear();
    	grabList.addAll(list);
    	list = processGrabList(blockEntries, event.replaceList, ConfigGrab.replace_list_partial_match, 1);
    	replaceList.clear();
    	replaceList.addAll(list);
    	list = processGrabList(blockEntries, event.windResistanceList, ConfigGrab.wind_resistance_partial_matches, 2);
    	//Go through all entries and add missing wind entries
    	Set<ResourceLocation> blocks = ForgeRegistries.BLOCKS.getKeys();
    	String strID; Block block; BlockState state;
    	for (ResourceLocation id : blocks)
    	{
    		strID = id.toString();
    		if (!list.containsKey(strID))
    		{
    			block = ForgeRegistries.BLOCKS.getValue(id);
    			if (block.getHarvestLevel(block.defaultBlockState()) >= 0)
    			{
    				state = block.defaultBlockState();
    				list.add(strID, !state.requiresCorrectToolForDrops() || block.isToolEffective(block.defaultBlockState(), ToolType.AXE) ? WeatherAPI.getEFWindSpeed(block.getHarvestLevel(block.defaultBlockState())) : WeatherAPI.getEFWindSpeed(block.getHarvestLevel(block.defaultBlockState()) * 2.0F));
    			}
    		}
    	}
    	windResistanceList.clear();
    	windResistanceList.addAll(list);
    	list = processGrabList(entityEntries, event.entityList, ConfigGrab.entity_blacklist_partial_match, 0);
    	entityList.clear();
    	entityList.addAll(list);
    	
    	Weather2Remastered.debug("Grab Rules have been updated:\n- Grab List = " + WeatherAPI.getGrabList().size() + " Entry(s)\n- Replace List = " + WeatherAPI.getReplaceList().size() + " Entry(s)\n- Wind Resistance List = " + WeatherAPI.getWRList().size() + " Entry(s)\n- Blacklisted Entity List = " + WeatherAPI.getEntityGrabList().size() + " Entry(s)");
	}
	private static ConfigList processGrabList(Set<ResourceLocation> entries, ConfigList cfg, boolean partialMatches, int type)
	{
		ConfigList list = new ConfigList();
    	String keyA, keyB, keyC;
    	List<String> keys;
    	List<Object> values;
    	boolean usePartialMatch = false;
    	int meta;
    	
    	if (cfg.isReplaceOnly())
    		list.setReplaceOnly();
    	
    	for (Entry<String, Object[]> entry : cfg.toMap().entrySet())
    	{
    		if (type > 0 && entry.getValue().length == 0) continue;
    		keyA = entry.getKey();
    		if (keyA.contains("#"))
    		{
    			try {meta = Integer.parseInt(keyA.replaceAll(".*\\#", ""));}
    			catch (Exception e) {meta = -1;}
    			keyA = keyA.replaceAll("\\#.*", "");
    		}
    		else
    			meta = -1;
    		
    		if (keyA.contains(":"))
    			keyB = keyA;
    		else
    		{
    			keyB = "minecraft:" + keyA;
    			usePartialMatch = partialMatches;
    		}
    		
    		keys = new ArrayList<String>();
    		values = new ArrayList<Object>();
    		for(ResourceLocation block : entries)
    		{
    			keyC = block.toString();
    			
    			if (keyC.equals(keyB) || usePartialMatch && keyC.toLowerCase().contains(keyA.toLowerCase()))
    				keys.add(keyC + (meta > -1 ? "#" + meta : ""));
    		}

    		for(Object str : entry.getValue())
    		{
    			switch (type)
    			{
    				case 1:
    					if (str instanceof String)
    	    			{
    	    				usePartialMatch = false;
    		    			keyA = (String) str;
    		    			
    		    			if (keyA.contains("#"))
    		        		{
    		        			try {meta = Integer.parseInt(keyA.replaceAll(".*\\#", ""));}
    		        			catch (Exception e) {meta = -1;}
    		        			keyA = keyA.replaceAll("\\#.*", "");
    		        		}
    		        		else
    		        			meta = -1;
    		    			
    		    			if (keyA.contains(":"))
    		    	    		keyB = keyA;
    		    	    	else
    		    	    	{
    		    	    		keyB = "minecraft:" + keyA;
    		    	    		usePartialMatch = partialMatches;
    		    	    	}
    		    				
    		    			for(ResourceLocation block : entries)
    		    	    	{
    		    	    		keyC = block.toString();
    		    	    			
    		    	    		if (keyC.equals(keyB) || usePartialMatch && keyC.toLowerCase().contains(keyA.toLowerCase()))
    		        				values.add(keyC + (meta > -1 ? "#" + meta : ""));
    		    	    	}
    	    			}
    					break;
    				case 2:
    					if (str instanceof Float)
    						values.add(str);
    					else if (str instanceof String)
    					{
    						try
    						{
    							float a = Float.parseFloat((String) str);
    							values.add(a);
    						}
    						catch (Exception e) {}
    					}
    					break;
    				default:
    					values.add(str);
    			}
    		}
    		
    		if (type > 0 && values.size() == 0) continue;
    		
    		for (String key : keys)
    		{
    			list.add(key, values.toArray());
    		}
    	}
    	return list;
	}
	public static void refreshRenders(boolean b) {
		Weather2Remastered.error("Sorry, refreshRenders isn't imported yet!");
		
	}
	public static ResourceLocation getParticleRendererId() {
		// TODO Auto-generated method stub
		return null;
	}
	public static AbstractWeatherRenderer getParticleRenderer(AbstractStormObject abstractStormObject) {
		// TODO Auto-generated method stub
		return null;
	}
	/**Gets the weather manager used in the world provided. There is a weather manager for each dimension.*/
	public static AbstractWeatherManager getManager(World world)
	{
		AbstractWeatherManager manager = null;
		if (world != null)
			if (world.isClientSide())
				manager = getManager();
			else
				manager = net.mrbt0907.weather2remastered.event.ServerTickHandler.dimensionSystems.get(world.dimension().location().toString());
		
		return manager;
	}
	/**Gets the weather manager used on the client.*/
	@OnlyIn(Dist.CLIENT)
	public static AbstractWeatherManager getManager()
	{
		return net.mrbt0907.weather2remastered.client.ClientTickHandler.weatherManager;
	}
	/**Gets the tornado stage list, which is used for rolling tornado stages*/
	public static ConfigList getTornadoStageList()
	{
		return tornadoStageList;
	}
	
	/**Gets the hurricane stage list, which is used for rolling hurricane stages*/
	public static ConfigList getHurricaneStageList()
	{
		return hurricaneStageList;
	}
	/**Refreshes the dimension rules. These rules determine if weather can spawn in a dimension and whether they can create effects in a dimension.*/
	public static void refreshDimensionRules()
	{
		EZConfigParser.refreshDimensionRules();
	}

}
