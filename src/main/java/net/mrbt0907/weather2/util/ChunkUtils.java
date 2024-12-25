package net.mrbt0907.weather2.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ChunkUtils
{
	public static IBlockState getBlockState(World world, int x, int y, int z)
	{
		return getBlockState(world, new BlockPos(x, y, z));
	}
	
	public static IBlockState getBlockState(World world, BlockPos pos)
	{
		return world.getBlockState(pos);
	}
	
	public static void setBlockState(World world, int x, int y, int z, IBlockState newState)
	{
		setBlockState(world, new BlockPos(x, y, z), newState);
	}
	
	public static void setBlockState(World world, BlockPos pos, IBlockState newState)
	{
		world.setBlockState(pos, newState);
	}
	
	public static boolean isValidPos(World world, int y)
	{
		return world != null && y > -1 && y < 256;
	}
}