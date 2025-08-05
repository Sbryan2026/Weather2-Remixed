package net.mrbt0907.weather2remastered;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.mrbt0907.weather2remastered.gui.EZGUI;
import net.mrbt0907.weather2remastered.gui.EZGUIEX;
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientProxy
{
	public static void preInit()
	{
		
	}
	
	public static void init()
	{
		
	}
	
	public static void postInit()
	{
		
	}
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
    public static void onGuiInit(InitGuiEvent.Post event) {
        if (event.getGui().isPauseScreen() && Minecraft.getInstance().level != null && !(event.getGui() instanceof EZGUI)&& !(event.getGui() instanceof EZGUIEX)) {
            event.addWidget(new Button((event.getGui().width - 200) / 2, 10, 200, 20, new StringTextComponent("Weather2 EZConfig"),
                button -> Minecraft.getInstance().setScreen(new EZGUI())
            ));
        }
    }
}
