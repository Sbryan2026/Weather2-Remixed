package net.mrbt0907.weather2.client.entity.particle;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
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
import net.mrbt0907.weather2.util.Maths;

@SideOnly(Side.CLIENT)
public class ParticleMovingBlock extends Particle
{
	protected static final Minecraft MC = Minecraft.getMinecraft();
	protected final RenderManager renderManager;
	protected TileEntity tile;
	protected IBlockState state;
	protected Block block;
	
	public ParticleMovingBlock(World world, IBlockState state, BlockPos pos, double speedX, double speedY, double speedZ)
	{
		super(world, pos.getX(), pos.getY(), pos.getZ(), speedX, speedY, speedZ);
		renderManager = Minecraft.getMinecraft().getRenderManager();
		this.state = state;
		block = state.getBlock();
		if (block.hasTileEntity(state))
			tile = world.getTileEntity(pos);
		
		setBoundingBox(Block.FULL_BLOCK_AABB);
		setSize(1.0F, 1.0F);
	}

	@Override
	public void renderParticle(@Nonnull BufferBuilder buffer, @Nonnull Entity entity, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
    {
        doRender(this, entity.posX, entity.posY, entity.posZ, entity.rotationYaw, partialTicks);
    }
	
	public void doRender(ParticleMovingBlock entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
		if (state == null) return;
		
		EnumBlockRenderType renderType = state.getRenderType();
		float yaw = (float) Math.toDegrees(Maths.fastATan2(entity.motionZ, entity.motionX)) - 90F;
		float pitch = (float) -Math.toDegrees(Maths.fastATan2(entity.motionY, Math.sqrt(entity.motionX * entity.motionX + entity.motionZ * entity.motionZ)));

		if (renderType == EnumBlockRenderType.MODEL)
		{
			renderManager.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			GlStateManager.pushMatrix();
			GlStateManager.disableLighting();
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferbuilder = tessellator.getBuffer();


			bufferbuilder.begin(7, DefaultVertexFormats.BLOCK);
			BlockPos blockpos = new BlockPos(entity.posX, boundingBox.maxY, entity.posZ);
				
			GlStateManager.translate((float)(x), (float)(y), (float)(z));
			bufferbuilder.setTranslation((double)((float)(-blockpos.getX()) - 0.5F), (double)(-blockpos.getY()), (double)((float)(-blockpos.getZ()) - 0.5F));
			
			// Rotate in the direction this block needs to be flying
			//GlStateManager.rotate((float)(age * 0.1F * 180.0D / 12.566370964050293D - 0.0D), 1.0F, 0.0F, 0.0F);
			//GlStateManager.rotate((float)(age * 0.1F * 180.0D / (Math.PI * 2D) - 0.0D), 0.0F, 1.0F, 0.0F);
			//GlStateManager.rotate((float)(age * 0.1F * 180.0D / (Math.PI * 2D) - 0.0D), 0.0F, 0.0F, 1.0F);
			
			GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
			GlStateManager.scale(entity.width, entity.height, entity.width);
			
			BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
			blockrendererdispatcher.getBlockModelRenderer().renderModel(world, blockrendererdispatcher.getModelForState(state), state, blockpos, bufferbuilder, false, MathHelper.getPositionRandom(new BlockPos(entity.posX, entity.posY, entity.posZ)));
			bufferbuilder.setTranslation(0.0D, 0.0D, 0.0D);
			tessellator.draw();

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
					GlStateManager.translate((float)(x), (float)(y), (float)(z));
					GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
					GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
					GlStateManager.scale(entity.width, entity.height, entity.width);
					TileEntityRendererDispatcher.instance.render(tile, 0, 0, 0, partialTicks);
					GlStateManager.popMatrix();
				}
				catch (Exception e) {}
		}
    }
	
	@Override
	public int getFXLayer() {return 1;}

	@Override
	public void setParticleTexture(@Nonnull TextureAtlasSprite texture) {}

	@Override
	public void setParticleTextureIndex(int particleTextureIndex) {}
}