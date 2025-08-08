package net.mrbt0907.weather2remastered.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.io.FileInputStream;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraftforge.common.MinecraftForge;
import net.mrbt0907.configex.ConfigManager;
import net.mrbt0907.weather2remastered.Weather2Remastered;
import net.mrbt0907.weather2remastered.api.EZGUIAPI;
import net.mrbt0907.weather2remastered.event.EventEZGUIData;
import net.mrbt0907.weather2remastered.config.ConfigClient;
import net.mrbt0907.weather2remastered.config.ConfigFront;
import net.mrbt0907.weather2remastered.config.ConfigGrab;
import net.mrbt0907.weather2remastered.config.ConfigMisc;
import net.mrbt0907.weather2remastered.config.ConfigSand;
import net.mrbt0907.weather2remastered.config.ConfigStorm;
import net.mrbt0907.weather2remastered.util.TriMapEx;
import net.mrbt0907.weather2remastered.util.coro.CoroFile;

public class EZConfigParser
{
	public static final String version = "2.6";
	public static final Map<String, Integer> CLIENT_DEFAULTS = new HashMap<String, Integer>();
	public static final Map<String, Integer> SERVER_DEFAULTS = new HashMap<String, Integer>();
	private static List<String> weatherList = new ArrayList<String>();
	private static List<String> effectList = new ArrayList<String>();
	
	public static final Map<Integer, String> dimNames = new HashMap<Integer, String>();
	
	//actual data that gets written out to disk
	public static CompoundNBT nbtServerData = new CompoundNBT();
	public static CompoundNBT nbtRealServerData = new CompoundNBT();
	public static CompoundNBT nbtClientData = new CompoundNBT();

