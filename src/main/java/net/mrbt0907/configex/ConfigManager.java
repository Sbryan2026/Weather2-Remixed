package net.mrbt0907.configex;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.configex.manager.ConfigInstance;
import net.mrbt0907.configex.network.NetField;

public class ConfigManager
{
	private static final Map<String, ConfigInstance> configs = new LinkedHashMap<String, ConfigInstance>();
	public static final boolean IS_REMOTE = FMLEnvironment.dist.equals(Dist.CLIENT);
	
	public static void register(IConfigEX config)
	{
		ConfigInstance instance = new ConfigInstance(config);
		configs.put(instance.registryName, instance);
	}
	
	public static void readNBT(CompoundNBT nbt, int permissionLevel)
	{
		if (!nbt.isEmpty()) return;
		for (ConfigInstance instance : configs.values())
			instance.readNBT(nbt, permissionLevel);
	}
	
	public static ConfigInstance get(String registryName)
	{
		return configs.get(registryName);
	}
	
	public static List<ResourceLocation> ids()
	{
		List<ResourceLocation> variables = new ArrayList<ResourceLocation>();
		for (ConfigInstance instance : configs.values())
			instance.ids(variables);
		return variables;
	}
	
	public static void sync(PlayerEntity... players)
	{
		CompoundNBT nbt = new CompoundNBT();
		for (ConfigInstance instance : configs.values())
			instance.writeNBT(nbt, false);
		NetField.sendValue(nbt, (Object[]) players);
	}
	
	public static void syncAll(PlayerEntity... players)
	{
		CompoundNBT nbt = new CompoundNBT();
		for (ConfigInstance instance : configs.values())
			instance.writeNBT(nbt, true);
		NetField.sendValue(nbt, (Object[]) players);
	}
	
	public static void onConfigLoaded(ForgeConfigSpec spec)
	{
		configs.forEach((registryName, config) ->
		{
			if (config.clientCFG != null && spec.hashCode() == config.clientCFG.hashCode() || config.commonCFG != null && spec.hashCode() == config.commonCFG.hashCode() || config.serverCFG != null && spec.hashCode() == config.serverCFG.hashCode())
				config.load();
		});
	}
	
	public static String formatRegistryName(String name)
	{
		return name.toLowerCase().replaceAll("[^a-z0-9\\\\-\\\\_ ]*", "").replaceAll(" +", "_");
	}

	public static void save() {
		System.out.println("Can't save from ConfigManager just yet...");
		// TODO Auto-generated method stub
		
	}
}