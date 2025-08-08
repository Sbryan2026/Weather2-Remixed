package net.mrbt0907.weather2remastered.util;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ChunkUtils
{
	public static BlockState getBlockState(World world, int x, int y, int z)
	{
		return getBlockState(world, new BlockPos(x, y, z));
	}
	
	public static BlockState getBlockState(World world, BlockPos pos)
	{
		return world.getBlockState(pos);
	}
	
	public static void setBlockState(World world, int x, int y, int z, BlockState newState)
	{
		setBlockState(world, new BlockPos(x, y, z), newState);
	}
	
	public static void setBlockState(World world, BlockPos pos, BlockState newState)
	{
		world.setBlockAndUpdate(pos, newState);
	}
	
	public static boolean isValidPos(World world, int y)
	{
		return world != null && y > -1 && y < 256;
	}
}
