package net.mrbt0907.weather2.mixin.injection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.mrbt0907.weather2.api.WeatherAPI;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.weather.WeatherManager;

@Mixin(World.class)
public class MixinWorld
{
	/** Injecting into isRaining allows other objects in Minecraft to detect whether it is raining all over or if any storm is raining */
	@Inject(method = "isRaining()Z", at = @At("RETURN"), cancellable=true)
	private void isRaining(CallbackInfoReturnable<Boolean> callback)
	{
		if (ConfigMisc.overcast_mode ? !callback.getReturnValueZ() : true)
		{
			WeatherManager manager = WeatherAPI.getManager((World)(Object) this);
			callback.setReturnValue(manager != null && manager.hasDownfall());
		}
	}
	
	/** Injecting into isRainingAt allows other objects in Minecraft to detect whether it can rain where they are and if any storms above the position is raining */
	@Inject(method = "isRainingAt(Lnet/minecraft/util/math/BlockPos;)Z", at = @At("RETURN"), cancellable=true)
	private void isRainingAt(BlockPos position, CallbackInfoReturnable<Boolean> callback)
	{
		World world = (World)(Object) this;
		if (ConfigMisc.overcast_mode ? !callback.getReturnValueZ() : true)
		{
			WeatherManager manager = WeatherAPI.getManager(world);
			callback.setReturnValue(manager != null && manager.hasDownfall(position));
		}
	}
}