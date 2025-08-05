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

public class EZGUIEX extends Screen {

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
	public static final String PREFIX = "btn_";
    public EZGUIEX() {
        super(new StringTextComponent("EZ Config Advanced"));
    }
    @Override
    protected void init() {
        super.init();
        int centerX = (this.width - xSize) / 2;
        int centerY = (this.height - ySize) / 2;
        this.addButton(new Button(centerX + 170, centerY + 231, 80, 20, new TranslationTextComponent("configex.ezgui.save"),
            button -> {
            	System.out.println("We should save the nbt data and sync it here for the advanced gui!");
            	this.minecraft.setScreen(null);
            }
        ));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        Minecraft.getInstance().getTextureManager().bind(background);
        int centerX = (this.width - xSize) / 2;
        int centerY = (this.height - ySize) / 2;
        blit(matrixStack, centerX, centerY, 0, 0, xSize, ySize, xSize, ySize);
        drawCenteredString(matrixStack, this.font,
            new TranslationTextComponent("configex.ezgui.title.advanced") // or new StringTextComponent("EZ Config")
                .append(" - " + "Page " + String.valueOf(page)),
            this.width / 2,
            centerY + 15,
            0xFFFFFF
        );
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}