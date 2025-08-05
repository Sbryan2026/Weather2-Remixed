package net.mrbt0907.weather2remastered.util.coro;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.storage.FolderName;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CoroFile {
public static String lastWorldFolder = "";
    
	public static CompoundNBT getExtraWorldNBT(String fileName, World world) {
		CompoundNBT data = new CompoundNBT();
		//try load
		
		String saveFolder = getWorldSaveFolderPath() + getWorldFolderName(world);
		
		if ((new File(saveFolder + fileName)).exists()) {
			try {
				data = CompressedStreamTools.readCompressed(new FileInputStream(saveFolder + fileName));
			} catch (Exception ex) {
				System.out.println("CoroUtilFile: getExtraWorldNBT: Error loading " + saveFolder + fileName);
			}
		}
		
		return data;
	}
	
	public static void setExtraWorldNBT(String fileName, World world, CompoundNBT data) {
		try {
    		String saveFolder = getWorldSaveFolderPath() + getWorldFolderName(world);
    		//Write out to file
    		FileOutputStream fos = new FileOutputStream(saveFolder + fileName);
	    	CompressedStreamTools.writeCompressed(data, fos);
	    	fos.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	//this must be used while server is active
    public static String getWorldFolderName(World world) {
		if (world != null) {
			lastWorldFolder = world.getServer().getWorldPath(FolderName.ROOT).toString();
			return lastWorldFolder + File.separator;
		}
		
		return lastWorldFolder + File.separator;
	}
	
	public static String getSaveFolderPath() {
		if (Minecraft.getInstance().getCurrentServer() == null || Minecraft.getInstance().hasSingleplayerServer()) {
    		return getClientSidePath() + File.separator;
    	} else {
    		return new File(".").getAbsolutePath() + File.separator;
    	}
    	
    }
	
	public static String getMinecraftSaveFolderPath() {
		if (Minecraft.getInstance().getCurrentServer() == null || Minecraft.getInstance().hasSingleplayerServer()) {
    		return getClientSidePath() + File.separator + "config" + File.separator;
    	} else {
    		return new File(".").getAbsolutePath() + File.separator + "config" + File.separator;
    	}
    }
	
	public static String getWorldSaveFolderPath() {
    	if (Minecraft.getInstance().getCurrentServer() == null || Minecraft.getInstance().hasSingleplayerServer()) {
    		return getClientSidePath() + File.separator + "saves" + File.separator;
    	} else {
    		return new File(".").getAbsolutePath() + File.separator;
    	}
    }
    
    @OnlyIn(Dist.CLIENT)
	public static String getClientSidePath() {
		return Minecraft.getInstance().gameDirectory.getPath();
	}
    
    public static void writeCoords(String name, CoroBlockCoord coords, CompoundNBT nbt) {
    	nbt.putInt(name + "X", coords.posX);
    	nbt.putInt(name + "Y", coords.posY);
    	nbt.putInt(name + "Z", coords.posZ);
    }
    
    public static CoroBlockCoord readCoords(String name, CompoundNBT nbt) {
    	if (nbt.contains(name + "X")) {
    		return new CoroBlockCoord(nbt.getInt(name + "X"), nbt.getInt(name + "Y"), nbt.getInt(name + "Z"));
    	} else {
    		return null;
    	}
    }

    @OnlyIn(Dist.CLIENT)
    public static String getContentsFromResourceLocation(ResourceLocation resourceLocation) {
		try {
			IResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
			IResource iresource = resourceManager.getResource(resourceLocation);
			String contents = IOUtils.toString(iresource.getInputStream(), StandardCharsets.UTF_8);
			return contents;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return "";
	}
}
