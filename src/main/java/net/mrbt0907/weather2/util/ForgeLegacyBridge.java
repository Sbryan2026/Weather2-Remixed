package net.mrbt0907.weather2.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * Temporary bridge used while porting from Forge 1.12 to 1.20.1.
 *
 * It centralizes legacy FML APIs so callsites can move off direct imports first,
 * then be rewritten to fully modern APIs in later commits.
 */
public class ForgeLegacyBridge {

    private ForgeLegacyBridge() {}

    public static MinecraftServer getCurrentServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    public static boolean canSendServerCommands(MinecraftServer server, com.mojang.authlib.GameProfile profile) {
        if (server == null) return false;
        return server.isSinglePlayer() || server.getPlayerList().canSendCommands(profile);
    }

    @SuppressWarnings("unchecked")
    public static List<Object> fetchRuntimeMessages(String modId) {
        try {
            Class<?> clazz = Class.forName("net.minecraftforge.fml.common.event.FMLInterModComms");
            Method method = clazz.getMethod("fetchRuntimeMessages", String.class);
            Object result = method.invoke(null, modId);
            if (result instanceof List<?>) {
                return (List<Object>) result;
            }
        } catch (Throwable ignored) {
            // No-op on modern Forge where old runtime IMC API no longer exists.
        }
        return Collections.emptyList();
    }

    public static void sendRuntimeMessage(Object sourceMod, String sourceModId, String key, NBTTagCompound nbt) {
        try {
            Class<?> clazz = Class.forName("net.minecraftforge.fml.common.event.FMLInterModComms");
            Method method = clazz.getMethod("sendRuntimeMessage", Object.class, String.class, String.class, NBTTagCompound.class);
            method.invoke(null, sourceMod, sourceModId, key, nbt);
        } catch (Throwable ignored) {
            // No-op on modern Forge where old runtime IMC API no longer exists.
        }
    }
}
