package net.mrbt0907.weather2.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.mrbt0907.weather2.config.ConfigParticle;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer
{
	@Inject(method = "setupFog(IF)V", at = @At("HEAD"), cancellable=true)
	private void setupFog(int startCoords, float partialTicks, CallbackInfo callback)
	{
		callback.cancel();
		EntityRenderer renderer = (EntityRenderer) (Object) this;
        final Entity entity = renderer.mc.getRenderViewEntity();
		ForgeHooksClient.getFogDensity(renderer, entity, ActiveRenderInfo.getBlockStateAtEntityViewpoint((World)renderer.mc.world, entity, partialTicks), partialTicks, 0);
	}
	
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
		if (CoroUtil.config.ConfigCoroUtil.useEntityRenderHookForShaders)
			extendedrenderer.EventHandler.hookRenderShaders(partialTicks);
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
