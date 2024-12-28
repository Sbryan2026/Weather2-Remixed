package net.mrbt0907.weather2.mixin.injection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.EntityRenderer;
import net.mrbt0907.weather2.client.NewSceneEnhancer;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.config.ConfigClient;
import net.mrbt0907.weather2.mixin.MixinWorldReciever;

@Pseudo
@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer
{
	/** Overriding this pushes the skybox further back when Extended Render Distance is enabled */
	@Shadow
    private float farPlaneDistance;
	/** Overriding this pushes the skybox further back when Extended Render Distance is enabled with Optifine installed */
    private float clipDistance;
    
    /** Injecting into setupCameraTransform gives us the perfect spot to override farPlaneDistance and clipDistance */
	@Inject(method = "setupCameraTransform(FI)V", at = @At("RETURN"), cancellable=true)
	private void setupCameraTransform(float partialTicks, int pass, CallbackInfo callback)
	{
		EntityRenderer renderer = (EntityRenderer)(Object) this;
		NewSceneEnhancer scene = NewSceneEnhancer.instance();

		farPlaneDistance = ConfigClient.enable_extended_render_distance ? (float) ConfigClient.extended_render_distance : renderer.mc.gameSettings.renderDistanceChunks * 16;
		scene.renderDistance = farPlaneDistance;
		clipDistance = farPlaneDistance * 2.0f;
        if (clipDistance < 173.0f)
            clipDistance = 173.0f;
	}
	
	/** Injecting into renderRainSnow removes the need to create a different EntityRenderer as we can directly hook into the method.<br><br>
	 * When Proxy Render Override is enabled, it solves:<br>
	 * - Translucent blocks not rendering correctly<br>
	 * - Shaders not color adjusting to Water, Stained Glass, etc.
	 */
	@Inject(method = "renderRainSnow(F)V", at = @At("HEAD"), cancellable=true)
	private void renderRain(float partialTicks, CallbackInfo callback)
	{
		MixinWorldReciever.renderRain(partialTicks, callback);
	}
	
	/** Injecting into addRainParticles allows us to disable rain splashes and sounds in favor of our own effects */
	@Inject(method = "addRainParticles()V", at = @At("HEAD"), cancellable=true)
	private void renderSplash(CallbackInfo callback)
	{
		if (ConfigMisc.proxy_render_override && !ConfigClient.enable_vanilla_rain)
			callback.cancel();
	}

}