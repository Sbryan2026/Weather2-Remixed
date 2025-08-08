package net.mrbt0907.weather2remastered.network;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.mrbt0907.weather2remastered.event.ServerTickHandler;
import net.mrbt0907.weather2remastered.gui.EZConfigParser;

public class PacketNBT {
    private final CompoundNBT tag;

    public PacketNBT(CompoundNBT tag) {
        this.tag = tag;
    }

    public static void encode(PacketNBT msg, PacketBuffer buf) {
        buf.writeNbt(msg.tag);
    }

    public static PacketNBT decode(PacketBuffer buf) {
        CompoundNBT tag = buf.readNbt();
        return new PacketNBT(tag);
    }

    public static void handle(PacketNBT msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // This runs on the server thread
            if (ctx.get().getSender() != null) {
                // Do something with the tag or player
                CompoundNBT tag = msg.tag;
                System.out.println("Received NBT from client: " + tag);
                System.out.println("ENQUEUING WORK AND PROCESSING COMMAND WITH INT: " + msg.tag.getInt("command"));
    				switch(msg.tag.getInt("command"))
    				{
    					case 8:
    						CompoundNBT sendNBT = EZConfigParser.nbtServerData;
    						sendNBT.putInt("command", 9);
    						sendNBT.putInt("server", 1);
    						sendNBT.putBoolean("op", net.minecraft.client.Minecraft.getInstance().hasSingleplayerServer() || ServerLifecycleHooks.getCurrentServer().getPlayerList().isOp(ctx.get().getSender().getGameProfile()));
    						PacketEZGUI.syncResponse(sendNBT);
    						break;
    					case 11:
    						ServerTickHandler.playerClientRequestsFullSync(ctx.get().getSender());
    						break;
    					case 10:
    						if (net.minecraft.client.Minecraft.getInstance().hasSingleplayerServer() || ServerLifecycleHooks.getCurrentServer().getPlayerList().isOp(ctx.get().getSender().getGameProfile()))
    							EZConfigParser.nbtReceiveServer(msg.tag);
    						break;
    				}
    			
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