	public static void processServerData(CompoundNBT cache)
	{
		System.out.println("PROCESSING SERVER DATA");
		for(String key : cache.getAllKeys())
		{
			int value = cache.getInt(key);
			switch (key)
			{
				case EZGUIAPI.BB_GLOBAL:
					ConfigMisc.overcast_mode = value == 1;
					break;
				case EZGUIAPI.BC_ENABLE_TORNADO:
					ConfigStorm.disable_tornados = value == 0;
					break;
				case EZGUIAPI.BC_ENABLE_CYCLONE:
					ConfigStorm.disable_cyclones = value == 0;
					break;
				case EZGUIAPI.BC_ENABLE_SANDSTORM:
					ConfigSand.disable_sandstorms = value == 0;
					break;
				case EZGUIAPI.BC_FREQUENCY:
					switch(value)
					{
						case 0:
							ConfigFront.max_front_objects = 3;
							ConfigStorm.max_weather_objects = 30;
							ConfigStorm.storm_spawn_chance_min = 2;
							ConfigStorm.storm_spawn_chance_max = 5;
							ConfigStorm.storm_spawn_delay = 2000;
							ConfigSand.sandstorm_spawn_1_in_x = 300;
							ConfigSand.sandstorm_spawn_delay = 8000;
							break;
						case 1:
							ConfigFront.max_front_objects = 3;
							ConfigStorm.max_weather_objects = 30;
							ConfigStorm.storm_spawn_chance_min = 5;
							ConfigStorm.storm_spawn_chance_max = 10;
							ConfigStorm.storm_spawn_delay = 1250;
							ConfigSand.sandstorm_spawn_1_in_x = 200;
							ConfigSand.sandstorm_spawn_delay = 8000;
							break;
						case 2:
							ConfigFront.max_front_objects = 3;
							ConfigStorm.max_weather_objects = 30;
							ConfigStorm.storm_spawn_chance_min = 10;
							ConfigStorm.storm_spawn_chance_max = 15;
							ConfigStorm.storm_spawn_delay = 1000;
							ConfigSand.sandstorm_spawn_1_in_x = 100;
							ConfigSand.sandstorm_spawn_delay = 4000;
							break;
						case 3:
							ConfigFront.max_front_objects = 3;
							ConfigStorm.max_weather_objects = 30;
							ConfigStorm.storm_spawn_chance_min = 20;
							ConfigStorm.storm_spawn_chance_max = 30;
							ConfigStorm.storm_spawn_delay = 750;
							ConfigSand.sandstorm_spawn_1_in_x = 60;
							ConfigSand.sandstorm_spawn_delay = 2000;
							break;
						case 4:
							ConfigFront.max_front_objects = 4;
							ConfigStorm.max_weather_objects = 40;
							ConfigStorm.storm_spawn_chance_min = 35;
							ConfigStorm.storm_spawn_chance_max = 40;
							ConfigStorm.storm_spawn_delay = 650;
							ConfigSand.sandstorm_spawn_1_in_x = 40;
							ConfigSand.sandstorm_spawn_delay = 1500;
							break;
						case 5:
							ConfigFront.max_front_objects = 5;
							ConfigStorm.max_weather_objects = 50;
							ConfigStorm.storm_spawn_chance_min = 40;
							ConfigStorm.storm_spawn_chance_max = 50;
							ConfigStorm.storm_spawn_delay = 500;
							ConfigSand.sandstorm_spawn_1_in_x = 25;
							ConfigSand.sandstorm_spawn_delay = 1200;
							break;
						case 6:
							ConfigFront.max_front_objects = 5;
							ConfigStorm.max_weather_objects = 100;
							ConfigStorm.storm_spawn_chance_min = 100;
							ConfigStorm.storm_spawn_chance_max = 100;
							ConfigStorm.storm_spawn_delay = 200;
							ConfigStorm.storms_aim_at_player = true;
							ConfigStorm.storm_aim_accuracy_in_angle = 0;
							ConfigSand.sandstorm_spawn_1_in_x = 3;
							ConfigSand.sandstorm_spawn_delay = 1000;
							break;
					}
					break;
				case EZGUIAPI.BC_GRAB_BLOCK:
					ConfigGrab.grab_blocks = value == 1;
					break;
				case EZGUIAPI.BC_GRAB_ITEM:
					ConfigGrab.grab_items = value == 1;
					break;
				case EZGUIAPI.BC_GRAB_MOB:
					ConfigGrab.grab_villagers = value == 1;
					ConfigGrab.grab_animals = value == 1;
					ConfigGrab.grab_mobs = value == 1;
					break;
				case EZGUIAPI.BC_GRAB_PLAYER:
					ConfigGrab.grab_players = value == 1;
					break;
				case EZGUIAPI.BC_STORM_PER_PLAYER:
					ConfigStorm.enable_spawn_per_player = value == 0;
					ConfigSand.enable_global_rates_for_sandstorms = value == 0;
					break;
			}
			
			if (!key.equals("dimData"))
			{
				EventEZGUIData event = new EventEZGUIData(key, nbtClientData.getInt(key), value);
				MinecraftForge.EVENT_BUS.post(event);
			}
		}
		
		if (cache.contains("dimData"))
		{
			for(String key : cache.getCompound("dimData").getAllKeys())
			{
				if (key.contains("dimb_"))
				{
					String keyN = (key.replaceFirst("dimb_", ""));
					
					if (cache.getCompound("dimData").getString(key) == "minecraft:the_end")
						weatherList.add(keyN);
					else if (weatherList.contains(keyN))
						weatherList.remove(weatherList.indexOf(keyN));
				}
				else if (key.contains("dimc_"))
				{
					String keyN = (key.replaceFirst("dimc_", ""));
					
					if (cache.getCompound("dimData").getInt(key) == 1)
						effectList.add(keyN);
					else if (effectList.contains(keyN))
						effectList.remove(effectList.indexOf(keyN));
				}
			}
			String list = "";
			for (String dimension : weatherList)
				if (list.length() == 0)
					list = dimension + "";
				else
					list += ", " + dimension;
			ConfigMisc.dimensions_weather = list;

			list = "";
			for (String dimension : effectList)
				if (list.length() == 0)
					list = dimension + "";
				else
					list += ", " + dimension;
			ConfigMisc.dimensions_effects = list;
			refreshDimensionRules();
		}
		nbtSaveDataServer();
		ConfigManager.save();
	}
	
