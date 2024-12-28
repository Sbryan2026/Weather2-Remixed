package net.mrbt0907.weather2.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.mrbt0907.weather2.api.interfaces.ITileInteractable;
import net.mrbt0907.weather2.block.tile.TileRadioTransmitter;

public class BlockRadio extends BlockContainer
{
	public BlockRadio(Material materialIn)
	{
		super(materialIn);
	}
	
	@Override
    public TileEntity createNewTileEntity(World var1, int meta)
    {
        return new TileRadioTransmitter();
    }
	
	@Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
        if (world.isRemote && hand == EnumHand.MAIN_HAND)
        {
            TileEntity tEnt = world.getTileEntity(pos);

            if (tEnt instanceof ITileInteractable)
                ((ITileInteractable) tEnt).onTileActivated(world, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
        }

        return true;
    }
}