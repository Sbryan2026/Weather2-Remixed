package net.mrbt0907.weather2remastered.network;

import java.util.function.Supplier;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;
import net.mrbt0907.weather2remastered.Weather2Remastered;
import net.mrbt0907.weather2remastered.client.ClientTickHandler;
import net.mrbt0907.weather2remastered.client.NewSceneEnhancer;
import net.mrbt0907.weather2remastered.event.ServerTickHandler;
import net.mrbt0907.weather2remastered.gui.EZConfigParser;
import net.mrbt0907.weather2remastered.util.fartsy.FartsyUtil;

public class PacketNBT {
    private final CompoundNBT tag;

    public PacketNBT(CompoundNBT tag) { this.tag = tag; }

    public static void encode(PacketNBT msg, PacketBuffer buf) { buf.writeNbt(msg.tag); }

    public static PacketNBT decode(PacketBuffer buf) {
        CompoundNBT tag = buf.readNbt();
        return new PacketNBT(tag);
    }

    public static void handle(PacketNBT pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> { if (ctx.get().getDirection().getReceptionSide().isClient()) handleClient(pkt); else handleServer(pkt, ctx); });
        ctx.get().setPacketHandled(true);
    }

	@OnlyIn(Dist.CLIENT)
	private static void handleClient(PacketNBT pkt) {
	    CompoundNBT nbt = pkt.tag;
	    int command = nbt.getInt("command");
	    //Weather2Remastered.debug("PacketNBT.java:38 - sent to client, it contains: \n" + FartsyUtil.prettyPrintNBT(pkt.tag));
	    switch(command)
		{
			case 0: case 1: case 2: case 3:case 4: case 5: case 6: case 7:
				//System.out.println(FartsyUtil.prettyPrintNBT(nbt));
				ClientTickHandler.checkClientWeather();
				ClientTickHandler.weatherManager.nbtSyncFromServer(nbt);
				break;
			case 9:
				EZConfigParser.nbtReceiveClient(nbt);
				break;
			case 10:
				break;
			case 11: case 12: case 13: case 14:
				ClientTickHandler.checkClientWeather();
				ClientTickHandler.weatherManager.nbtSyncFromServer(nbt);
				
				break;
			case 17:
				NewSceneEnhancer.instance().reset();
				NewSceneEnhancer.instance().enable();
				break;
			case 18:
				break;
			case 20: {
				if (ClientTickHandler.weatherManager == null) break;
				ClientTickHandler.weatherManager.nbtSyncFromServer(nbt);
				break;
			}
			default: Weather2Remastered.error("Recieved an invalid network packet from the server");
		}
	}

    public static void handleServer(PacketNBT msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
        	net.minecraft.entity.player.ServerPlayerEntity player = ctx.get().getSender();
            if (player != null) {
            	//Weather2Remastered.debug("PacketNBT.java:69 - got from client, it contains: \n" + FartsyUtil.prettyPrintNBT(msg.tag));
    			switch(msg.tag.getInt("command"))
    			{
    				case 8:
    					CompoundNBT sendNBT = EZConfigParser.nbtServerData;
    					sendNBT.putInt("command", 9);
    					sendNBT.putInt("server", 1);
    					sendNBT.putBoolean("op", FartsyUtil.isPlayerOp(player.getGameProfile()));
    					PacketEZGUI.syncResponse(sendNBT);
    					break;
    				case 11:
    					ServerTickHandler.playerClientRequestsFullSync(player);
    					break;
    				case 10:
    					if (FartsyUtil.isPlayerOp(player.getGameProfile()));
    						EZConfigParser.nbtReceiveServer(msg.tag);
    					break;
    			}
            }
        });
        ctx.get().setPacketHandled(true);
    }
}