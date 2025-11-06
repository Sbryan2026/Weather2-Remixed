package net.mrbt0907.weather2.mixin;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import CoroUtil.config.ConfigCoroUtil;
import extendedrenderer.EventHandler;
import net.mrbt0907.weather2.config.ConfigClient;
import net.mrbt0907.weather2.config.ConfigMisc;

/** Mixins cannot detect other mods in time when importing their classes, leading to ClassNotFoundExceptions. To combat this, we move any imports from other mods into another class like this which gets loaded with Forge */
public class MixinWorldReciever
{
	public static void renderRain(float partialTicks, CallbackInfo callback)
	{
		if (ConfigMisc.proxy_render_override)
		{
			if (ConfigCoroUtil.useEntityRenderHookForShaders)
				EventHandler.hookRenderShaders(partialTicks);
			if (!ConfigClient.enable_vanilla_rain)
				callback.cancel();
		}
	}
}
