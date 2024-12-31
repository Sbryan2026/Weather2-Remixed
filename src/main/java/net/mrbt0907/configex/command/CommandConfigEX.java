package net.mrbt0907.configex.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.MessageArgument;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.mrbt0907.configex.ConfigModEX;
import net.mrbt0907.configex.network.NetworkHandler;

public class CommandConfigEX {
	@SubscribeEvent
    public static void onRegisterCommandEvent(RegisterCommandsEvent event) {
		ConfigModEX.info("Registering configEX command");
		CommandDispatcher<CommandSource> dispatcher = event.getDispatcher();
		LiteralArgumentBuilder<CommandSource> cmdCfgEX = Commands.literal("configex")
				.requires((commandSource) -> commandSource.hasPermission(2))
				.then(Commands.argument("message", MessageArgument.message())
				.executes(CommandConfigEX::confEX));

		dispatcher.register(cmdCfgEX);
	}

	static int confEX(CommandContext<CommandSource> cmdContext) throws CommandSyntaxException {
		ITextComponent messageVal = MessageArgument.getMessage(cmdContext, "message");
		NetworkHandler.sendClientPacket(0, new CompoundNBT(), (Object[])null);
		ConfigModEX.info(messageVal.toString());
		return 1;
	}
}
