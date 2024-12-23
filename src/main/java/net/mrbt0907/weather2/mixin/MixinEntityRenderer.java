package net.mrbt0907.weather2.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import CoroUtil.config.ConfigCoroUtil;
import extendedrenderer.EventHandler;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.mrbt0907.weather2.config.ConfigParticle;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer
{
	@Inject(method = "renderRainSnow(F)V", at = @At("HEAD"), cancellable=true)
	private void renderRain(float partialTicks, CallbackInfo callback)
	{
		/**
		 * why render here? because renderRainSnow provides better context, solves issues:
		 * - translucent blocks rendered after
		 * -- shaders are color adjusted when rendering on other side of
		 * --- water
		 * --- stained glass, etc
		 */
		if (ConfigCoroUtil.useEntityRenderHookForShaders)
		{
			GlStateManager.pushMatrix();
            GlStateManager.disableCull();
            GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.alphaFunc(516, 0.1F);
			EventHandler.hookRenderShaders(partialTicks);
			GlStateManager.popMatrix();
		}
		if (!ConfigParticle.enable_vanilla_rain)
			callback.cancel(); //note, the overcast effect change will effect vanilla non particle rain distance too, particle rain for life!
	}
	
	@Inject(method = "addRainParticles()V", at = @At("HEAD"), cancellable=true)
	private void renderSplash(CallbackInfo callback)
	{
		if (!ConfigParticle.enable_vanilla_rain)
			callback.cancel();
	}
}
