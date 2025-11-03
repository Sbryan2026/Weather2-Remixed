package net.mrbt0907.weather2.client.entity.particle;

import extendedrenderer.particle.entity.EntityRotFX;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.mrbt0907.weather2.util.Maths;

public class ExtendedEntityRotFX extends EntityRotFX
{
	protected int ticksExisted;
	private float invMax;
	protected float startRed, startGreen, startBlue, startMult, finalRed, finalBlue, finalGreen, finalAdj, finalMult;
	
	public ExtendedEntityRotFX(World world, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, TextureAtlasSprite texture)
	{
		super(world, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn - 0.5D, zSpeedIn);
        setParticleTexture(texture);
		invMax = 1.0F / (particleMaxAge * startMult);
        particleGravity = 1F;
        particleScale = 1F;
        setMaxAge(100);
        setCanCollide(false);
		finalMult = 1.0F;
		startMult = 1.0F;
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if (finalAdj < 1.0F)
		{
			finalAdj = ticksExisted * invMax + finalMult;
			finalAdj = finalAdj > 1.0F ? 1.0F : finalAdj;

			float f = 1.0F - finalAdj;
			setRBGColorF(startRed * f + finalRed * finalAdj, startGreen * f + finalGreen * finalAdj, startBlue * f + finalBlue * finalAdj);
		}
		
		ticksExisted++;
	}
	
	public void setColor(float r, float g, float b)
	{
		startRed = r;
		startGreen = g;
		startBlue = b;
		finalRed = r;
		finalGreen = g;
		finalBlue = b;
	}
	
	public void setFinalColor(float percent, float r, float g, float b)
	{
		finalRed = r;
		finalGreen = g;
		finalBlue = b;
		finalAdj = 0.0F;
		finalMult = percent;
	}

	public void setColorFade(float percent)
	{
		startMult = Maths.clamp(percent, 0.0F, 1.0F);
		invMax = 1.0F / (particleMaxAge * startMult);
	}
	
	@Override
	public void setMaxAge(int particleLifeTime)
    {
        super.setMaxAge(particleLifeTime);
		invMax = 1.0F / (particleMaxAge * startMult);
    }

    @Override
    public int getFXLayer()
    {
        return 1;
    }

	@Override
	public int getBrightnessForRender(float partialTick)
    {
		Chunk chunk = world.getChunk((int) posX >> 4, (int) posZ >> 4);
		if (chunk == null) return 0;
		int x = (int) posX & 15;
        int y = (int) posY;
        int z = (int) posZ & 15;
		if (y > 255) y = 255;
		if (y < 0) y = 0;
		ExtendedBlockStorage[] storageArray = chunk.getBlockStorageArray();
		if (storageArray == null) return EnumSkyBlock.SKY.defaultLightValue;
		ExtendedBlockStorage storage = storageArray[y >> 4];
		if (storage == null) return EnumSkyBlock.SKY.defaultLightValue;
		int sky_light = !world.provider.hasSkyLight() ? 0 : storage.getSkyLight(x, y & 15, z);
		int block_light = storage.getBlockLight(x, y & 15, z);
        return sky_light << 20 | block_light << 4;
    }
}
