package net.mrbt0907.weather2remastered.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.mrbt0907.weather2remastered.Weather2Remastered;
import net.mrbt0907.weather2remastered.config.ConfigMisc;
import net.mrbt0907.weather2remastered.gui.EZConfigParser;
import net.mrbt0907.weather2remastered.network.PacketEZGUI;
import net.mrbt0907.weather2remastered.weather.WeatherManagerServer;

public class ServerTickHandler
{
	//Main lookup method for dim to weather systems
	public static Map<String, WeatherManagerServer> dimensionSystems = new HashMap<String, WeatherManagerServer>();
	public static World lastWorld;
	public static CompoundNBT worldNBT = new CompoundNBT(); 
	
	public static void onTickInGame()
	{
		if (LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER) == null)
		{
			return;
		}
		MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
		World world = server.getLevel(World.OVERWORLD);
		
		if (world != null && lastWorld != world)
		{
			lastWorld = world;
		}
		
		//regularly save data
		if (world != null)
			if (world.getGameTime() % ConfigMisc.auto_save_interval == 0)
				Weather2Remastered.writeOutData(false);
		
		World[] worlds = StreamSupport.stream(server.getAllLevels().spliterator(), false).toArray(ServerWorld[]::new);
		World dim;
		List<String> removedManagers = new ArrayList<String>();
		int size = worlds.length;
		//add use of CSV of supported dimensions here once feature is added, for now just overworld
		
		for (int i = 0; i < size; i++)
		{
			dim = worlds[i];
			String dimension = World.OVERWORLD.location().toString();

			if (!dimensionSystems.containsKey(dimension))
			{
				if (EZConfigParser.isWeatherEnabled(dimension)){
					addWeatherSystem(dim);
				}
				if (!EZConfigParser.dimNames.containsValue(dimension))
				{
					EZConfigParser.dimNames.put(i, dim.dimension().getRegistryName().toString());
					EZConfigParser.nbtServerData.getCompound("dimData").putString("dima_" + i, dim.dimension().getRegistryName().toString());
					EZConfigParser.nbtSaveDataServer();
				}
			}
			
			if (dimensionSystems.containsKey(dimension))
			{
				if (EZConfigParser.isWeatherEnabled(dimension))
					dimensionSystems.get(dimension).tick();
				else
					removedManagers.add(dimension);
			}
		}

		for (String i : removedManagers)
			removeWeatherSystem(i);
		
		if (ConfigMisc.aesthetic_mode)
		{
			if (!ConfigMisc.overcast_mode)
			{
				ConfigMisc.overcast_mode = true;
				Weather2Remastered.debug("detected Aesthetic_Only_Mode on, setting overcast mode on");
				EZConfigParser.setOvercastModeServerSide(ConfigMisc.overcast_mode);
				Weather2Remastered.error("Can't force save all files from runtime settings...");
				//ConfigMod.forceSaveAllFilesFromRuntimeSettings();
				syncServerConfigToClient();
			}
		}

		//TODO: only sync when things change? is now sent via PlayerLoggedInEvent at least
		if (world.getGameTime() % 200 == 0)
			syncServerConfigToClient();
	}
	
	//must only be used when world is active, soonest allowed is TickType.WORLDLOAD
	public static void addWeatherSystem(World world)
	{
		String dim = world.dimension().location().toString();
		Weather2Remastered.debug("Registering Weather2 manager for dim: " + dim);
		WeatherManagerServer wm = new WeatherManagerServer(world);
		dimensionSystems.put(dim, wm);
		wm.readFromFile();
	}
	
	public static void removeWeatherSystem(String i)
	{
		Weather2Remastered.debug("Unregistering manager for dim: " + i);
		WeatherManagerServer wm = dimensionSystems.get(i);
		
		try
		{
			if (wm != null)
			{
				dimensionSystems.remove(i);
				wm.writeToFile();
				wm.reset(true);
			}
		}
		catch(Exception e)
		{
			Weather2Remastered.error(e);
		}
	}

	public static void playerClientRequestsFullSync(ServerPlayerEntity entP) {
		WeatherManagerServer wm = dimensionSystems.get(entP.level.dimension().location().toString());
		if (wm != null) {
			wm.playerJoinedWorldSyncFull(entP);
		}
	}
	
	public static void reset() {
		Weather2Remastered.debug("Weather2: ServerTickHandler resetting");
		for (String key : dimensionSystems.keySet())
				removeWeatherSystem(key);
		//should never happen
		if (dimensionSystems.size() > 0)
		{
			Weather2Remastered.debug("Weather2: reset state failed to manually clear lists, dimensionSystems.size(): " + dimensionSystems.size() + " - forcing a full clear of lists");
			dimensionSystems.clear();
		}
	}
	
	public static WeatherManagerServer getWeatherSystemForDim(String dimID) {
		return dimensionSystems.get(dimID);
	}

	public static void syncServerConfigToClient() {
		//packets
		CompoundNBT data = new CompoundNBT();
		//ClientConfigData.writeNBT(data);
		PacketEZGUI.apply(data);
	}

	public static void syncServerConfigToClientPlayer(ServerPlayerEntity player) {
		//packets
		CompoundNBT data = new CompoundNBT();
		//ClientConfigData.writeNBT(data);
		PacketEZGUI.apply(data, player);
	}
	public static RegistryKey<World> dimensionKey (String dim) {
		return RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(dim));
	}
}
