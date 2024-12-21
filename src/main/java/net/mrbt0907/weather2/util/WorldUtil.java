package net.mrbt0907.weather2.util;

import java.io.File;

import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class WorldUtil
{
	private static final FMLCommonHandler HANDLER = FMLCommonHandler.instance();
	private static String cachedWorldName;
	
	public static boolean isSinglePlayer()
	{
		return HANDLER.getMinecraftServerInstance() == null || HANDLER.getMinecraftServerInstance().isSinglePlayer();
	}
			
	public static String getSaveFolder()
	{
		return isSinglePlayer() ? net.minecraft.client.Minecraft.getMinecraft().gameDir.getPath() + File.separator + "saves" + File.separator : new File(".").getAbsolutePath() + File.separator;
	}
	
	public static String getWorldFolder()
	{
		WorldServer world = DimensionManager.getWorld(0);
		if (world != null) cachedWorldName = world.getChunkSaveLocation().getName();
		return cachedWorldName;
	}
	
	public static String getWorldFile()
	{
		String worldFolder = getWorldFolder();
		return worldFolder == null ? null : getSaveFolder() + worldFolder + File.separator;
	}
}