	public static void processClientData(CompoundNBT cache)
	{
		for(String key : cache.getAllKeys())
		{
			int value = cache.getInt(key);
			switch (key)
			{
				case EZGUIAPI.BA_CLOUD:
					switch(value)
					{
						case 0:
							ConfigClient.max_cloud_coverage_perc = 0.0D;
							ConfigClient.min_cloud_coverage_perc = 0.0D;
							ConfigClient.cloud_particle_delay = 666999;
							break;
						case 1:
							ConfigClient.max_cloud_coverage_perc = 15.0D;
							ConfigClient.min_cloud_coverage_perc = 0.0D;
							ConfigClient.cloud_particle_delay = 10;
							break;
						case 2:
							ConfigClient.max_cloud_coverage_perc = 25.0D;
							ConfigClient.min_cloud_coverage_perc = 10.0D;
							ConfigClient.cloud_particle_delay = 5;
							break;
						case 3:
							ConfigClient.max_cloud_coverage_perc = 50.0D;
							ConfigClient.min_cloud_coverage_perc = 20.0D;
							ConfigClient.cloud_particle_delay = 3;
							break;
						case 4:
							ConfigClient.max_cloud_coverage_perc = 80.0D;
							ConfigClient.min_cloud_coverage_perc = 250.0D;
							ConfigClient.cloud_particle_delay = 2;
							break;
						case 5:
							ConfigClient.max_cloud_coverage_perc = 100.0D;
							ConfigClient.min_cloud_coverage_perc = 50.0D;
							ConfigClient.cloud_particle_delay = 1;
							break;
						case 6:
							ConfigClient.max_cloud_coverage_perc = 200.0D;
							ConfigClient.min_cloud_coverage_perc = 50.0D;
							ConfigClient.cloud_particle_delay = 0;
							break;
					}
					break;
				case EZGUIAPI.BA_FUNNEL:
					switch(value)
					{
						case 0:
							ConfigClient.sandstorm_debris_particle_rate = 0.0D;
							ConfigClient.sandstorm_dust_particle_rate = 0.0D;
							ConfigClient.funnel_particle_delay = 666999;
							ConfigClient.ground_debris_particle_delay = 666999;
							ConfigClient.enable_tornado_block_colors = false;
							ConfigClient.enable_tornado_debris = false;
							break;
						case 1:
							ConfigClient.sandstorm_debris_particle_rate = 0.025D;
							ConfigClient.sandstorm_dust_particle_rate = 0.05D;
							ConfigClient.funnel_particle_delay = 45;
							ConfigClient.ground_debris_particle_delay = 20;
							ConfigClient.enable_tornado_block_colors = false;
							ConfigClient.enable_tornado_debris = false;
							break;
						case 2:
							ConfigClient.sandstorm_debris_particle_rate = 0.05D;
							ConfigClient.sandstorm_dust_particle_rate = 0.1D;
							ConfigClient.funnel_particle_delay = 20;
							ConfigClient.ground_debris_particle_delay = 10;
							ConfigClient.enable_tornado_block_colors = false;
							ConfigClient.enable_tornado_debris = false;
							break;
						case 3:
							ConfigClient.sandstorm_debris_particle_rate = 0.1D;
							ConfigClient.sandstorm_dust_particle_rate = 0.15D;
							ConfigClient.funnel_particle_delay = 10;
							ConfigClient.ground_debris_particle_delay = 5;
							ConfigClient.enable_tornado_block_colors = false;
							ConfigClient.enable_tornado_debris = false;
							break;
						case 4:
							ConfigClient.sandstorm_debris_particle_rate = 0.15D;
							ConfigClient.sandstorm_dust_particle_rate = 0.4D;
							ConfigClient.funnel_particle_delay = 5;
							ConfigClient.ground_debris_particle_delay = 5;
							ConfigClient.enable_tornado_block_colors = true;
							ConfigClient.enable_tornado_debris = false;
							break;
						case 5:
							ConfigClient.sandstorm_debris_particle_rate = 0.25D;
							ConfigClient.sandstorm_dust_particle_rate = 0.6D;
							ConfigClient.funnel_particle_delay = 2;
							ConfigClient.ground_debris_particle_delay = 3;
							ConfigClient.enable_tornado_block_colors = true;
							ConfigClient.enable_tornado_debris = true;
								break;
						case 6:
							ConfigClient.sandstorm_debris_particle_rate = 0.5D;
							ConfigClient.sandstorm_dust_particle_rate = 1.0D;
							ConfigClient.funnel_particle_delay = 0;
							ConfigClient.ground_debris_particle_delay = 0;
							ConfigClient.enable_tornado_block_colors = true;
							ConfigClient.enable_tornado_debris = true;
							break;
					}
					break;
				case EZGUIAPI.BA_PRECIPITATION:
					switch(value)
					{
						case 0:
							ConfigClient.enable_precipitation = true;
							ConfigClient.enable_precipitation_splash = false;
							ConfigClient.enable_heavy_precipitation = false;
							ConfigClient.enable_vanilla_rain = false;
							ConfigClient.precipitation_particle_rate = 0.0D;
							ConfigClient.enable_distant_downfall = false;
							ConfigClient.distant_downfall_particle_rate = 0.0F;
							break;
						case 1:
							ConfigClient.enable_precipitation = false;
							ConfigClient.enable_precipitation_splash = false;
							ConfigClient.enable_heavy_precipitation = false;
							ConfigClient.enable_vanilla_rain = true;
							ConfigClient.precipitation_particle_rate = 0.05D;
							ConfigClient.enable_distant_downfall = false;
							ConfigClient.distant_downfall_particle_rate = 0.4F;
							break;
						case 2:
							ConfigClient.enable_precipitation = true;
							ConfigClient.enable_precipitation_splash = false;
							ConfigClient.enable_heavy_precipitation = false;
							ConfigClient.enable_vanilla_rain = false;
							ConfigClient.precipitation_particle_rate = 0.2D;
							ConfigClient.enable_distant_downfall = false;
							ConfigClient.distant_downfall_particle_rate = 0.2F;
							break;
						case 3:
							ConfigClient.enable_precipitation = true;
							ConfigClient.enable_precipitation_splash = true;
							ConfigClient.enable_heavy_precipitation = false;
							ConfigClient.enable_vanilla_rain = false;
							ConfigClient.precipitation_particle_rate = 0.40D;
							ConfigClient.enable_distant_downfall = false;
							ConfigClient.distant_downfall_particle_rate = 0.2F;
							break;
						case 4:
							ConfigClient.enable_precipitation = true;
							ConfigClient.enable_precipitation_splash = true;
							ConfigClient.enable_heavy_precipitation = true;
							ConfigClient.enable_vanilla_rain = false;
							ConfigClient.precipitation_particle_rate = 0.65D;
							ConfigClient.enable_distant_downfall = true;
							ConfigClient.distant_downfall_particle_rate = 0.4F;
							break;
						case 5:
							ConfigClient.enable_precipitation = true;
							ConfigClient.enable_precipitation_splash = true;
							ConfigClient.enable_heavy_precipitation = true;
							ConfigClient.enable_vanilla_rain = false;
							ConfigClient.precipitation_particle_rate = 1.0D;
							ConfigClient.enable_distant_downfall = true;
							ConfigClient.distant_downfall_particle_rate = 0.6F;
							break;
						case 6:
							ConfigClient.enable_precipitation = true;
							ConfigClient.enable_precipitation_splash = true;
							ConfigClient.enable_heavy_precipitation = true;
							ConfigClient.enable_vanilla_rain = false;
							ConfigClient.precipitation_particle_rate = 1.4D;
							ConfigClient.enable_distant_downfall = true;
							ConfigClient.distant_downfall_particle_rate = 1.0F;
							break;
						case 7:
							ConfigClient.enable_precipitation = true;
							ConfigClient.enable_precipitation_splash = true;
							ConfigClient.enable_heavy_precipitation = true;
							ConfigClient.enable_vanilla_rain = false;
							ConfigClient.precipitation_particle_rate = 2.0D;
							ConfigClient.enable_distant_downfall = true;
							ConfigClient.distant_downfall_particle_rate = 2.0F;
							break;
					}
					break;
				case EZGUIAPI.BA_EFFECT:
					switch(value)
					{
						case 0:
							ConfigClient.enable_falling_leaves = false;
							ConfigClient.enable_fire_particle = false;
							ConfigClient.enable_waterfall_splash = false;
							ConfigClient.enable_wind_particle = false;
							ConfigClient.wind_particle_rate = 0.0D;
							ConfigClient.ambient_particle_rate = 0.0D;
							ConfigClient.fog_mult = 0.0D;
							ConfigClient.camera_shake_mult = 0.0F;
							break;
						case 1:
							ConfigClient.enable_falling_leaves = true;
							ConfigClient.enable_fire_particle = false;
							ConfigClient.enable_waterfall_splash = false;
							ConfigClient.enable_wind_particle = false;
							ConfigClient.wind_particle_rate = 0.0D;
							ConfigClient.ambient_particle_rate = 0.1D;
							ConfigClient.fog_mult = 0.0D;
							ConfigClient.camera_shake_mult = 0.0F;
							break;
						case 2:
							ConfigClient.enable_falling_leaves = true;
							ConfigClient.enable_fire_particle = true;
							ConfigClient.enable_waterfall_splash = false;
							ConfigClient.enable_wind_particle = false;
							ConfigClient.wind_particle_rate = 0.0D;
							ConfigClient.ambient_particle_rate = 0.2D;
							ConfigClient.fog_mult = 0.0D;
							ConfigClient.camera_shake_mult = 0.0F;
							break;
						case 3:
							ConfigClient.enable_falling_leaves = true;
							ConfigClient.enable_fire_particle = true;
							ConfigClient.enable_waterfall_splash = true;
							ConfigClient.enable_wind_particle = true;
							ConfigClient.wind_particle_rate = 0.1D;
							ConfigClient.ambient_particle_rate = 0.35D;
							ConfigClient.fog_mult = 0.15D;
							ConfigClient.camera_shake_mult = 0.25F;
							break;
						case 4:
							ConfigClient.enable_falling_leaves = true;
							ConfigClient.enable_fire_particle = true;
							ConfigClient.enable_waterfall_splash = true;
							ConfigClient.enable_wind_particle = true;
							ConfigClient.wind_particle_rate = 0.2D;
							ConfigClient.ambient_particle_rate = 0.6D;
							ConfigClient.fog_mult = 0.25D;
							ConfigClient.camera_shake_mult = 0.5F;
							break;
						case 5:
							ConfigClient.enable_falling_leaves = true;
							ConfigClient.enable_fire_particle = true;
							ConfigClient.enable_waterfall_splash = true;
							ConfigClient.enable_wind_particle = true;
							ConfigClient.wind_particle_rate = 0.25D;
							ConfigClient.ambient_particle_rate = 1.0D;
							ConfigClient.fog_mult = 0.6D;
							ConfigClient.camera_shake_mult = 1.0F;
							break;
						case 6:
							ConfigClient.enable_falling_leaves = true;
							ConfigClient.enable_fire_particle = true;
							ConfigClient.enable_waterfall_splash = true;
							ConfigClient.enable_wind_particle = true;
							ConfigClient.wind_particle_rate = 0.3D;
							ConfigClient.ambient_particle_rate = 2.0D;
							ConfigClient.fog_mult = 1.0D;
							ConfigClient.camera_shake_mult = 1.0F;
							break;
						case 7:
							ConfigClient.enable_falling_leaves = true;
							ConfigClient.enable_fire_particle = true;
							ConfigClient.enable_waterfall_splash = true;
							ConfigClient.enable_wind_particle = true;
							ConfigClient.wind_particle_rate = 0.5D;
							ConfigClient.ambient_particle_rate = 4.0D;
							ConfigClient.fog_mult = 1.0D;
							ConfigClient.camera_shake_mult = 1.0F;
							break;
					}
					break;
				case EZGUIAPI.BA_EF:
					ConfigStorm.enable_ef_scale = value == 1;
					break;
				case EZGUIAPI.BA_SHADER:
					switch(value)
					{
						case 0:
							//ConfigCoroUtil.particleShaders = true;
							//ConfigCoroUtil.useEntityRenderHookForShaders = true;
							ConfigMisc.proxy_render_override = true;
							break;
						case 1:
							//ConfigCoroUtil.particleShaders = false;
							//ConfigCoroUtil.useEntityRenderHookForShaders = false;
							ConfigMisc.proxy_render_override = true;
							break;
						case 2:
							//ConfigCoroUtil.particleShaders = false;
							//ConfigCoroUtil.useEntityRenderHookForShaders = false;
							ConfigMisc.proxy_render_override = true;
							break;
						case 3:
							//ConfigCoroUtil.particleShaders = true;
							//ConfigCoroUtil.useEntityRenderHookForShaders = true;
							ConfigMisc.proxy_render_override = true;
							break;
						case 4:
							//ConfigCoroUtil.particleShaders = true;
							//ConfigCoroUtil.useEntityRenderHookForShaders = false;
							ConfigMisc.proxy_render_override = false;
							break;
						case 5:
							//ConfigCoroUtil.particleShaders = true;
							//ConfigCoroUtil.useEntityRenderHookForShaders = false;
							ConfigMisc.proxy_render_override = false;
							break;
						case 6:
							//ConfigCoroUtil.particleShaders = true;
							//ConfigCoroUtil.useEntityRenderHookForShaders = false;
							ConfigMisc.proxy_render_override = true;
							break;
						case 7:
							//ConfigCoroUtil.particleShaders = true;
							//ConfigCoroUtil.useEntityRenderHookForShaders = false;
							ConfigMisc.proxy_render_override = false;
							break;
						case 8:
							//ConfigCoroUtil.particleShaders = true;
							//ConfigCoroUtil.useEntityRenderHookForShaders = false;
							ConfigMisc.proxy_render_override = false;
							break;
						case 9:
							//ConfigCoroUtil.particleShaders = true;
							//ConfigCoroUtil.useEntityRenderHookForShaders = false;
							ConfigMisc.proxy_render_override = false;
							break;
						case 10:
							//ConfigCoroUtil.particleShaders = true;
							//ConfigCoroUtil.useEntityRenderHookForShaders = false;
							ConfigMisc.proxy_render_override = false;
							break;
						case 11:
							//ConfigCoroUtil.particleShaders = false;
							//ConfigCoroUtil.useEntityRenderHookForShaders = false;
							ConfigMisc.proxy_render_override = false;
							break;
					}
					//ConfigMod.configLookup.get("coroutil_general").updateField("particleShaders", ConfigCoroUtil.particleShaders);
					//ConfigMod.configLookup.get("coroutil_general").updateField("useEntityRenderHookForShaders", ConfigCoroUtil.useEntityRenderHookForShaders);
					break;
				case EZGUIAPI.BA_FOLIAGE:
					//ConfigCoroUtil.foliageShaders = value == 1;
					break;

				case EZGUIAPI.BA_RENDER_DISTANCE:
					switch(value)
					{
						case 0:
							ConfigClient.enable_extended_render_distance = false;
							ConfigClient.extended_render_distance = 128.0D;
							ConfigClient.max_particles = 3000;
							break;
						case 1:
							ConfigClient.enable_extended_render_distance = true;
							ConfigClient.extended_render_distance = 128.0D;
							ConfigClient.max_particles = 3000;
							break;
						case 2:
							ConfigClient.enable_extended_render_distance = true;
							ConfigClient.extended_render_distance = 256.0D;
							ConfigClient.max_particles = 4000;
							break;
						case 3:
							ConfigClient.enable_extended_render_distance = true;
							ConfigClient.extended_render_distance = 384.0D;
							ConfigClient.max_particles = 5000;
							break;
						case 4:
							ConfigClient.enable_extended_render_distance = true;
							ConfigClient.extended_render_distance = 512.0D;
							ConfigClient.max_particles = -1;
							break;
						case 5:
							ConfigClient.enable_extended_render_distance = true;
							ConfigClient.extended_render_distance = 750.0D;
							ConfigClient.max_particles = -1;
							break;
						case 6:
							ConfigClient.enable_extended_render_distance = true;
							ConfigClient.extended_render_distance = 1028.0D;
							ConfigClient.max_particles = -1;
							break;
						case 7:
							ConfigClient.enable_extended_render_distance = true;
							ConfigClient.extended_render_distance = 2300.0D;
							ConfigClient.max_particles = -1;
							break;
					}

				case EZGUIAPI.BA_FANCY_RENDERING:
					ConfigClient.enable_legacy_rendering = value == 0;
					break;
				case EZGUIAPI.BA_RADAR:
					ConfigMisc.debug_mode_radar = value == 1;
					break;
				}
			
				if (!key.equals("dimData"))
				{
					EventEZGUIData event = new EventEZGUIData(key, nbtClientData.getInt(key), value);
					MinecraftForge.EVENT_BUS.post(event);
				}
			}
		
		nbtSaveDataClient();
		ConfigManager.save();
	}
	
