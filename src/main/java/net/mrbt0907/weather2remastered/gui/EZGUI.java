package net.mrbt0907.weather2remastered.gui;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.mrbt0907.configex.ConfigManager;
import net.mrbt0907.weather2remastered.gui.EZConfigParser;
import net.mrbt0907.weather2remastered.api.EZGUIAPI;
import net.mrbt0907.weather2remastered.ClientProxy;
import net.mrbt0907.weather2remastered.Weather2Remastered;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class EZGUI extends Screen {

	public int xCenter;
	public int yCenter;
	public int xStart;
	public int yStart;
	/** The X size of the inventory window in pixels. */
	protected int xSize = 256;
	/** The Y size of the inventory window in pixels. */
	protected int ySize = 256;
    private ResourceLocation background = new ResourceLocation(Weather2Remastered.MODID, "textures/gui/ez_gui_2.png");
    private int page = 0;
    public int subPage = 0;
	public int maxSubPages = 0;
	public int maxEntries = 6;
	private boolean send = false;
	//Locals
	private static final String[] PAGEL = {"configex.ezgui.graphics", "configex.ezgui.system", "configex.ezgui.storms", "configex.ezgui.dimensions"};
	private static final String[] FLAGL = {"flag.op","flag.reload"};
	public static final String PREFIX = "btn_";
	//Main Buttons
	public static final String B_EXIT = "m_exit";
	public static final String B_ADVANCED = "m_advanced";
	public static final String B_NEXT = "m_next";
	public static final String B_PREVIOUS = "m_previous";
	public static final String B_GRAPHICS = "m_graphics";
	public static final String B_SYSTEM = "m_system";
	public static final String B_STORM = "m_storm";
	public static final String B_DIMENSION = "m_dimension";
	List<String> settings = new ArrayList<String>();
	public HashMap<Integer, String> buttons = new HashMap<Integer, String>();
	public CompoundNBT nbtSendCache;
    public EZGUI() {
        super(new StringTextComponent("EZ Config"));
        //only sync request on initial gui open
      	EZConfigParser.nbtRealServerData = new CompoundNBT();
      	//PacketEZGUI.sync();
      	//Initialize send cache.
      	nbtSendCache = new CompoundNBT();
      	//ClientProxy.clientTickHandler.op = ConfigManager.getPermissionLevel() > 3;
      	System.out.println("clientproxy clienttickhanlder no exist! and packetezgui.sync no exist either!");
      	EZGUIAPI.refreshOptions();
    }

    @Override
    protected void init() {
        super.init();
        
        int centerX = (this.width - xSize) / 2;
        int centerY = (this.height - ySize) / 2;

        this.addButton(new Button(centerX+8, centerY+47, 56, 20, new TranslationTextComponent("configex.ezgui.graphics"),button -> {
        	page = 0;
        }));
        this.addButton(new Button(centerX+68, centerY+47, 56, 20, new TranslationTextComponent("configex.ezgui.system"),button -> {
        	page = 1;
        }));
        this.addButton(new Button(centerX+128, centerY+47, 56, 20, new TranslationTextComponent("configex.ezgui.storms"),button -> {
        	page = 2;
        }));
        this.addButton(new Button(centerX+188, centerY+47, 60, 20, new TranslationTextComponent("configex.ezgui.dimensions"),button -> {
        	page = 3;
        }));
        this.addButton(new Button(
                centerX + 120, centerY + 231, 60, 20,
                new TranslationTextComponent("configex.ezgui.advanced"),
                button -> {
                	this.minecraft.setScreen(new EZGUIEX());
                }
            ));
        this.addButton(new Button(
            centerX + 185, centerY + 231, 65, 20,
            new TranslationTextComponent("configex.ezgui.save"),
            button -> {
            	this.minecraft.setScreen(null);
            }
        ));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack); // dim background

        // Draw the background image
        Minecraft.getInstance().getTextureManager().bind(background);
        int centerX = (this.width - xSize) / 2;
        int centerY = (this.height - ySize) / 2;
        blit(matrixStack, centerX, centerY, 0, 0, xSize, ySize, xSize, ySize);

        // Draw a title or dynamic text
        drawCenteredString(matrixStack, this.font,
            new TranslationTextComponent("configex.ezgui.title") // or new StringTextComponent("EZ Config")
                .append(" - " + new TranslationTextComponent(PAGEL[page]).getString()),
            this.width / 2,
            centerY + 15,
            0xFFFFFF
        );

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return true; // GUI will pause the game
    }
}