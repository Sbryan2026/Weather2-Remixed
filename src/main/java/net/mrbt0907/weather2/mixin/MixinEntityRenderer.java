package net.mrbt0907.weather2.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.EntityRenderer;
import net.mrbt0907.weather2.client.NewSceneEnhancer;
import net.mrbt0907.weather2.config.ConfigParticle;

@Pseudo
@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer
{
	@Shadow
    private float farPlaneDistance;
    private float clipDistance;
    
	@Inject(method = "setupCameraTransform(FI)V", at = @At("RETURN"), cancellable=true)
	private void transf(float partialTicks, int pass, CallbackInfo callback)
	{
		EntityRenderer renderer = (EntityRenderer)(Object) this;
		NewSceneEnhancer scene = NewSceneEnhancer.instance();

		farPlaneDistance = ConfigParticle.enable_extended_render_distance ? (float) ConfigParticle.extended_render_distance : renderer.mc.gameSettings.renderDistanceChunks * 16;
		scene.renderDistance = farPlaneDistance;
		clipDistance = farPlaneDistance * 2.0f;
        if (clipDistance < 173.0f)
            clipDistance = 173.0f;
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