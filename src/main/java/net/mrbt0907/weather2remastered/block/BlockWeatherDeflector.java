package net.mrbt0907.weather2remastered.block;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

public class BlockWeatherDeflector extends BlockMachine
{
    public BlockWeatherDeflector()
    {
        super(Properties.of(Material.CLAY));
    }

	@Override
	public TileEntity newBlockEntity(IBlockReader reader)
	{
		return null;//TileWeatherDeflector;
	}
}
