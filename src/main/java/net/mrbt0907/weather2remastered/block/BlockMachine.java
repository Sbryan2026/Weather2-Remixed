package net.mrbt0907.weather2remastered.block;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraftforge.common.ToolType;

public abstract class BlockMachine extends ContainerBlock
{
	public BlockMachine(Properties properties)
	{
		super
		(
			properties
	    	.harvestLevel(1)
	    	.harvestTool(ToolType.PICKAXE)
	    	.strength(0.6F, 10.0F)
	    );
	}
	
	@Override
	public BlockRenderType getRenderShape(BlockState state)
	{
	      return BlockRenderType.MODEL;
	}
}
