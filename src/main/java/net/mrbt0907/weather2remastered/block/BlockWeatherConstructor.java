package net.mrbt0907.weather2remastered.block;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

public class BlockWeatherConstructor extends BlockMachine
{
    public BlockWeatherConstructor()
    {
        super(Properties.of(Material.CLAY));
    }

	@Override
	public TileEntity newBlockEntity(IBlockReader p_196283_1_)
	{
		return null; //TileWeatherConstructor
	}
}
