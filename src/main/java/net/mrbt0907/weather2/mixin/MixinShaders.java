package net.mrbt0907.weather2.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.mrbt0907.weather2.config.ConfigParticle;

@Pseudo
@Mixin(targets="net.optifine.shaders.Shaders")
public class MixinShaders
{
	@ModifyVariable(method="drawHorizon()V", at=@At("STORE"), index=1, require=-1)
	private static float onClipDistance(float x)
	{
		return ConfigParticle.enable_extended_render_distance ? (float) ConfigParticle.extended_render_distance : x;
	}
}