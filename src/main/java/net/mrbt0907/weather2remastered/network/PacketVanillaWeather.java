package net.mrbt0907.weather2remastered.network;

import net.minecraft.nbt.CompoundNBT;

public class PacketVanillaWeather extends PacketBase
{
	public static void send(String dim, int weatherID, float f)
	{
		CompoundNBT nbt = new CompoundNBT();
		nbt.putInt("weatherID", weatherID);
		nbt.putInt("weatherRainTime", (int) f);
		send(0, nbt, dim);
	}
}
