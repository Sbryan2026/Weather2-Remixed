package net.mrbt0907.weather2remastered.event;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import net.mrbt0907.weather2remastered.Weather2Remastered;
import net.mrbt0907.weather2remastered.client.render.AbstractWeatherRenderer;

@Cancelable
public class EventRegisterParticleRenderer extends Event {

    private final Map<ResourceLocation, Class<?>> registry = new LinkedHashMap<>();

    /** Event used to register custom particle spawning renderers into the game. */
    public EventRegisterParticleRenderer() {}

    /** Gets a copy of the current registry for particle renderers. */
    public Map<ResourceLocation, Class<?>> getRegistry() {
        return new LinkedHashMap<>(registry);
    }

    /** Registers a particle renderer to the game. */
    public void register(ResourceLocation id, Class<?> particleRenderer) {
        if (id == null) {
            Weather2Remastered.debug("Failed to register a particle renderer as the id was null. Skipping...");
        } else if (id.toString().equals(Weather2Remastered.MODID + ":normal") || registry.containsKey(id)) {
        	Weather2Remastered.debug("Failed to register a particle renderer as the id is already taken. Skipping...");
        } else if (particleRenderer == null) {
        	Weather2Remastered.debug("Failed to register particle renderer " + id + " as the renderer was null. Skipping...");
        } else if (!AbstractWeatherRenderer.class.isAssignableFrom(particleRenderer)) {
        	Weather2Remastered.debug("Failed to register particle renderer " + id + " as the renderer does not extend from AbstractWeatherRenderer. Skipping...");
        } else {
            registry.put(id, particleRenderer);
            Weather2Remastered.debug("Registered particle renderer " + id);
        }
    }
}