	/**Used to get needed information from a client to add to the server data*/
	public static void nbtReceiveServer(CompoundNBT parNBT)
	{
		System.out.println("GOT NBT DATA");
		CompoundNBT cache = new CompoundNBT();
		String newKey;
		for (String key : parNBT.getAllKeys())
			if (key.matches("^" + EZGUI.PREFIX + ".+"))
			{
				newKey = key.replaceFirst("^" + EZGUI.PREFIX, "");
				cache.putInt(newKey, parNBT.getInt(key));
				nbtServerData.putInt(newKey, parNBT.getInt(key));
			}
		
		//also add dimension feature config, its iterated over
		cache.put("dimData", parNBT.getCompound("dimData"));
		nbtServerData.put("dimData", parNBT.getCompound("dimData"));
		
		Weather2Remastered.debug("Received server data from a client: " + parNBT);
		processServerData(cache);
	}
	
	/**Used to get needed information from server and client to add to the client data. Must have command. <br>0 - From Client<br>1 - From Server*/
	public static void nbtReceiveClient(CompoundNBT parNBT)
	{
		if (parNBT.contains("server"))
		{
			String newKey;
			if (parNBT.getInt("server") == 1)
			{
				nbtClientData.putBoolean("op", parNBT.getBoolean("op"));
				for (String key : parNBT.getAllKeys())
				{
					if (key.matches("^" + EZGUI.PREFIX + ".+"))
					{
						newKey = key.replaceFirst("^" + EZGUI.PREFIX, "");
						nbtRealServerData.putInt(newKey, parNBT.getInt(key));
					}
					else if (nbtServerData.contains(key))
						nbtRealServerData.putInt(key, parNBT.getInt(key));
				}
				CompoundNBT dimensions = parNBT.getCompound("dimData");
				if (dimensions != null)
				{
					weatherList.clear();
					effectList.clear();
					
					for(String name : dimensions.getAllKeys())
						if (name.contains("dima_"))
							dimNames.put(Integer.parseInt(name.replaceFirst("dima_", "")), dimensions.getString(name));
						else if (name.contains("dimb_"))
							weatherList.add((name.replaceFirst("dimb_", "")));
						else if (name.contains("dimc_"))
							effectList.add(name.replaceFirst("dimc_", ""));;
				}
				Weather2Remastered.debug("Received server data from the server: " + parNBT);
			}
			else
			{
				CompoundNBT cache = new CompoundNBT();
				for (String key : parNBT.getAllKeys())
					if (key.matches("^" + EZGUI.PREFIX + ".+"))
					{
						newKey = key.replaceFirst("^" + EZGUI.PREFIX, "");
						cache.putInt(newKey, parNBT.getInt(key));
						nbtClientData.putInt(newKey, parNBT.getInt(key));
					}
				
				Weather2Remastered.debug("Received client data from self: " + parNBT);
				processClientData(cache);
			}
		}
	}
	
