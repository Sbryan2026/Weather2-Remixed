package net.mrbt0907.weather2remastered.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.mrbt0907.weather2remastered.Weather2Remastered;
import net.mrbt0907.weather2remastered.api.weather.AbstractWeatherObject;

public class PacketWeatherObject extends PacketBase
{
	public static void create(Object target, AbstractWeatherObject wo)
	{
		if (wo.nbt == null) {
			Weather2Remastered.error("wo is not null, but wo.nbt is? wat?");
			return;
		}
		CompoundNBT nbt = new CompoundNBT();
		wo.nbt.setUpdateForced(true);
		wo.writeToNBT();
		wo.nbt.setUpdateForced(false);
		nbt.put("weatherObject", wo.nbt.getNewNBT());
		send(1, nbt, target);
	}
	
	public static void update(Object target, AbstractWeatherObject wo)
	{
		CompoundNBT nbt = new CompoundNBT();
		wo.nbt.setNewNBT(new CompoundNBT());
		wo.writeToNBT();
		nbt.put("weatherObject", wo.nbt.getNewNBT());
		send(2, nbt, target);
	}
	
	public static void remove(String dimension, AbstractWeatherObject wo)
	{
		CompoundNBT nbt = new CompoundNBT();
		nbt.putUUID("frontObject", wo.front.getUUID());
		nbt.putUUID("weatherObject", wo.getUUID());
		send(3, nbt, dimension);
	}
	
	public static void clientCleanup(ServerPlayerEntity player)
	{
		send(14, new CompoundNBT(), player);
	}
}
