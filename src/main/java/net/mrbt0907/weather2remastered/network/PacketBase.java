package net.mrbt0907.weather2remastered.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.PacketDistributor;
import net.mrbt0907.weather2remastered.Weather2Remastered;
import net.mrbt0907.weather2remastered.util.fartsy.FartsyUtil;

public class PacketBase {

    public static void send(int command, CompoundNBT nbt, Object... target) {
        if (nbt == null) {
            Weather2Remastered.error("Network command #" + command + " returned null nbt data");
            return;
        }
        
        if (command == 20) return; // skip command 20
        nbt.putInt("command", command);
       // System.out.println("Set nbt to " + nbt);
        if (FMLEnvironment.dist.isClient()) {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();

            // If connected now, send immediately
            if (mc.level != null && mc.getConnection() != null) {
                sendNow(nbt);
                return;
            }
            // Otherwise, wait until we are connected
            if (mc.level == null&& mc.getConnection() == null) System.out.println("Server not ready, scheduling packet send...");
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new Object() {
            @OnlyIn(Dist.CLIENT)
            @SubscribeEvent
            public void onClientTick(net.minecraftforge.event.TickEvent.ClientTickEvent event)
            {
            	if (event.phase == net.minecraftforge.event.TickEvent.Phase.END)
            	{
            		if (mc.getConnection() != null && mc.getCurrentServer() != null) {
                                sendNow(nbt);
                                // Unregister after sending
                                net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(this);
                            }
                        }
                    }
                });
            }
		else
		{
			//FartsyUtil.prettyPrintNBT(nbt);
			if (target.length == 0)
				Weather2Remastered.CHANNEL.send(PacketDistributor.ALL.noArg(), new PacketNBT(nbt));
			else if (target[0] instanceof String)
				Weather2Remastered.CHANNEL.send(PacketDistributor.DIMENSION.with(() -> dimensionKey(target[0])), new PacketNBT(nbt));
			else if (target[0] instanceof ServerPlayerEntity) {
				System.out.println("Sending to target " + target[0]);
				Weather2Remastered.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) target[0]), new PacketNBT(nbt));
			}
			else Weather2Remastered.error("Network packet #" + command + " returned an invalid target");
		}
	}

	private static RegistryKey<World> dimensionKey(Object object) {
		return RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation((String)object));
	}

    private static void sendNow(CompoundNBT nbt) {
       // System.out.println("Sending PacketNBT " + new PacketNBT(nbt));
        Weather2Remastered.CHANNEL.sendToServer(new PacketNBT(nbt));
    }
}