	public static boolean isOp()
	{
		return nbtClientData.getBoolean("op");
	}
	
	public static void nbtSaveDataClient()
	{
		nbtWriteNBTToDisk(nbtClientData, true);
	}
	
	public static void nbtSaveDataServer()
	{
		nbtWriteNBTToDisk(nbtServerData, false);
	}
	
	public static void loadNBT()
	{
		EZGUIAPI.refreshOptions();
		nbtClientData = nbtReadNBTFromDisk(true);
		nbtServerData = nbtReadNBTFromDisk(false);
		checkVersion();
		TriMapEx<String, List<String>, Integer> options = EZGUIAPI.getOptions();
		Map<String, Integer> optionCategories = EZGUIAPI.getOptionCategories();
		String index;
		for(Entry<String, Integer> entry : optionCategories.entrySet())
		{
			index = entry.getKey();
			if (entry.getValue() == 0)
				CLIENT_DEFAULTS.put(index, options.getB(index));
			else
				SERVER_DEFAULTS.put(index, options.getB(index));
		}
		
		for (String key : CLIENT_DEFAULTS.keySet())
			if (!nbtClientData.contains(key))
				nbtClientData.putInt(key, CLIENT_DEFAULTS.get(key));
		for (String key : SERVER_DEFAULTS.keySet())
			if (!nbtServerData.contains(key))
				nbtServerData.putInt(key, SERVER_DEFAULTS.get(key));
	}
	
