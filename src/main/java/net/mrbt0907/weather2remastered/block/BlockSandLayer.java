package net.mrbt0907.weather2remastered.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.state.Property;

public class BlockSandLayer extends Block
{
	//public static final PropertyInteger LAYERS = PropertyInteger.create("layers", 1, 8);
	/*protected static final AxisAlignedBB[] SAND_AABB = new AxisAlignedBB[] {
		new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.0D, 1.0D), 
		new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D), 
		new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.25D, 1.0D), 
		new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.375D, 1.0D), 
		new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D), 
		new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.625D, 1.0D), 
		new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.75D, 1.0D), 
		new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.875D, 1.0D), 
		new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D)};*/

	@SuppressWarnings("rawtypes")
	public static Property LAYERS;

	public BlockSandLayer()
	{
		super(Properties.of(Material.SAND));
	}
}