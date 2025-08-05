package net.mrbt0907.weather2remastered.registry;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.mrbt0907.weather2remastered.Weather2Remastered;
/***To play sounds from the SoundRegistry please use SoundRegistry.SOUND_NAME.get() to return the SoundEvent.***/
public class SoundRegistry
{

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Weather2Remastered.MODID);

    public static final RegistryObject<SoundEvent> SIREN = register("block.siren");
    public static final RegistryObject<SoundEvent> SIREN_DARUDE = register("block.siren.darude");
    public static final RegistryObject<SoundEvent> SIREN_ADVANCED = register("block.siren.advanced");

    public static final RegistryObject<SoundEvent> LEAVES = register("ambient.leaves");
    public static final RegistryObject<SoundEvent> WATERFALL = register("ambient.waterfall");
    public static final RegistryObject<SoundEvent> WIND_FAST = register("ambient.wind.fast");
    public static final RegistryObject<SoundEvent> WIND = register("ambient.wind");
    public static final RegistryObject<SoundEvent> RAIN_LIGHT = register("ambient.rain.light");
    public static final RegistryObject<SoundEvent> RAIN_HEAVY = register("ambient.rain.heavy");

    public static final RegistryObject<SoundEvent> DEBRIS = register("weather.debris");
    public static final RegistryObject<SoundEvent> STORM = register("weather.storm");
    public static final RegistryObject<SoundEvent> SANDSTORM_FAST = register("weather.sandstorm.fast");
    public static final RegistryObject<SoundEvent> SANDSTORM = register("weather.sandstorm");
    public static final RegistryObject<SoundEvent> SANDSTORM_SLOW = register("weather.sandstorm.slow");

    public static final RegistryObject<SoundEvent> THUNDER_NEAR = register("entity.lightning.thunder.near");
    public static final RegistryObject<SoundEvent> THUNDER_FAR = register("entity.lightning.thunder.far");

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> new SoundEvent(new ResourceLocation(Weather2Remastered.MODID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}

