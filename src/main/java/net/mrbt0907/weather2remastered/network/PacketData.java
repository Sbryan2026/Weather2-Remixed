package net.mrbt0907.weather2remastered.network;

import net.minecraft.nbt.CompoundNBT;

public class PacketData extends PacketBase
{
	public static void sync()
	{
		CompoundNBT nbt = new CompoundNBT();
		nbt.putInt("command", 21);
		send(21, nbt);
	}
}
