package net.mrbt0907.weather2remastered.block;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

public class BlockRadio extends BlockMachine
{
	public BlockRadio()
	{
		super(Properties.of(Material.CLAY));
	}

	@Override
	public TileEntity newBlockEntity(IBlockReader reader)
	{
		return null; //TileRadioTransmitter
	}
}