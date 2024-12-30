package net.mrbt0907.weather2remastered.block;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

public class BlockSiren extends BlockMachine
{
	//public static final PropertyBool ENABLED = PropertyBool.create("enabled");

	public BlockSiren()
	{
		super(Properties.of(Material.CLAY));
		//setDefaultState(blockState.getBaseState().withProperty(ENABLED, Boolean.valueOf(true)));
	}


	@Override
	public TileEntity newBlockEntity(IBlockReader reader)
	{
		return null;//TileSiren()
	}
}
