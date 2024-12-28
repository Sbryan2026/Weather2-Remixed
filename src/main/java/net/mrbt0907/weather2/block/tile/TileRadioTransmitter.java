package net.mrbt0907.weather2.block.tile;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.weather2.api.interfaces.IRadioTransmitter;
import net.mrbt0907.weather2.api.interfaces.ITileInteractable;

public class TileRadioTransmitter extends TileMachine implements IRadioTransmitter<TileEntity>, ITileInteractable
{
	protected String frequency;
	protected String message;
	protected ResourceLocation sound;
	
	public TileRadioTransmitter()
	{
		super();
		frequency = "";
		message = "";
		sound = null;
	}

    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
    	nbt.setString("frequency", frequency);
    	nbt.setString("message", message);
    	if (sound != null)
    		nbt.setString("sound", sound.toString());
        return super.writeToNBT(nbt);
    }

    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        frequency = nbt.getString("frequency");
        message = nbt.getString("message");
        if (nbt.hasKey("sound"))
        	sound = new ResourceLocation(nbt.getString("sound"));
    }
    
	@Override
	public void setRadioFrequency(TileEntity obj, String frequency)
	{
		this.frequency = frequency;
	}

	@Override
	public String getRadioFrequency(TileEntity obj)
	{
		return frequency;
	}

	@Override
	public void setRadioMessage(TileEntity obj, String message)
	{
		this.message = message;
	}

	@Override
	public String getRadioMessage(TileEntity obj)
	{
		return message;
	}

	@Override
	public void setRadioSound(TileEntity obj, ResourceLocation sound)
	{
		this.sound = sound;
	}

	@Override
	public ResourceLocation getRadioSound(TileEntity obj)
	{
		return sound;
	}

	@Override
	public boolean onTileActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		if (world.isRemote)
			openScreen();
		return true;
	}
	
	@SideOnly(Side.CLIENT)
	private void openScreen()
	{
		net.minecraft.client.Minecraft.getMinecraft().displayGuiScreen(new net.mrbt0907.weather2.client.gui.GuiEZConfig());
	}
	
	@Override
	public void invalidate()
	{
    	super.invalidate();
	}
}