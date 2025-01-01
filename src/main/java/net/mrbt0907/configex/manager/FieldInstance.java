package net.mrbt0907.configex.manager;

import java.lang.reflect.Field;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.mrbt0907.configex.ConfigManager;
import net.mrbt0907.configex.ConfigModEX;
import net.mrbt0907.configex.api.ConfigAnnotations.ClientSide;
import net.mrbt0907.configex.api.ConfigAnnotations.Comment;
import net.mrbt0907.configex.api.ConfigAnnotations.DoubleRange;
import net.mrbt0907.configex.api.ConfigAnnotations.FloatRange;
import net.mrbt0907.configex.api.ConfigAnnotations.Hidden;
import net.mrbt0907.configex.api.ConfigAnnotations.IntegerRange;
import net.mrbt0907.configex.api.ConfigAnnotations.LongRange;
import net.mrbt0907.configex.api.ConfigAnnotations.Name;
import net.mrbt0907.configex.api.ConfigAnnotations.Permission;
import net.mrbt0907.configex.api.ConfigAnnotations.RequiresWorldReload;
import net.mrbt0907.configex.api.ConfigAnnotations.ServerSide;
import net.mrbt0907.configex.api.ConfigAnnotations.ShortRange;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.weather2remastered.Weather2Remastered;

public class FieldInstance
{
	public final ForgeConfigSpec configuration;
	public final IConfigEX instance;
	public final Field field;
	public final String name;
	public final String registryName;
	public final String[] comment;
	public final int permission;
	public final boolean hidden;
	public final boolean requiresWorldReload;
	public final double min;
	public final double max;
	public final boolean showMin;
	public final boolean showMax;
	
	public final byte type;
	public final Object defaultValue;
	public final ConfigValue<Object> configValue;
	public final Type configType;
	public final boolean ignoreServer;
	public boolean isDirty;
	protected Object clientValue;
	protected Object serverValue;
	
	public FieldInstance(Builder builder, ForgeConfigSpec configuration, IConfigEX instance, Field field)
	{
		this.configuration = configuration;
		this.instance = instance;
		this.field = field;
		configType = field.isAnnotationPresent(ClientSide.class) ? Type.CLIENT : field.isAnnotationPresent(ServerSide.class) ? Type.SERVER : Type.COMMON;
		ignoreServer = configType.equals(Type.CLIENT);
		
		Object defaultValue = null;
		try {defaultValue = field.get(instance);} catch (Exception e) {Weather2Remastered.fatal(e);}
		type = (byte) (defaultValue instanceof Integer ? 1 : defaultValue instanceof Short ? 2 : defaultValue instanceof Long ? 3 : defaultValue instanceof Float ? 4 : defaultValue instanceof Double ? 5 : defaultValue instanceof String ? 6 : defaultValue instanceof Boolean ? 7 : 0);
		this.defaultValue = defaultValue;
		
		Name name = field.getDeclaredAnnotation(Name.class);
		this.name = name == null || name.value().length() == 0 ? field.getName() : name.value();
		registryName = ConfigManager.formatRegistryName(field.getName());
		
		Comment comment = field.getDeclaredAnnotation(Comment.class);
		String[] declaredComment = comment == null ? null : comment.value();
		this.comment = new String[declaredComment.length + 1];
		for (int i = 0; i < declaredComment.length; i++)
			this.comment[i] = declaredComment[i];
		this.comment[declaredComment.length] = "Default Value: " + defaultValue;
		
		Permission permission = field.getDeclaredAnnotation(Permission.class);
		this.permission = permission == null ? 3 : permission.value();
		
		
		hidden = field.isAnnotationPresent(Hidden.class);
		requiresWorldReload = field.isAnnotationPresent(RequiresWorldReload.class);
		
		switch(type)
		{
			case 1:
				IntegerRange rangeI = field.getAnnotation(IntegerRange.class);
				min = rangeI != null ? rangeI.min() : Integer.MIN_VALUE;
				max = rangeI != null ? rangeI.max() : Integer.MAX_VALUE;
				showMin = min != Integer.MIN_VALUE;
				showMax = max != Integer.MAX_VALUE;
				break;
			case 2:
				ShortRange rangeS = field.getAnnotation(ShortRange.class);
				min = rangeS != null ? rangeS.min() : Short.MIN_VALUE;
				max = rangeS != null ? rangeS.max() : Short.MAX_VALUE;
				showMin = min != Short.MIN_VALUE;
				showMax = max != Short.MAX_VALUE;
				break;
			case 3:
				LongRange rangeL = field.getAnnotation(LongRange.class);
				min = rangeL != null ? rangeL.min() : Long.MIN_VALUE;
				max = rangeL != null ? rangeL.max() : Long.MAX_VALUE;
				showMin = min != Long.MIN_VALUE;
				showMax = max != Long.MAX_VALUE;
				break;
			case 4:
				FloatRange rangeF = field.getAnnotation(FloatRange.class);
				min = rangeF != null ? rangeF.min() : -Float.MAX_VALUE;
				max = rangeF != null ? rangeF.max() : Float.MAX_VALUE;
				showMin = min != -Float.MAX_VALUE;
				showMax = max != Float.MAX_VALUE;
				break;
			case 5:
				DoubleRange rangeD = field.getAnnotation(DoubleRange.class);
				min = rangeD != null ? rangeD.min() : -Double.MAX_VALUE;
				max = rangeD != null ? rangeD.max() : Double.MAX_VALUE;
				showMin = min != -Double.MAX_VALUE;
				showMax = max != Double.MAX_VALUE;
				break;
			default:
				min = 0.0D;
				max = 0.0D;
				showMin = false;
				showMax = false;
		}
		
		builder.comment(this.comment);
		if (requiresWorldReload)
			builder.worldRestart();
		switch (type)
		{
			case 0: 
				configValue = null; 
				Weather2Remastered.fatal("Field " + field.getName() + " is using an unsupported type");
				break;
			default:
				configValue = builder.define(this.name, defaultValue);
		}
		
		set(defaultValue, !ConfigManager.IS_REMOTE);
	}
	
