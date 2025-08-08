package net.mrbt0907.weather2remastered.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.PacketDistributor;
import net.mrbt0907.weather2remastered.Weather2Remastered;

public class PacketBase
{
	public static void send(int command, CompoundNBT nbt, Object... target)
	{
		if (nbt == null)
		{
			Weather2Remastered.error("Network command #" + command + " returned null nbt data");
			return;
		}
		
		nbt.putInt("command", command);
		if (FMLEnvironment.dist.isClient() && (net.minecraft.client.Minecraft.getInstance().getCurrentServer() != null))
		{
			System.out.println("Sending PacketNBT " + new PacketNBT(nbt));
			Weather2Remastered.CHANNEL.sendToServer(new PacketNBT(nbt));
		}
		else
		{
			if (target.length == 0)
				Weather2Remastered.CHANNEL.send(PacketDistributor.ALL.noArg(), new PacketNBT(nbt));
			else if (target[0] instanceof String)
				Weather2Remastered.CHANNEL.send(PacketDistributor.DIMENSION.with(() -> dimensionKey(target[0])), new PacketNBT(nbt));
			else if (target[0] instanceof ServerPlayerEntity)
				Weather2Remastered.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) target[0]), new PacketNBT(nbt));
			else
				Weather2Remastered.error("Network packet #" + command + " returned an invalid target");
		}
	}

	private static RegistryKey<World> dimensionKey(Object object) {
		return RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation((String)object));
	}
}
