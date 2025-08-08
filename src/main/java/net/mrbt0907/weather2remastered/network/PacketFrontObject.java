package net.mrbt0907.weather2remastered.network;

import net.minecraft.nbt.CompoundNBT;
import net.mrbt0907.weather2remastered.Weather2Remastered;
import net.mrbt0907.weather2remastered.api.weather.AbstractFrontObject;

public class PacketFrontObject extends PacketBase
{
	public static void create(Object target, AbstractFrontObject front)
	{
		Weather2Remastered.info("Creating new PacketFrontObject with target / front " + target + front);
		CompoundNBT nbt = new CompoundNBT();
		nbt.put("frontObject", front.writeNBT());
		nbt.putUUID("uuid", front.getUUID());
		send(11, nbt, target);
	}
	
	public static void update(Object target, AbstractFrontObject front)
	{
		CompoundNBT nbt = new CompoundNBT();
		nbt.put("frontObject", front.writeNBT());
		send(12, nbt, target);
	}
	
	public static void remove(String dimension, AbstractFrontObject front)
	{
		CompoundNBT nbt = new CompoundNBT();
		nbt.putUUID("frontUUID", front.getUUID());
		send(13, nbt, dimension);
	}
}
