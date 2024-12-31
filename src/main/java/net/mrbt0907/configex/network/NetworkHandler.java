package net.mrbt0907.configex.network;

import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.mrbt0907.configex.ConfigModEX;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;

public class NetworkHandler
{
	public static final SimpleChannel instance = NetworkRegistry.newSimpleChannel(new ResourceLocation(ConfigModEX.MODID), () -> "1.0.0", version -> version.equals("1.0.0"), version -> version.equals("1.0.0"));
	private static int ID = -1;
	
	public static void preInit()
	{
		register(PacketNBT.class, PacketNBT::onEncode, PacketNBT::onDecode, PacketNBT::handle);
	}
	
	@OnlyIn(Dist.CLIENT)
	public static void onClientMessage(int index, CompoundNBT nbt)
	{
		net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
		
		switch(index)
		{	
			default:
				ConfigModEX.warn("Network Handler recieved an invalid packet with index of " + index + ". Skipping...");
		}
	}
	
	public static void onServerMessage(int index, CompoundNBT nbt, ServerPlayerEntity player)
	{
		switch(index)
		{
			
			default:
				ConfigModEX.warn("Network Handler recieved an invalid packet with index of " + index + ". Skipping...");
		}
	}
	
	public static boolean sendClientPacket(int index, CompoundNBT nbt, Object... targets)
	{
		return sendClientPacket(new PacketNBT(index, nbt), targets);
	}
	
	@SuppressWarnings("unchecked")
	public static <MSG> boolean sendClientPacket(MSG message, Object... targets)
	{
		if (message == null) return false;
		if (targets != null && targets.length > 0)
			for (Object target : targets)
			{
				if (target instanceof RegistryKey<?>)
				{
					instance.send(PacketDistributor.DIMENSION.with((Supplier<RegistryKey<World>>) target), message);
				}
				else if (target instanceof ServerPlayerEntity)
					instance.send(PacketDistributor.PLAYER.with((Supplier<ServerPlayerEntity>) target), message);
			}
		else
			instance.send(PacketDistributor.ALL.noArg(), message);
		
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	public static boolean sendServerPacket(int index, CompoundNBT nbt)
	{
		return sendServerPacket(new PacketNBT(index, nbt));
	}
	
	@OnlyIn(Dist.CLIENT)
	public static <MSG> boolean sendServerPacket(MSG message)
	{
		if (message == null) return false;
		instance.sendToServer(message);
		return true;
	}
	
	private static <MSG> void register(Class<MSG> message, BiConsumer<MSG, PacketBuffer> encoder, Function<PacketBuffer, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer)
	{
		instance.registerMessage(ID++, message, encoder, decoder, messageConsumer);
	}
}