	public void readNBT(CompoundNBT nbt)
	{
		if (ConfigManager.IS_REMOTE ? !ignoreServer : true && nbt.contains(registryName))
			switch(type)
			{
				case 1: set(nbt.getInt(registryName), ConfigManager.IS_REMOTE); break;
				case 2: set(nbt.getShort(registryName), ConfigManager.IS_REMOTE); break;
				case 3: set(nbt.getLong(registryName), ConfigManager.IS_REMOTE); break;
				case 4: set(nbt.getFloat(registryName), ConfigManager.IS_REMOTE); break;
				case 5: set(nbt.getDouble(registryName), ConfigManager.IS_REMOTE); break;
				case 6: set(nbt.getString(registryName), ConfigManager.IS_REMOTE); break;
				case 7: set(nbt.getBoolean(registryName), ConfigManager.IS_REMOTE); break;
			}
	}
	
	public void writeNBT(CompoundNBT nbt)
	{
		if (ConfigManager.IS_REMOTE ? !ignoreServer : true)
			switch(type)
			{
				case 1: nbt.putInt(registryName, (int) get()); break;
				case 2: nbt.putShort(registryName, (short) get()); break;
				case 3: nbt.putLong(registryName, (long) get()); break;
				case 4: nbt.putFloat(registryName, (float) get()); break;
				case 5: nbt.putDouble(registryName, (double) get()); break;
				case 6: nbt.putString(registryName, (String) get()); break;
				case 7: nbt.putBoolean(registryName, (boolean) get()); break;
			}
	}
	
	public Object getSavedValue()
	{
		return configValue.get();
	}
	
	public Object getActualValue()
	{
		try
		{
			return field.get(instance);
		}
		catch (Exception e)
		{
			ConfigModEX.fatal(e);
			return null;
		}
	}
	
	public Object get()
	{
		return serverValue == null ? clientValue : serverValue;
	}
	
	public FieldInstance set(Object value, boolean setServerValue)
	{
		Object oldValue;
		if (setServerValue)
		{
			oldValue = serverValue;
			serverValue = value;
		}
		else
		{
			oldValue = clientValue;
			clientValue = value;
		}
		
		try
		{
			field.set(instance, serverValue == null ? clientValue : serverValue);
		}
		catch (Exception e)
		{
			Weather2Remastered.error(e);
			if (setServerValue)
			{
				serverValue = oldValue;
			}
			else
			{
				clientValue = oldValue;
			}
		}
		return this;
	}
	
	public FieldInstance markDirty()
	{
		isDirty = true;
		return this;
	}
	
	public FieldInstance reset()
	{
		if (ConfigManager.IS_REMOTE)
			set(null, true);
		return this;
	}
	
	public void save()
	{
		if (ConfigManager.IS_REMOTE)
			configValue.set(clientValue);
		else
			configValue.set(serverValue);
		configValue.save();
	}
}