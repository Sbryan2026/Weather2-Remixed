package net.mrbt0907.weather2remastered.registry;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.mrbt0907.weather2remastered.Weather2Remastered;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class ItemRegistry
{
	private static final List<BlockItem> BLOCKS = new ArrayList<BlockItem>();
	public static final Item sensor = new Item(new Item.Properties().tab(Weather2Remastered.TAB));
	public static final Item thermometer = new Item(new Item.Properties().tab(Weather2Remastered.TAB));
	public static final Item hygrometer = new Item(new Item.Properties().tab(Weather2Remastered.TAB));
	public static final Item anemometer = new Item(new Item.Properties().tab(Weather2Remastered.TAB));
	public static final Item radio = new Item(new Item.Properties().tab(Weather2Remastered.TAB));
	
	public static final Item itemMotor = new Item(new Item.Properties().tab(Weather2Remastered.TAB));
	public static final Item itemSpeaker = new Item(new Item.Properties().tab(Weather2Remastered.TAB));
	public static final Item itemAntenna0 = new Item(new Item.Properties().tab(Weather2Remastered.TAB));
	public static final Item itemAntenna1 = new Item(new Item.Properties().tab(Weather2Remastered.TAB));
	public static final Item itemAntenna2 = new Item(new Item.Properties().tab(Weather2Remastered.TAB));
	public static final Item itemCPU0 = new Item(new Item.Properties().tab(Weather2Remastered.TAB));
	public static final Item itemCPU1 = new Item(new Item.Properties().tab(Weather2Remastered.TAB));
	public static final Item itemCPU2 = new Item(new Item.Properties().tab(Weather2Remastered.TAB));
	public static final Item itemBulb = new Item(new Item.Properties().tab(Weather2Remastered.TAB));
	public static final Item itemDryBulb = new Item(new Item.Properties().tab(Weather2Remastered.TAB));
	public static final Item itemWetBulb = new Item(new Item.Properties().tab(Weather2Remastered.TAB));
	public static final Item itemLCD0 = new Item(new Item.Properties().tab(Weather2Remastered.TAB));
	public static final Item itemLCD1 = new Item(new Item.Properties().tab(Weather2Remastered.TAB));
	
	public static final Item itemSandLayer = new Item(new Item.Properties().tab(Weather2Remastered.TAB));
	public static final Item itemWeatherRecipe = new Item(new Item.Properties().tab(Weather2Remastered.TAB));
	public static final Item itemPocketSand = new Item(new Item.Properties().tab(Weather2Remastered.TAB));
	
	private static boolean isInitialized = false;
	@SubscribeEvent
	public static void onBlockRegistry(RegistryEvent.Register<Item> event)
	{
		if (isInitialized) return;
		Weather2Remastered.debug("Registering items...");
		addItem(event, "sand_layer_placeable", itemSandLayer);
		addItem(event, "weather_item", itemWeatherRecipe);
		addItem(event, "pocket_sand", itemPocketSand);

		addItem(event, "handheld_thermometer", thermometer);
		addItem(event, "handheld_hygrometer", hygrometer);
		addItem(event, "handheld_anemometer", anemometer);
		
		addItem(event, "motor", itemMotor);
		addItem(event, "speaker", itemSpeaker);
		addItem(event, "antenna_0", itemAntenna0);
		addItem(event, "antenna_1", itemAntenna1);
		addItem(event, "antenna_2", itemAntenna2);
		addItem(event, "cpu_0", itemCPU0);
		addItem(event, "cpu_1", itemCPU1);
		addItem(event, "cpu_2", itemCPU2);
		addItem(event, "bulb", itemBulb);
		addItem(event, "bulb_dry", itemDryBulb);
		addItem(event, "bulb_wet", itemWetBulb);
		addItem(event, "lcd_0", itemLCD0);
		addItem(event, "lcd_1", itemLCD1);
		BLOCKS.forEach(block -> event.getRegistry().register(block)); BLOCKS.clear();
		Weather2Remastered.debug("Finished registering items");
		isInitialized = true;
	}
	
	public static void addBlock(Block block, boolean inCreativeTab)
	{
		BlockItem item = new BlockItem(block, new Item.Properties().tab(inCreativeTab ? Weather2Remastered.TAB : null));
		item.setRegistryName(block.getRegistryName());
		BLOCKS.add(item);
	}
	
	public static void addItem(RegistryEvent.Register<Item> event, String registryName, Item item)
	{
		item.setRegistryName(registryName);
		event.getRegistry().register(item);
	}
}
