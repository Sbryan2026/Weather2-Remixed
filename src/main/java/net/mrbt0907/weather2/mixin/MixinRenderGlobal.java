package net.mrbt0907.weather2.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.mrbt0907.weather2.config.ConfigParticle;

@Pseudo
@Mixin(targets="net.minecraft.client.renderer.RenderGlobal")
public class MixinRenderGlobal
{
	@Shadow
    private int renderDistance;
	@Shadow
    private int renderDistanceSq;
	
	@Inject(method = "renderSky(FI)V", at = @At("HEAD"), require=1, cancellable=true)
	public void renderSky(float partialTicks, int pass, CallbackInfo callback)
	{
		callback.cancel();
	}
	
	@Inject(method = "renderSky(Lnet/minecraft/client/renderer/BufferBuilder;FZ)V", at = @At("HEAD"), require=1, cancellable=true)
	private void renderSky(BufferBuilder worldRendererIn, float posY, boolean reverseX, CallbackInfo callback)
	{
        worldRendererIn.begin(7, DefaultVertexFormats.POSITION);
		callback.cancel();
		renderDistance = (int) (ConfigParticle.enable_extended_render_distance ? Math.min(ConfigParticle.extended_render_distance, Integer.MAX_VALUE - 1) + 1 : renderDistance);
		renderDistanceSq = renderDistance * renderDistance;
	}
}
