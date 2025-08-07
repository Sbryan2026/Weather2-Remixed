package net.mrbt0907.weather2remastered.network;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

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
            ServerPlayerEntity player = ctx.get().getSender(); // only non-null on server side
            if (player != null) {
                // Do something with the tag or player
                CompoundNBT tag = msg.tag;
                System.out.println("Received NBT from client: " + tag);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
