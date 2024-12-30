package net.mrbt0907.weather2remastered.block;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

public class BlockSensor extends BlockMachine
{
	//public static final PropertyInteger POWER = PropertyInteger.create("power", 0, 15);
	
	/**Determines what the sensor will scan<br>0 - Scan Stage<br>1 - Scan Humidity<br>2 - Scan Rain<br>3 - Scan Temperature<br>4 - Scan Wind*/
	@SuppressWarnings("unused")
	private int scanType;
	
    public BlockSensor(int scanType)
    {
    	super(Properties.of(Material.CLAY)); 
        this.scanType = scanType;
    }
    
    @Override
	public TileEntity newBlockEntity(IBlockReader p_196283_1_)
    {
		return null; //TileSensor
	}
}
