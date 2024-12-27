package net.mrbt0907.weather2.network.packets;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.mrbt0907.weather2.Weather2;

public class PacketLightning extends PacketBase
{
	public static void spawnLightning(int dimension, Entity entity)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("posX", MathHelper.floor(entity.posX/* * 32.0D*/));
		nbt.setInteger("posY", MathHelper.floor(entity.posY/* * 32.0D*/));
		nbt.setInteger("posZ", MathHelper.floor(entity.posZ/* * 32.0D*/));
		nbt.setInteger("entityID", entity.getEntityId());
		send(7, nbt, dimension);
		FMLInterModComms.sendRuntimeMessage(Weather2.instance, Weather2.MODID, "weather.lightning", nbt);
	}
	
	public static void spawnInvisibleLightning(int dimension, double x, double y, double z)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("posX", MathHelper.floor(x));
		nbt.setInteger("posY", MathHelper.floor(y));
		nbt.setInteger("posZ", MathHelper.floor(z));
		send(7, nbt, dimension);
		FMLInterModComms.sendRuntimeMessage(Weather2.instance, Weather2.MODID, "weather.lightning", nbt);
	}
}
