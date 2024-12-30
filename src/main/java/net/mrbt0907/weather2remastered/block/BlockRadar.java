package net.mrbt0907.weather2remastered.block;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

public class BlockRadar extends BlockMachine
{
	private int tier = 0;
	
	public BlockRadar(int tier)
	{
		super
    	(
    		Properties.of(Material.CLAY)
    	);
		

		this.tier = tier;
	}
	
	public int getTier()
	{
		return tier;
	}

	@Override
	public TileEntity newBlockEntity(IBlockReader reader)
	{
		return null; //TileRadar(tier)
	}
}
