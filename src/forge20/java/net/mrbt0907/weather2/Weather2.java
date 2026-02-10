package net.mrbt0907.weather2;

import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Weather2.MODID)
public class Weather2 {
    public static final String MODID = "weather2";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Weather2() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::onClientSetup);
        LOGGER.info("Weather2 Remixed initialized for Forge 1.20.1");
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        LOGGER.debug("Weather2 client setup complete on {}", Dist.CLIENT);
    }
}
