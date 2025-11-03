package net.mrbt0907.weather2.registry;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.block.BlockAnemometer;
import net.mrbt0907.weather2.block.BlockMachine;
import net.mrbt0907.weather2.block.BlockNewRadar;
import net.mrbt0907.weather2.block.BlockNewSensor;
import net.mrbt0907.weather2.block.BlockNewSiren;
import net.mrbt0907.weather2.block.BlockNewWeatherConstructor;
import net.mrbt0907.weather2.block.BlockNewWeatherDeflector;
import net.mrbt0907.weather2.block.BlockRadio;
import net.mrbt0907.weather2.block.BlockSandLayer;
import net.mrbt0907.weather2.block.BlockSensor;
import net.mrbt0907.weather2.block.BlockSiren;
import net.mrbt0907.weather2.block.BlockTSirenManual;
import net.mrbt0907.weather2.block.BlockWeatherConstructor;
import net.mrbt0907.weather2.block.BlockWeatherDeflector;
import net.mrbt0907.weather2.block.BlockWindVane;
import net.mrbt0907.weather2.block.tile.TileAnemometer;
import net.mrbt0907.weather2.block.tile.TileEntityTSirenManual;
import net.mrbt0907.weather2.block.tile.TileMachine;
import net.mrbt0907.weather2.block.tile.TileRadar;
import net.mrbt0907.weather2.block.tile.TileRadioTransmitter;
import net.mrbt0907.weather2.block.tile.TileSiren;
import net.mrbt0907.weather2.block.tile.TileWeatherConstructor;
import net.mrbt0907.weather2.block.tile.TileWeatherDeflector;
import net.mrbt0907.weather2.block.tile.TileWindVane;

@SuppressWarnings("unused")
public class BlockRegistry
{
	private static IForgeRegistry<Block> registry;
	
	public static final Block wire = new BlockSensor();
	public static final BlockRadio radio = new BlockRadio(Material.CLAY);
	public static final Block wind_chimes = new BlockNewSiren();
	public static final Block air_horn_siren = new BlockNewSiren();
	public static final Block emergency_siren_alt = new BlockNewSiren();
	public static final Block emergency_siren_alt_manual = new BlockNewSiren();
	//public static final Block wind_sock = new BlockNewSensor(0.25F, 0.0D, false, false, true, true);
	//public static final Block thermometer = new BlockNewSensor(0.25F, 250.0D, true, false, false, false);
	//public static final Block hygrometer = new BlockNewSensor(0.25F, 250.0D, false, true, false, false);
	public static final Block weather_doppler_radar = new BlockNewRadar(1);
	public static final Block weather_pulse_radar = new BlockNewRadar(2);
	public static final Block weather_humidifier = new BlockNewWeatherConstructor();
	public static final Block weather_humidifier_2 = new BlockNewWeatherConstructor();
	public static final Block weather_conditioner = new BlockNewWeatherDeflector();
	public static final Block weather_conditioner_2 = new BlockNewWeatherDeflector();
	//public static final Block ground_sensor_unit = new BlockNewSensor(1.0F, 0.0D, true, true, true, true);
	public static final Block wind_vane = new BlockWindVane();
	public static final Block anemometer = new BlockAnemometer();
	
	public static final Block machineCase = new BlockMachine(Material.CLAY);
	public static final Block stormSensor = new BlockNewSensor(Material.CLAY, 0);
	public static final Block humiditySensor = new BlockNewSensor(Material.CLAY, 1);
	public static final Block rainSensor = new BlockNewSensor(Material.CLAY, 2);
	public static final Block temperatureSensor = new BlockNewSensor(Material.CLAY, 3);
	public static final Block windSensor = new BlockNewSensor(Material.CLAY, 4);
	public static final Block barometerSensor = new BlockNewSensor(Material.CLAY, 5);
	
	public static final Block tornado_sensor = new BlockSensor();
	public static final Block emergency_siren_manual = new BlockTSirenManual();
	public static final Block emergency_siren = new BlockSiren();
	public static final Block weather_radar = new BlockNewRadar();
	public static final Block weather_constructor = new BlockWeatherConstructor();
	public static final Block weather_deflector = new BlockWeatherDeflector();
	public static final Block sand_layer = new BlockSandLayer();
	
