package net.mrbt0907.weather2.api.interfaces;

import net.minecraft.util.ResourceLocation;

public interface IRadioTransmitter<T> extends IRadio<T>
{	
	public void setRadioMessage(T obj, String message);
	public String getRadioMessage(T obj);

	public void setRadioSound(T obj, ResourceLocation sound);
	public ResourceLocation getRadioSound(T obj);
}