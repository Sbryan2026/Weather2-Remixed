package net.mrbt0907.weather2.network.packets;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

public class PacketShader extends PacketBase
{
    public static void refreshShaders(EntityPlayerMP player)
    {
        PacketBase.send(19, new NBTTagCompound(), player);
    }
}