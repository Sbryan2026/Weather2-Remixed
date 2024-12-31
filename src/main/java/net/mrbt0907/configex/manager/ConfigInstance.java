package net.mrbt0907.configex.manager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.mrbt0907.configex.api.ConfigAnnotations.*;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.configex.config.ConfigMaster;
import net.mrbt0907.weather2remastered.Weather2Remastered;

public class ConfigInstance
{
	public final IConfigEX instance;
	public final String name;
	public final String registryName;
	public final String description;
	public final String saveLocation;
	public final ForgeConfigSpec configuration;
	private static final List<ConfigValue<?>> values = new ArrayList<>();
	
	public ConfigInstance(IConfigEX instance)
	{
		this.instance = instance;
		name = instance.getName();
		registryName = instance.getName();
		description = instance.getDescription();
		saveLocation = instance.getSaveLocation();
		configuration = build();
	}
	
	public ForgeConfigSpec build()
	{
		ForgeConfigSpec configuration;
		Builder builder = new Builder();
		boolean client = false, common = false, server = false;
		Class<? extends IConfigEX> clazz = instance.getClass();
		Field[] fields = clazz.getDeclaredFields();
		
		if (description != null)
			builder.comment(description);
		builder.push(name != null ? name : "main");
		
		for (Field field : fields)
		{
			if (field.isAnnotationPresent(Ignore.class)) continue;
			boolean clientSided = field.isAnnotationPresent(ClientSide.class);
			boolean serverSided = field.isAnnotationPresent(ServerSide.class);
			client = client || clientSided;
			common = common || !clientSided && !serverSided;
			server = server || serverSided;
			Name name = field.getDeclaredAnnotation(Name.class);
			Comment comment = field.getDeclaredAnnotation(Comment.class);
			boolean hidden = field.isAnnotationPresent(Hidden.class);
			boolean requiresWorldReload = field.isAnnotationPresent(RequiresWorldReload.class);
			//boolean requiresRestart = field.isAnnotationPresent(RequiresRestart.class);
			Object defaultValue = null;
			try
			{
				defaultValue = field.get(instance);
			}
			catch (Exception e)
			{
				Weather2Remastered.fatal(e);
			}
			byte type = (byte) (defaultValue instanceof Integer ? 1 : defaultValue instanceof Short ? 2 : defaultValue instanceof Long ? 3 : defaultValue instanceof Float ? 4 : defaultValue instanceof Double ? 5 : defaultValue instanceof String ? 6 : defaultValue instanceof Boolean ? 7 : 0);
			
			if (comment != null)
				builder.comment(comment.value());
			if (requiresWorldReload)
				builder.worldRestart();
			
			switch (type)
			{
				case 1: values.add(builder.define(name == null ? field.getName() : name.value(), (int) defaultValue)); break;
				case 2: values.add(builder.define(name == null ? field.getName() : name.value(), (short) defaultValue)); break;
				case 3: values.add(builder.define(name == null ? field.getName() : name.value(), (long) defaultValue)); break;
				case 4: values.add(builder.define(name == null ? field.getName() : name.value(), (float) defaultValue)); break;
				case 5: values.add(builder.define(name == null ? field.getName() : name.value(), (double) defaultValue)); break;
				case 6: values.add(builder.define(name == null ? field.getName() : name.value(), (String) defaultValue)); break;
				case 7: values.add(builder.define(name == null ? field.getName() : name.value(), (boolean) defaultValue)); break;
			}
			
		}
		builder.pop();
		configuration = builder.build();
		
		ModLoadingContext context = ModLoadingContext.get();
		if (client)
			context.registerConfig(Type.CLIENT, ConfigMaster.SPEC, saveLocation + "-client.toml");
		if (common)
			context.registerConfig(Type.COMMON, ConfigMaster.SPEC, saveLocation + "-common.toml");
		if (server)
			context.registerConfig(Type.SERVER, ConfigMaster.SPEC, saveLocation + "-server.toml");
		return configuration;
	}
}