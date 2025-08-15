package net.mrbt0907.configex;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.OpEntry;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.configex.manager.ConfigInstance;
import net.mrbt0907.configex.network.NetField;
import net.mrbt0907.weather2remastered.Weather2Remastered;

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

	public static void save()
	{
		Weather2Remastered.debug("[ERROR] Can't save from ConfigManager just yet...");
	}

	@OnlyIn(Dist.CLIENT)
	public static int getPermissionLevel()
	{
		net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
		return mc.player != null ? getPermissionLevel(mc.player.getUUID()) : 4;
	}
	
	public static int getPermissionLevel(UUID uuid)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server != null)
		{
			PlayerList players = server.getPlayerList();
			ServerPlayerEntity player = players.getPlayer(uuid);
			if (player != null)
			{
				GameProfile profile = player.getGameProfile();
				if (server.isSingleplayer())
				{
					if (server.isSingleplayerOwner(profile))
						return 4;
					else
						return players.isAllowCheatsForAllPlayers() ? 4 : 0;
				}
				else
				{
					if (players.isOp(profile))
					{
						OpEntry opentry = players.getOps().get(profile);
						if (opentry != null) 
							return opentry.getLevel();
						else 
							return server.getOperatorUserPermissionLevel();
					}
					else
						return 0;
				}
			}
		}
		
		return 0;
	}
}