package net.mrbt0907.configex.network;

import net.minecraft.nbt.CompoundNBT;
import net.mrbt0907.configex.ConfigManager;

public class NetField
{
	public static void sendValue(CompoundNBT nbt, Object... targets)
	{
		if (ConfigManager.IS_REMOTE)
			NetworkHandler.sendServerPacket(0, nbt);
		else
			NetworkHandler.sendClientPacket(0, nbt, targets);
	}
}
