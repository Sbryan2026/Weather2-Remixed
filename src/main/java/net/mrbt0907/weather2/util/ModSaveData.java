package net.mrbt0907.weather2.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.mrbt0907.weather2.Weather2;
/**
This class allows for variables to be automatically saved and loaded without having to manually update variables during save and load.
 */
public class ModSaveData
{
	/**A reference to the instance that this class will update*/
	private final Object instance;
	/**The save file location relative to the world save folder*/
	private final String saveLocation;
	/**A list of variables to save to the world*/
	private final List<String> variables = new ArrayList<String>();
	/**A map of variables that have custom save and load methods.*/
	private final Map<String, Function<Object, NBTTagCompound>> variableFunction = new HashMap<String, Function<Object, NBTTagCompound>>();
	private final Map<String, Consumer<NBTTagCompound>> variableConsumer = new HashMap<String, Consumer<NBTTagCompound>>();
	
	/**
	This class allows for variables to be automatically saved and loaded without having to manually update variables during save and load.
	 */
	public ModSaveData(Object instance, String saveLocation, String... variables)
	{
		if (instance == null)
			Weather2.fatal("Tried to create ModSaveData with a null instance");
		if (saveLocation.endsWith(File.separator))
			Weather2.fatal("Tried to create ModSaveData with a folder instead of a file. Location: " + saveLocation);
		this.instance = instance;
		this.saveLocation = saveLocation;
		addVariables(variables);
		load();
	}
	
	/**Adds a variable to be automatically saved and loaded. If you need to save unsupported variables, use addVariable()*/
	public void addVariables(String... variables)
	{
		for(String variable : variables)
			addVariable(variable, null, null);
	}
	
	public void addVariable(String variable, Function<Object, NBTTagCompound> saveMethod, Consumer<NBTTagCompound> loadMethod)
	{
		if (!variables.contains(variable))
		{
			variables.add(variable);
			if (saveMethod != null && loadMethod != null)
			{
				variableFunction.put(variable, saveMethod);
				variableConsumer.put(variable, loadMethod);
			}
		}
	}

	/**Loads all save data and updates all variables assigned*/
	public boolean load()
	{
		String worldFolder = WorldUtil.getWorldFile();
		if (worldFolder == null) return false;
		worldFolder += saveLocation;
		
		return true;
	}
	
	/**Saves data from all variables assigned to file*/
	public boolean save()
	{
		String worldFolder = WorldUtil.getWorldFile();
		if (worldFolder == null) return false;
		worldFolder += saveLocation;
		
		return true;
	}
	
	/**Takes data from all variables assigned and builds it into an nbt*/
	public NBTTagCompound build()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		Object value;
		
		// Iterated through assigned variables and gather their data
		for (String variable : variables)
		{
			value = ReflectionHelper.get(instance.getClass(), instance, variable, null);
			if (value == null) continue;
			if (variableFunction.containsKey(variable))
			{
				NBTTagCompound customNBT = variableFunction.get(variable).apply(value);
				if (customNBT != null)
					nbt.setTag(variable, customNBT);				
			}
			else if (value instanceof Byte)
				nbt.setByte(variable, (byte)value);
			else if (value instanceof Integer)
				nbt.setInteger(variable, (int)value);
			else if (value instanceof Short)
				nbt.setShort(variable, (short)value);
			else if (value instanceof Long)
				nbt.setLong(variable, (long)value);
			else if (value instanceof Float)
				nbt.setFloat(variable, (float)value);
			else if (value instanceof Double)
				nbt.setDouble(variable, (double)value);
			else if (value instanceof Boolean)
				nbt.setBoolean(variable, (boolean)value);
			else if (value instanceof String)
				nbt.setString(variable, (String)value);
			else if (value instanceof UUID)
				nbt.setUniqueId(variable, (UUID) value);
			else if (value instanceof NBTBase)
				nbt.setTag(variable, (NBTBase) value);
		}
		return nbt;
	}
}
