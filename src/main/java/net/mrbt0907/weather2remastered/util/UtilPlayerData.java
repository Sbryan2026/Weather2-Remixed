package net.mrbt0907.weather2remastered.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.UUID;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.mrbt0907.weather2remastered.Weather2Remastered;
import net.mrbt0907.weather2remastered.util.coro.CoroFile;

public class UtilPlayerData {

	public static HashMap<UUID, CompoundNBT> playerNBT = new HashMap<UUID, CompoundNBT>();
	
	public static CompoundNBT getPlayerNBT(UUID playerUUID) {
		if (!playerNBT.containsKey(playerUUID))
			tryLoadPlayerNBT(playerUUID);
		
		return playerNBT.get(playerUUID);
	}
	
	public static void tryLoadPlayerNBT(UUID playerUUID)
	{
		CompoundNBT nbt = new CompoundNBT();
		
		try
		{
			String fileURL = CoroFile.getWorldSaveFolderPath() + CoroFile.getWorldFolderName(ServerLifecycleHooks.getCurrentServer().overworld()) + File.separator + "weather2remastered" + File.separator + "PlayerData" + File.separator + playerUUID.toString() + ".dat";
			
			if ((new File(fileURL)).exists())
			{
				nbt = CompressedStreamTools.readCompressed(new FileInputStream(fileURL));
			}
		} catch (Exception ex) {
			//Weather.dbg("no saved data found for " + username);
		}
		
		playerNBT.put(playerUUID, nbt);
	}
	
	public static void writeAllPlayerNBT(boolean resetData)
	{		
		String fileURL = CoroFile.getWorldSaveFolderPath() + CoroFile.getWorldFolderName(ServerLifecycleHooks.getCurrentServer().overworld()) + File.separator + "weather2remastered" + File.separator + "PlayerData";
		if (!new File(fileURL).exists()) new File(fileURL).mkdir();
		
		playerNBT.forEach((uuid, nbt) -> writePlayerNBT((uuid), nbt));
	    
	    if (resetData)
	    	playerNBT.clear();
	}
	
	public static void writePlayerNBT(UUID playerUUID, CompoundNBT nbt)
	{
		String fileURL = CoroFile.getWorldSaveFolderPath() + CoroFile.getWorldFolderName(ServerLifecycleHooks.getCurrentServer().overworld()) + File.separator + "weather2remastered" + File.separator + "PlayerData" + File.separator + playerUUID.toString() + ".dat";
		
		try
		{
			FileOutputStream fos = new FileOutputStream(fileURL);
	    	CompressedStreamTools.writeCompressed(nbt, fos);
	    	fos.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Weather2Remastered.debug("Error writing Weather2 player data for " + playerUUID.toString());
		}
	}
	
}
