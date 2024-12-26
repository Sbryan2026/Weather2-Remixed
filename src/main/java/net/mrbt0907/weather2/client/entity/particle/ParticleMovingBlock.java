package net.mrbt0907.weather2.client.entity.particle;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.registry.ParticleRegistry;

@SideOnly(Side.CLIENT)
public class ParticleMovingBlock extends Particle
{
	protected final RenderManager renderManager;
	protected TileEntity tile;
	protected IBlockState state;
	
	public ParticleMovingBlock(World world, IBlockState state, double x, double y, double z, double speedX, double speedY, double speedZ)
	{
		super(world, x, y, z, speedX, speedY, speedZ);
		renderManager = Minecraft.getMinecraft().getRenderManager();
		this.state = state;
		tile = null;
		
		setBoundingBox(Block.FULL_BLOCK_AABB);
		super.setParticleTexture(ParticleRegistry.cloud256);
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
    {
		
        doRender(buffer, entityIn, 0, 0, 0, partialTicks);
        
    }
	
	protected void doRender(BufferBuilder buffer, Entity entity, double x, double y, double z, float partialTicks)
    {
		if (state == null) return;
		
		EnumBlockRenderType renderType = state.getRenderType();
		World world = renderManager.world;
		int age = particleAge * 5;

		if (renderType == EnumBlockRenderType.MODEL)
		{
			Tessellator tessellator = Tessellator.getInstance();
			tessellator.draw();
			renderManager.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			GlStateManager.pushMatrix();
			GlStateManager.disableLighting();
			buffer.begin(7, DefaultVertexFormats.BLOCK);
			BlockPos blockpos = new BlockPos(posX, posY, posZ);

			GlStateManager.translate((float)(x), (float)(y), (float)(z));
			buffer.setTranslation((double)((float)(-blockpos.getX()) - 0.5F), (double)(-blockpos.getY()), (double)((float)(-blockpos.getZ()) - 0.5F));
			GlStateManager.rotate((float)(age * 0.1F * 180.0D / 12.566370964050293D - 0.0D), 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate((float)(age * 0.1F * 180.0D / (Math.PI * 2D) - 0.0D), 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate((float)(age * 0.1F * 180.0D / (Math.PI * 2D) - 0.0D), 0.0F, 0.0F, 1.0F);
			
			BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
			blockrendererdispatcher.getBlockModelRenderer().renderModel(world, blockrendererdispatcher.getModelForState(state), state, blockpos, buffer, false, MathHelper.getPositionRandom(blockpos));
			buffer.setTranslation(0.0D, 0.0D, 0.0D);
			GlStateManager.enableLighting();
			GlStateManager.popMatrix();
		}
		else if (renderType == EnumBlockRenderType.ENTITYBLOCK_ANIMATED)
		{
			if (tile == null)
				tile = state.getBlock().createTileEntity(world, state);
					
			if (tile != null)
				try
				{
					GlStateManager.pushMatrix();
					TileEntityRendererDispatcher.instance.render(tile, x, y, z, partialTicks);
					GlStateManager.popMatrix();
				}
				catch (Exception e)
				{e.printStackTrace();}
		}
    }
	
	@Override
	public int getFXLayer() {return 1;}
	@Override
	public void setParticleTexture(TextureAtlasSprite texture) {}
	@Override
	public void setParticleTextureIndex(int particleTextureIndex) {}
}