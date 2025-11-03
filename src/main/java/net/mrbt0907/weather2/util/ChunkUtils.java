package net.mrbt0907.weather2.util;

import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ChunkUtils
{
	public static IBlockState getBlockState(World world, int x, int y, int z)
	{
		return ChunkUtils.getBlockState(world, new BlockPos(x, y, z));
	}
	
	public static IBlockState getBlockState(World world, BlockPos pos)
	{
		return world.getBlockState(pos);
	}
	
	public static void setBlockState(World world, int x, int y, int z, IBlockState newState)
	{
		ChunkUtils.setBlockState(world, new BlockPos(x, y, z), newState);
	}
	
	public static void setBlockState(World world, BlockPos pos, IBlockState newState)
	{
		if (newState.getBlock() instanceof BlockAir && ChunkUtils.getBlockState(world, pos.up()).getMaterial().isLiquid())
			world.setBlockState(pos, newState);
		else
			world.setBlockState(pos, newState, 2 | 16);
	}
	
	public static boolean isValidPos(World world, int y)
	{
		return world != null && y > -1 && y < 256;
	}
}