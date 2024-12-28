package net.mrbt0907.weather2.api.interfaces;

public interface IRadio<T>
{
	public void setRadioFrequency(T obj, String frequency);
	public String getRadioFrequency(T obj);
}