	public static int getConfigValue(String buttonID)
	{
		if (nbtClientData.contains(buttonID))
			return nbtClientData.getInt(buttonID);
		else if (nbtRealServerData.contains(buttonID))
			return nbtRealServerData.getInt(buttonID);
		else if (nbtServerData.contains(buttonID))
			return nbtServerData.getInt(buttonID);
		else
			return 0;
	}
	
	public static List<String> parseList(String sList)
	{
		String[] arrStr = sList.split("[\\s\\,]+");
		List<String> arrInt = new ArrayList<String>();
		for (int i = 0; i < arrStr.length; i++)
		{
			try {arrInt.add(arrStr[i]);}
			catch (Exception ex) {Weather2Remastered.debug("Entry was not a string: " + arrStr[i]);}
		}
		return arrInt;
	}
	
	private static void checkVersion()
	{
		if (!nbtServerData.contains("version") || !nbtServerData.getString("version").equalsIgnoreCase(version))
		{
			Weather2Remastered.debug("Detected old EZ server data, reseting everything to default...");
			nbtServerData = new CompoundNBT();
			nbtServerData.putString("version", version);
			nbtSaveDataServer();
		}
		if (!nbtClientData.contains("version") || !nbtClientData.getString("version").equalsIgnoreCase(version))
		{
			Weather2Remastered.debug("Detected old EZ client data, reseting everything to default...");
			nbtClientData = new CompoundNBT();
			nbtClientData.putString("version", version);
			nbtSaveDataClient();
		}
	}
	
