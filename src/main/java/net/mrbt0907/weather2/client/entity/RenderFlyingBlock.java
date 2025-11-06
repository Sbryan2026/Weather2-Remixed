package net.mrbt0907.weather2.client.entity;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.weather2.entity.EntityHail;
import net.mrbt0907.weather2.entity.EntityMovingBlock;
import net.mrbt0907.weather2.util.Maths;

@SideOnly(Side.CLIENT)
public class RenderFlyingBlock extends Render<Entity>
{
	Block renderBlock;
	TileEntity tile;
	
	public RenderFlyingBlock(RenderManager manager)
    {
		this(manager, null);
    }
	
    public RenderFlyingBlock(RenderManager manager, Block parBlock)
    {
    	super(manager);
    	renderBlock = parBlock;
    	tile = null;
    }
    
    @Override
	protected ResourceLocation getEntityTexture(Entity entity)
    {
		return TextureMap.LOCATION_BLOCKS_TEXTURE;
	}

    @Override
    public void doRender(Entity entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
		IBlockState state = null;
		if (entity instanceof EntityMovingBlock)
			state = ((EntityMovingBlock) entity).state;
		
		else if (renderBlock != null)
			state = renderBlock.getDefaultState();
		
		if (state == null) return;
		
		double size = entity.width;
		if (entity instanceof EntityHail)
			size = ((EntityHail)entity).size;

		EnumBlockRenderType renderType = state.getRenderType();
		World world = entity.world;
		float yaw = (float) Math.toDegrees(Maths.fastATan2(entity.motionZ, entity.motionX)) - 90F;
		float pitch = (float) -Math.toDegrees(Maths.fastATan2(entity.motionY, Math.sqrt(entity.motionX * entity.motionX + entity.motionZ * entity.motionZ)));

		if (renderType == EnumBlockRenderType.MODEL)
		{
			this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			GlStateManager.pushMatrix();
			GlStateManager.disableLighting();
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferbuilder = tessellator.getBuffer();

			if (this.renderOutlines)
			{
				GlStateManager.enableColorMaterial();
				GlStateManager.enableOutlineMode(this.getTeamColor(entity));
			}

			bufferbuilder.begin(7, DefaultVertexFormats.BLOCK);
			BlockPos blockpos = new BlockPos(entity.posX, entity.getEntityBoundingBox().maxY, entity.posZ);
				
			GlStateManager.translate((float)(x), (float)(y), (float)(z));
			bufferbuilder.setTranslation((double)((float)(-blockpos.getX()) - 0.5F), (double)(-blockpos.getY()), (double)((float)(-blockpos.getZ()) - 0.5F));
			
			// Rotate in the direction this block needs to be flying
			//GlStateManager.rotate((float)(age * 0.1F * 180.0D / 12.566370964050293D - 0.0D), 1.0F, 0.0F, 0.0F);
			//GlStateManager.rotate((float)(age * 0.1F * 180.0D / (Math.PI * 2D) - 0.0D), 0.0F, 1.0F, 0.0F);
			//GlStateManager.rotate((float)(age * 0.1F * 180.0D / (Math.PI * 2D) - 0.0D), 0.0F, 0.0F, 1.0F);
			
			GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
			GlStateManager.scale(size, size, size);
			
			BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
			blockrendererdispatcher.getBlockModelRenderer().renderModel(world, blockrendererdispatcher.getModelForState(state), state, blockpos, bufferbuilder, false, MathHelper.getPositionRandom(entity.getPosition()));
			bufferbuilder.setTranslation(0.0D, 0.0D, 0.0D);
			tessellator.draw();

			if (this.renderOutlines)
			{
				GlStateManager.disableOutlineMode();
				GlStateManager.disableColorMaterial();
			}

			GlStateManager.enableLighting();
			GlStateManager.popMatrix();
			super.doRender(entity, x, y, z, entityYaw, partialTicks);
		}
		else if (renderType == EnumBlockRenderType.ENTITYBLOCK_ANIMATED)
		{
			if (entity instanceof EntityMovingBlock)
			{
				EntityMovingBlock movingBlock = (EntityMovingBlock) entity;

				if (movingBlock.tileClass != null)
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
							GlStateManager.scale(size, size, size);
							if (this.renderOutlines)
							{
								GlStateManager.enableColorMaterial();
								GlStateManager.enableOutlineMode(this.getTeamColor(entity));
							}

							TileEntityRendererDispatcher.instance.render(tile, 0, 0, 0, partialTicks);
							
							
							GlStateManager.popMatrix();
						}
						catch (Exception e)
						{e.printStackTrace();}
				}
			}
			super.doRender(entity, x, y, z, entityYaw, partialTicks);
		}
    }
}
