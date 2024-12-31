package net.mrbt0907.configex.network;
import java.util.function.Supplier;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketNBT
{
	public int index;
	public CompoundNBT nbt;
	
	public PacketNBT(PacketBuffer buffer)
	{
		index = buffer.readInt();
		nbt = buffer.readNbt();
	}
	
	public PacketNBT(int index, CompoundNBT nbt)
	{
		this.index = index;
		this.nbt = nbt;
	}
	
	public void encode(PacketBuffer buffer)
	{
		buffer.writeInt(index);
		buffer.writeNbt(nbt);
	}
	
	public static void handle(PacketNBT packet, Supplier<Context> supplier)
	{
		Context context = supplier.get();
		if (context.getDirection().getReceptionSide().equals(LogicalSide.CLIENT))
		{
			context.enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> NetworkHandler.onClientMessage(packet.index, packet.nbt));
			});
		}
		else
			context.enqueueWork(() ->
			{
				NetworkHandler.onServerMessage(packet.index, packet.nbt, context.getSender());
			});
		
		context.setPacketHandled(true);
	}
	
	public static PacketNBT onDecode(PacketBuffer buffer)
	{
		return new PacketNBT(buffer);
	}
	
	public static void onEncode(PacketNBT packet, PacketBuffer buffer)
	{
		packet.encode(buffer);
	}
}