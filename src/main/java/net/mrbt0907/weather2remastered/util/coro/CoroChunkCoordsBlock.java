package net.mrbt0907.weather2remastered.util.coro;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

public class CoroChunkCoordsBlock extends CoroBlockCoord {

	public Block block = null;
	public int meta = 0;
	
	public CoroChunkCoordsBlock(int par1, int par2, int par3, Block parBlockID)
	{
		this(par1, par2, par3, parBlockID, 0);
	}
	
	public CoroChunkCoordsBlock(int par1, int par2, int par3, Block parBlockID, int parMeta)
    {
        super(par1, par2, par3);
        block = parBlockID;
        meta = parMeta;
    }
	
	public CoroChunkCoordsBlock(CoroBlockCoord par1BlockCoord, Block parBlockID)
	{
		this(par1BlockCoord, parBlockID, 0);
	}

    public CoroChunkCoordsBlock(CoroBlockCoord par1BlockCoord, Block parBlockID, int parMeta)
    {
        super(par1BlockCoord);
        block = parBlockID;
        meta = parMeta;
    }
    
    public BlockPos toBlockPos() {
    	return new BlockPos(this.posX, this.posY, this.posZ);
    }
	
}