	public static void register(RegistryEvent.Register<Block> event)
	{
		Weather2.debug("Registering blocks...");
		BlockRegistry.registry = event.getRegistry();
		BlockRegistry.addBlock("tornado_sensor", BlockRegistry.tornado_sensor);
		BlockRegistry.addBlock("tornado_siren", BlockRegistry.emergency_siren);
		BlockRegistry.addBlock("tornado_siren_manual", BlockRegistry.emergency_siren_manual);
		BlockRegistry.addBlock("wind_vane", BlockRegistry.wind_vane);
		BlockRegistry.addBlock("weather_forecast", BlockRegistry.weather_radar);
		BlockRegistry.addBlock("weather_forecast_2", BlockRegistry.weather_doppler_radar);
		BlockRegistry.addBlock("weather_forecast_3", BlockRegistry.weather_pulse_radar);
		BlockRegistry.addBlock("weather_machine", BlockRegistry.weather_constructor);
		BlockRegistry.addBlock("weather_deflector", BlockRegistry.weather_deflector);
		BlockRegistry.addBlock("anemometer", BlockRegistry.anemometer);
		BlockRegistry.addBlock("sand_layer", BlockRegistry.sand_layer, false);
		
		BlockRegistry.addBlock("machine_case", BlockRegistry.machineCase);
		BlockRegistry.addBlock("storm_sensor", BlockRegistry.stormSensor);
		BlockRegistry.addBlock("humidity_sensor", BlockRegistry.humiditySensor);
		BlockRegistry.addBlock("rain_sensor", BlockRegistry.rainSensor);
		BlockRegistry.addBlock("temperature_sensor", BlockRegistry.temperatureSensor);
		BlockRegistry.addBlock("wind_sensor", BlockRegistry.windSensor);
		BlockRegistry.addBlock("barometer_sensor", BlockRegistry.barometerSensor);
		BlockRegistry.addBlock("radio_transmitter", BlockRegistry.radio);
		
		BlockRegistry.addTileEntity("tornado_siren", TileSiren.class);
		BlockRegistry.addTileEntity("tornado_siren_manual", TileEntityTSirenManual.class);
		BlockRegistry.addTileEntity("wind_vane", TileWindVane.class);
		BlockRegistry.addTileEntity("weather_forecast", TileRadar.class);
		BlockRegistry.addTileEntity("weather_machine", TileWeatherConstructor.class);
		BlockRegistry.addTileEntity("weather_deflector", TileWeatherDeflector.class);
		BlockRegistry.addTileEntity("anemometer", TileAnemometer.class);
		BlockRegistry.addTileEntity("machine_case", TileMachine.class);
		BlockRegistry.addTileEntity("radio_transmitter", TileRadioTransmitter.class);
		BlockRegistry.registry = null;
		Weather2.debug("Finished registering blocks");
	}
	
	private static void addTileEntity(String registry_name, Class<? extends TileEntity> tile)
	{
		if (tile != null)
			GameRegistry.registerTileEntity(tile, new ResourceLocation(Weather2.MODID, registry_name));
	}
	
	private static void addBlock(String registry_name, Block block)
	{
		BlockRegistry.addBlock(registry_name, null, block, true);
	}
	
	
	private static void addBlock(String registry_name, Block block, boolean creative_tab)
	{
		BlockRegistry.addBlock(registry_name, null, block, creative_tab);
	}
	
	private static void addBlock(String registry_name, String ore_dict_name, Block block)
	{
		BlockRegistry.addBlock(registry_name, ore_dict_name, block, true);
	}
	
	private static void addBlock(String registry_name, String ore_dict_name, Block block, boolean creative_tab)
	{
		if (BlockRegistry.registry != null)
		{
			block.setRegistryName(new ResourceLocation(Weather2.OLD_MODID, registry_name));
			block.setTranslationKey(registry_name);
			
			if (ore_dict_name != null)
				OreDictionary.registerOre(ore_dict_name, block);
			if (creative_tab)
				block.setCreativeTab(Weather2.TAB);
			
			BlockRegistry.registry.register(block);			
			
			ItemRegistry.add(block);
			Weather2.debug("Registered block " + block.getRegistryName());
			return;
		}
		
		Weather2.error("Registry event returned null");
	}
}
