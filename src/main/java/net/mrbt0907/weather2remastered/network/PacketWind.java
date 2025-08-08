package net.mrbt0907.weather2remastered.network;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraftforge.fml.InterModComms;
import net.mrbt0907.weather2remastered.Weather2Remastered;
import net.mrbt0907.weather2remastered.api.weather.AbstractWindManager;

public class PacketWind extends PacketBase
{
	public static void update(String dimension, AbstractWindManager wm) {
		CompoundNBT nbt = new CompoundNBT();
		nbt.put("manager", wm.nbtSyncForClient());
		send(6, nbt, dimension);
		InterModComms.sendTo(Weather2Remastered.MODID,"weather.wind", () -> nbt);
	}
}
