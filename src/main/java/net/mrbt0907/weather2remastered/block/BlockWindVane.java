package net.mrbt0907.weather2remastered.block;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.IBlockReader;

public class BlockWindVane extends BlockMachine
{
	public static final AxisAlignedBB AABB = new AxisAlignedBB(0.4F, 0, 0.4F, 0.6F, 0.3F, 0.6F);
	
    public BlockWindVane()
    {
        super(Properties.of(Material.CLAY));
    }
    
	@Override
	public TileEntity newBlockEntity(IBlockReader reader)
	{
		return null; //TileWindVane
	}
}
