package net.mrbt0907.weather2remastered.registry;

import net.minecraft.block.AbstractBlock.Properties;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.mrbt0907.weather2remastered.Weather2Remastered;
import net.mrbt0907.weather2remastered.block.*;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class BlockRegistry
{
	//public static final Block wire = new BlockSensor();
	public static final BlockRadio radio = new BlockRadio();
	public static final Block wind_chimes = new BlockSiren();
	public static final Block air_horn_siren = new BlockSiren();
	public static final Block emergency_siren_alt = new BlockSiren();
	public static final Block emergency_siren_alt_manual = new BlockSiren();
	//public static final Block wind_sock = new BlockNewSensor(0.25F, 0.0D, false, false, true, true);
	//public static final Block thermometer = new BlockNewSensor(0.25F, 250.0D, true, false, false, false);
	//public static final Block hygrometer = new BlockNewSensor(0.25F, 250.0D, false, true, false, false);
	public static final Block weather_doppler_radar = new BlockRadar(1);
	public static final Block weather_pulse_radar = new BlockRadar(2);
	public static final Block weather_humidifier = new BlockWeatherConstructor();
	public static final Block weather_humidifier_2 = new BlockWeatherConstructor();
	public static final Block weather_conditioner = new BlockWeatherDeflector();
	public static final Block weather_conditioner_2 = new BlockWeatherDeflector();
	//public static final Block ground_sensor_unit = new BlockNewSensor(1.0F, 0.0D, true, true, true, true);
	public static final Block wind_vane = new BlockWindVane();
	public static final Block anemometer = new BlockAnemometer();
	
	public static final Block machineCase = new Block(Properties.of(Material.CLAY).harvestLevel(1).harvestTool(ToolType.PICKAXE).strength(0.6F, 10.0F));
	public static final Block stormSensor = new BlockSensor(0);
	public static final Block humiditySensor = new BlockSensor(1);
	public static final Block rainSensor = new BlockSensor(2);
	public static final Block temperatureSensor = new BlockSensor(3);
	public static final Block windSensor = new BlockSensor(4);
	public static final Block barometerSensor = new BlockSensor(5);
	
	public static final Block tornado_sensor = new BlockSensor(6);
	public static final Block emergency_siren_manual = new BlockSiren();
	public static final Block emergency_siren = new BlockSiren();
	public static final Block weather_radar = new BlockRadar(0);
	public static final Block weather_constructor = new BlockWeatherConstructor();
	public static final Block weather_deflector = new BlockWeatherDeflector();
	public static final Block sand_layer = new BlockSandLayer();
	private static boolean isInitialized = false;
	@SubscribeEvent
	public static void onBlockRegistry(RegistryEvent.Register<Block> event)
	{
		if(isInitialized) return;
		Weather2Remastered.debug("Registering blocks...");
		addBlock(event, "tornado_sensor", tornado_sensor);
		addBlock(event, "tornado_siren", emergency_siren);
		addBlock(event, "tornado_siren_manual", emergency_siren_manual);
		addBlock(event, "wind_vane", wind_vane);
		addBlock(event, "weather_forecast", weather_radar);
		addBlock(event, "weather_forecast_2", weather_doppler_radar);
		addBlock(event, "weather_forecast_3", weather_pulse_radar);
		addBlock(event, "weather_machine", weather_constructor);
		addBlock(event, "weather_deflector", weather_deflector);
		addBlock(event, "anemometer", anemometer);
		addBlock(event, "sand_layer", sand_layer, false);
		
		addBlock(event, "machine_case", machineCase);
		addBlock(event, "storm_sensor", stormSensor);
		addBlock(event, "humidity_sensor", humiditySensor);
		addBlock(event, "rain_sensor", rainSensor);
		addBlock(event, "temperature_sensor", temperatureSensor);
		addBlock(event, "wind_sensor", windSensor);
		addBlock(event, "barometer_sensor", barometerSensor);
		addBlock(event, "radio_transmitter", radio);
		Weather2Remastered.debug("Finished registering blocks");
		isInitialized = true;
	}
	
	public static void addBlock(RegistryEvent.Register<Block> event, String registryName, Block block)
	{
		addBlock(event, registryName, block, true);
	}
	
	public static void addBlock(RegistryEvent.Register<Block> event, String registryName, Block block, boolean inCreativeTab)
	{
		block.setRegistryName(Weather2Remastered.MODID, registryName);
		ItemRegistry.addBlock(block, inCreativeTab);
		event.getRegistry().register(block);
	}
}