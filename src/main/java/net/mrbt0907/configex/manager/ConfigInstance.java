package net.mrbt0907.configex.manager;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.mrbt0907.configex.ConfigManager;
import net.mrbt0907.configex.api.ConfigAnnotations.*;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.configex.api.IConfigEX.Phase;

public class ConfigInstance
{
	private final Map<String, FieldInstance> fields = new LinkedHashMap<String, FieldInstance>();
	public final IConfigEX instance;
	public final String name;
	public final String registryName;
	public final String description;
	public final String saveLocation;
	public final ForgeConfigSpec clientCFG;
	public final ForgeConfigSpec commonCFG;
	public final ForgeConfigSpec serverCFG;
	
	public ConfigInstance(IConfigEX instance)
	{
		this.instance = instance;
		name = instance.getName();
		registryName = ConfigManager.formatRegistryName(name);
		description = instance.getDescription();
		saveLocation = instance.getSaveLocation();
		clientCFG = build(Type.CLIENT);
		commonCFG = build(Type.COMMON);
		serverCFG = build(Type.SERVER);
	}
	
	public FieldInstance get(String registryName)
	{
		return fields.get(registryName);
	}
	
	public void ids(List<ResourceLocation> variables)
	{
		fields.forEach((registryName, field) -> variables.add(new ResourceLocation(this.registryName, registryName)));	
	}
	
	public void readNBT(CompoundNBT nbt, int permissionLevel)
	{
		if (nbt.contains(registryName))
		{
			int variables = 0;
			CompoundNBT nbtFields = nbt.getCompound(registryName);
			FieldInstance field;
			
			instance.onConfigChanged(Phase.START, variables);
			for (String key : nbtFields.getAllKeys())
			{
				if (fields.containsKey(key))
				{
					field = fields.get(key);
					if (field.permission > permissionLevel) continue;
					Object oldValue = field.get();
					field.readNBT(nbtFields);
					Object newValue = field.get(); 
					if (newValue != oldValue)
					{
						instance.onValueChanged(key, oldValue, newValue);
						variables++;
					}
				}
			}
			instance.onConfigChanged(Phase.END, variables);
		}
	}
	
	public void writeNBT(CompoundNBT nbt, boolean fullNBT)
	{
		CompoundNBT nbtFields = new CompoundNBT();
		for (FieldInstance field : fields.values())
		{
			if (fullNBT || field.isDirty)
			{
				if (!fullNBT)
					field.isDirty = false;
				field.writeNBT(nbtFields);
			}
		}
		nbt.put(registryName, nbtFields);
	}
	
	private ForgeConfigSpec build(Type side)
	{
		ForgeConfigSpec configuration = null;
		Builder builder = new Builder();
		Class<? extends IConfigEX> clazz = instance.getClass();
		Field[] fields = clazz.getDeclaredFields();
		boolean register = false;
		
		builder.push(name != null ? name : "main");
		for (Field field : fields)
		{
			if (field.isAnnotationPresent(Ignore.class)) continue;
			Type configType = field.isAnnotationPresent(ClientSide.class) ? Type.CLIENT : field.isAnnotationPresent(ServerSide.class) ? Type.SERVER : Type.COMMON;
			if (!configType.equals(side)) continue;
			register = true;
			FieldInstance instance = new FieldInstance(builder, configuration, this.instance, field);
			this.fields.put(instance.registryName, instance);
		}
		builder.pop();
		configuration = builder.build();
		
		if (register)
		{
			ModLoadingContext context = ModLoadingContext.get();
			switch (side)
			{
				case CLIENT: context.registerConfig(Type.CLIENT, configuration, saveLocation + "-client.toml"); break;
				case COMMON: context.registerConfig(Type.COMMON, configuration, saveLocation + "-common.toml"); break;
				case SERVER: context.registerConfig(Type.SERVER, configuration, saveLocation + "-server.toml"); break;
			}
		}
		return configuration;
	}
	
	public void load()
	{
		instance.onConfigChanged(Phase.START, 0);
		for (FieldInstance field : this.fields.values())
		{
			field.set(field.getSavedValue(), !ConfigManager.IS_REMOTE);
			instance.onValueChanged(field.registryName, field.defaultValue, field.get());
		}
		instance.onConfigChanged(Phase.END, this.fields.size());
	}
}