	public static void nbtWriteNBTToDisk(CompoundNBT parData, boolean saveForClient)
	{
		Weather2Remastered.debug("Saving EZ Gui " + (saveForClient ? "client" : "server") + " data...");
		String fileURL = null;
		
		if (saveForClient)
			fileURL = CoroFile.getMinecraftSaveFolderPath() + File.separator + Weather2Remastered.MODID + File.separator + "EZGUIConfigClientData.dat";
		else
			fileURL = CoroFile.getMinecraftSaveFolderPath() + File.separator + Weather2Remastered.MODID + File.separator + "EZGUIConfigServerData.dat";
		
		try
		{
			FileOutputStream fos = new FileOutputStream(fileURL);
	    	CompressedStreamTools.writeCompressed(parData, fos);
	    	fos.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Weather2Remastered.debug("Error writing Weather2 EZ GUI data, unable to save data");
		}
	}
	
	public static CompoundNBT nbtReadNBTFromDisk(boolean loadForClient)
	{
		Weather2Remastered.debug("Loading EZ Gui " + (loadForClient ? "client" : "server") + " data...");
		CompoundNBT data = new CompoundNBT();
		String fileURL = null;
		if (loadForClient)
			fileURL = CoroFile.getMinecraftSaveFolderPath() + File.separator + Weather2Remastered.MODID + File.separator + "EZGUIConfigClientData.dat";
		else
			fileURL = CoroFile.getMinecraftSaveFolderPath() + File.separator + Weather2Remastered.MODID + File.separator + "EZGUIConfigServerData.dat";
		
		try
		{
			if ((new File(fileURL)).exists())
				data = CompressedStreamTools.readCompressed(new FileInputStream(fileURL));
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Weather2Remastered.debug("Error reading Weather2 EZ GUI data, resetting data to default...");
		}
		return data;
	}

	public static void setOvercastModeServerSide(boolean val)
	{
		nbtSaveDataServer();
	}
	
	public static boolean isWeatherEnabled(String registryKey)
	{
		return weatherList.contains(registryKey);
	}
	
	public static boolean isEffectsEnabled(String registryKey)
	{
		return effectList.contains(registryKey);
	}
	
	public static void refreshDimensionRules()
	{
		weatherList = parseList(ConfigMisc.dimensions_weather);
		effectList = parseList(ConfigMisc.dimensions_effects);
		nbtServerData.put("dimData", new CompoundNBT());
		String list = "Dimension Rules have been refreshed\nWeather:";
		for (String dim : weatherList)
		{
			nbtServerData.getCompound("dimData").putString("dimb_" + dim, "1");
			list += " " + dim;
		}
		list += "\nEffects:";
		for (String dim : effectList)
		{
			nbtServerData.getCompound("dimData").putString("dimc_" + dim, "1");
			list += " " + dim;
		}
		System.out.println(list);
	}
}