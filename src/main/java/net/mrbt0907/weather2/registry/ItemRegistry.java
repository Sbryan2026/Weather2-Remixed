package net.mrbt0907.weather2.registry;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.item.ItemPocketSand;
import net.mrbt0907.weather2.item.ItemRadar;
import net.mrbt0907.weather2.item.ItemSandLayer;
import net.mrbt0907.weather2.item.ItemSensor;
import net.mrbt0907.weather2.item.ItemWeatherRecipe;

public class ItemRegistry
{
	private static IForgeRegistry<Item> registry;
	private static final List<Block> item_blocks = new ArrayList<Block>();
	
	public static final Item radar = new ItemRadar();
	public static final Item sensor = new ItemSensor(0);
	public static final Item thermometer = new ItemSensor(1);
	public static final Item hygrometer = new ItemSensor(2);
	public static final Item anemometer = new ItemSensor(3);
	public static final Item radio = new Item();
	
	public static final Item itemMotor = new Item();
	public static final Item itemSpeaker = new Item();
	public static final Item itemAntenna0 = new Item();
	public static final Item itemAntenna1 = new Item();
	public static final Item itemAntenna2 = new Item();
	public static final Item itemCPU0 = new Item();
	public static final Item itemCPU1 = new Item();
	public static final Item itemCPU2 = new Item();
	public static final Item itemBulb = new Item();
	public static final Item itemDryBulb = new Item();
	public static final Item itemWetBulb = new Item();
	public static final Item itemLCD0 = new Item();
	public static final Item itemLCD1 = new Item();
	
	public static final Item itemSandLayer = new ItemSandLayer(BlockRegistry.sand_layer);
	public static final Item itemWeatherRecipe = new ItemWeatherRecipe();
	public static final Item itemPocketSand = new ItemPocketSand();
	
	public static void add(Block block)
	{
		ItemRegistry.item_blocks.add(block);
	}
	
	public static void register(RegistryEvent.Register<Item> event)
	{
		Weather2.debug("Registering items...");
		ItemRegistry.registry = event.getRegistry();
		ItemRegistry.add("sand_layer_placeable", ItemRegistry.itemSandLayer);
		ItemRegistry.add("weather_item", ItemRegistry.itemWeatherRecipe);
		ItemRegistry.add("pocket_sand", ItemRegistry.itemPocketSand);
		
		ItemRegistry.add("handheld_radar", ItemRegistry.radar);
		ItemRegistry.add("handheld_thermometer", ItemRegistry.thermometer);
		ItemRegistry.add("handheld_hygrometer", ItemRegistry.hygrometer);
		ItemRegistry.add("handheld_anemometer", ItemRegistry.anemometer);
		
		ItemRegistry.add("motor", ItemRegistry.itemMotor);
		ItemRegistry.add("speaker", ItemRegistry.itemSpeaker);
		ItemRegistry.add("antenna_0", ItemRegistry.itemAntenna0);
		ItemRegistry.add("antenna_1", ItemRegistry.itemAntenna1);
		ItemRegistry.add("antenna_2", ItemRegistry.itemAntenna2);
		ItemRegistry.add("cpu_0", ItemRegistry.itemCPU0);
		ItemRegistry.add("cpu_1", ItemRegistry.itemCPU1);
		ItemRegistry.add("cpu_2", ItemRegistry.itemCPU2);
		ItemRegistry.add("bulb", ItemRegistry.itemBulb);
		ItemRegistry.add("bulb_dry", ItemRegistry.itemDryBulb);
		ItemRegistry.add("bulb_wet", ItemRegistry.itemWetBulb);
		ItemRegistry.add("lcd_0", ItemRegistry.itemLCD0);
		ItemRegistry.add("lcd_1", ItemRegistry.itemLCD1);
		
		for (Block block : ItemRegistry.item_blocks)
			ItemRegistry.add(block.getRegistryName().getPath(), new ItemBlock(block));
		
		ItemRegistry.registry = null;
		Weather2.debug("Finished registering items");
	}
	
	public static void add(String name, Item item)
	{
		ItemRegistry.add(name, null, item);
	}
	
	public static void add(String name, String ore_dict_name, Item item)
	{
		if (ItemRegistry.registry != null)
		{
			item.setRegistryName(new ResourceLocation(Weather2.OLD_MODID, name));
			item.setTranslationKey(name);
			
			if (ore_dict_name != null)
				OreDictionary.registerOre(ore_dict_name, item);
			item.setCreativeTab(Weather2.TAB);
			ItemRegistry.registry.register(item);
			
			if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
				ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
			
			Weather2.debug("Registered item " + item.getRegistryName());
			return;
		}
		Weather2.error("Registry event returned null");
	}
}
