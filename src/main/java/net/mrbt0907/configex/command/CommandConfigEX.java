package net.mrbt0907.configex.command;

import java.util.List;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.mrbt0907.configex.ConfigManager;
import net.mrbt0907.configex.ConfigModEX;
import net.mrbt0907.configex.manager.ConfigInstance;
import net.mrbt0907.configex.manager.FieldInstance;

public class CommandConfigEX
{
	@SubscribeEvent
    public static void onRegisterCommandEvent(RegisterCommandsEvent event)
	{
		ConfigModEX.info("Registering configEX command");
		CommandConfigEX command = new CommandConfigEX();
		List<ResourceLocation> ids = ConfigManager.ids();
		LiteralArgumentBuilder<CommandSource> cmdCfgEX = Commands.literal("configex")
		.requires(command::hasPermission)
		.then(Commands.literal("default")
			.then(Commands.argument("variable", ResourceLocationArgument.id())
				.suggests((ctx, builder) -> ISuggestionProvider.suggestResource(ids, builder))
					.executes(context -> command.executeDefault(context.getSource(), ResourceLocationArgument.getId(context, "variable")))
			)
			.executes(context -> command.onFailiure(context.getSource(), "configex", 1))
		)
		.then(Commands.literal("get")
			.then(Commands.argument("variable", ResourceLocationArgument.id())
				.suggests((ctx, builder) -> ISuggestionProvider.suggestResource(ids, builder))
					.executes(context -> command.executeGet(context.getSource(), ResourceLocationArgument.getId(context, "variable")))
			)
			.executes(context -> command.onFailiure(context.getSource(), "configex", 1))
		)
		.then(Commands.literal("set")
			.then(Commands.argument("variable", ResourceLocationArgument.id())
				.suggests((ctx, builder) -> ISuggestionProvider.suggestResource(ids, builder))
				.then(Commands.argument("value", StringArgumentType.string())
					.executes(context -> command.executeSet(context.getSource(), ResourceLocationArgument.getId(context, "variable"), StringArgumentType.escapeIfRequired(StringArgumentType.getString(context, "value"))))
				)
			)
			.executes(context -> command.onFailiure(context.getSource(), "configex", 1))
		)
		.executes(context -> command.onFailiure(context.getSource(), "configex", 0));
		event.getDispatcher().register(cmdCfgEX);
	}

	public boolean hasPermission(CommandSource source)
	{
		return source.hasPermission(2);
	}
	
	public int onFailiure(CommandSource source, String command, int index)
	{	
		switch(index)
		{
			case 0:
				source.sendSuccess(new StringTextComponent("/" + command + " <default/get/set>"), false);
				break;
			case 1:
				source.sendSuccess(new StringTextComponent("/" + command + " <default/get/set> <variable_id>"), false);
				break;
		}
		return 0;
	}
	
	public int executeDefault(CommandSource source, ResourceLocation registryName)
	{
		ConfigInstance config = ConfigManager.get(registryName.getNamespace());
		if (config == null)
		{
			source.sendFailure(new StringTextComponent("Config " + registryName.getNamespace() + " does not exist"));
			return 0;
		}
		FieldInstance field = config.get(registryName.getPath());
		if (field == null)
		{
			source.sendFailure(new StringTextComponent("Config variable " + registryName.toString() + " does not exist"));
			return 0;
		}
		
		field.set(field.defaultValue, !ConfigManager.IS_REMOTE).markDirty();
		ConfigManager.sync();
		field.save();
		source.sendSuccess(new StringTextComponent("Changed variable " + registryName.toString() + " to \"" + field.defaultValue + "\""), true);
		return 1;
	}
	
	public int executeGet(CommandSource source, ResourceLocation registryName)
	{
		ConfigInstance config = ConfigManager.get(registryName.getNamespace());
		if (config == null)
		{
			source.sendFailure(new StringTextComponent("Config " + registryName.getNamespace() + " does not exist"));
			return 0;
		}
		FieldInstance field = config.get(registryName.getPath());
		if (field == null)
		{
			source.sendFailure(new StringTextComponent("Config variable " + registryName.toString() + " does not exist"));
			return 0;
		}
		source.sendSuccess(new StringTextComponent("Variable " + registryName.toString() + " = \"" + field.getActualValue() + "\""), true);
		return 1;
	}
	
	public int executeSet(CommandSource source, ResourceLocation registryName, String value)
	{
		ConfigInstance config = ConfigManager.get(registryName.getNamespace());
		if (config == null)
		{
			source.sendFailure(new StringTextComponent("Config " + registryName.getNamespace() + " does not exist"));
			return 0;
		}
		FieldInstance field = config.get(registryName.getPath());
		if (field == null)
		{
			source.sendFailure(new StringTextComponent("Config variable " + registryName.toString() + " does not exist"));
			return 0;
		}
		
		switch (field.type)
		{
			case 1:
				try {field.set(Integer.parseInt(value), !ConfigManager.IS_REMOTE);}
				catch (Exception e) {source.sendFailure(new StringTextComponent("Value \"" + value + "\" cannot be assigned to config variable " + registryName.toString() + " as it is not a valid integer")); return 0;}
				break;
			case 2:
				try {field.set(Short.parseShort(value), !ConfigManager.IS_REMOTE);}
				catch (Exception e) {source.sendFailure(new StringTextComponent("Value \"" + value + "\" cannot be assigned to config variable " + registryName.toString() + " as it is not a valid short")); return 0;}
				break;
			case 3:
				try {field.set(Long.parseLong(value), !ConfigManager.IS_REMOTE);}
				catch (Exception e) {source.sendFailure(new StringTextComponent("Value \"" + value + "\" cannot be assigned to config variable " + registryName.toString() + " as it is not a valid long")); return 0;}
				break;
			case 4:
				try {field.set(Float.parseFloat(value), !ConfigManager.IS_REMOTE);}
				catch (Exception e) {source.sendFailure(new StringTextComponent("Value \"" + value + "\" cannot be assigned to config variable " + registryName.toString() + " as it is not a valid float")); return 0;}
				break;
			case 5:
				try {field.set(Double.parseDouble(value), !ConfigManager.IS_REMOTE);}
				catch (Exception e) {source.sendFailure(new StringTextComponent("Value \"" + value + "\" cannot be assigned to config variable " + registryName.toString() + " as it is not a valid double")); return 0;}
				break;
			case 6:
				try {field.set(String.valueOf(value), !ConfigManager.IS_REMOTE);}
				catch (Exception e) {source.sendFailure(new StringTextComponent("Value \"" + value + "\" cannot be assigned to config variable " + registryName.toString() + " as it is not a valid string")); return 0;}
				break;
			case 7:
				try {field.set(Boolean.parseBoolean(value), !ConfigManager.IS_REMOTE);}
				catch (Exception e) {source.sendFailure(new StringTextComponent("Value \"" + value + "\" cannot be assigned to config variable " + registryName.toString() + " as it is not a valid boolean")); return 0;}
				break;
		}
		
		field.set(Boolean.parseBoolean(value), !ConfigManager.IS_REMOTE).markDirty();
		ConfigManager.sync();
		field.save();
		source.sendSuccess(new StringTextComponent("Changed variable " + registryName.toString() + " to \"" + field.get() + "\""), true);
		return 1;
	}
}
