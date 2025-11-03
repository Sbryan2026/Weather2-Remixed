package net.mrbt0907.weather2.client.rendering.manager;

import java.nio.FloatBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import CoroUtil.config.ConfigCoroUtil;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.ShaderManager;
import extendedrenderer.particle.entity.EntityRotFX;
import extendedrenderer.render.RotatingParticleManager;
import extendedrenderer.shader.InstancedMeshParticle;
import extendedrenderer.shader.Matrix4fe;
import extendedrenderer.shader.MeshBufferManagerParticle;
import extendedrenderer.shader.ShaderEngine;
import extendedrenderer.shader.ShaderProgram;
import extendedrenderer.shader.Transformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.mrbt0907.weather2.config.ConfigClient;
import net.mrbt0907.weather2.util.Maths;

public class ParticleManagerEX extends RotatingParticleManager
{
    protected static final Minecraft MC = Minecraft.getMinecraft();
    protected static final Tessellator TESSELLATOR = Tessellator.getInstance();
    protected static final ResourceLocation PARTICLE_TEXTURES = new ResourceLocation("textures/particle/particles.png");
    protected final TextureManager renderer;
    public final Comparator<? super Particle> COMPARE_DISTANCE = (pA, pB) ->
	{
		if (pA == null) return 0;
		if (pB == null) return 0;
		double a = Maths.distance(ParticleManagerEX.MC.player.posX, ParticleManagerEX.MC.player.posY, ParticleManagerEX.MC.player.posZ, pA.posX, pA.posY, pA.posZ);
		double b = Maths.distance(ParticleManagerEX.MC.player.posX, ParticleManagerEX.MC.player.posY, ParticleManagerEX.MC.player.posZ, pB.posX, pA.posY, pB.posZ);
		return a > b ? -1 : a == b ? 0 : 1;
	};
    private final List<Particle> layerA = new ArrayList<Particle>(10000);
	private final List<Particle> layerB = new ArrayList<Particle>(10000);
	private final List<Particle> layerC = new ArrayList<Particle>(10000);
	private final List<Particle> layerD = new ArrayList<Particle>(10000);
    private Map<Particle, InstancedMeshParticle> layerMesh = new HashMap<Particle, InstancedMeshParticle>();

    public ParticleManagerEX(World world, TextureManager renderer)
    {
        super(world, renderer);
        this.renderer = renderer;
    }

    private void render(Entity entity, Matrix4fe viewMatrix, Transformation transformation, List<Particle> particles, Map<Particle, InstancedMeshParticle> meshes, boolean useParticleShaders, float partialTicks)
	{
		float f = ActiveRenderInfo.getRotationX();
		float f1 = ActiveRenderInfo.getRotationZ();
		float f2 = ActiveRenderInfo.getRotationYZ();
		float f3 = ActiveRenderInfo.getRotationXY();
		float f4 = ActiveRenderInfo.getRotationXZ();
		
		if (useParticleShaders)
		{
            InstancedMeshParticle mesh;
			for (Particle particle : particles)
			{
				mesh = meshes.get(particle);
				if (mesh == null) continue;
				
				mesh.initRender();
				mesh.initRenderVBO1();

				//also resets position
				mesh.instanceDataBuffer.clear();
				mesh.curBufferPos = 0;
				if (particle instanceof EntityRotFX)
					((EntityRotFX) particle).renderParticleForShader(mesh, transformation, viewMatrix, entity, partialTicks, f, f4, f1, f2, f3);

				mesh.instanceDataBuffer.limit(mesh.curBufferPos * InstancedMeshParticle.INSTANCE_SIZE_FLOATS);
				OpenGlHelper.glBindBuffer(GL15.GL_ARRAY_BUFFER, mesh.instanceDataVBO);
				ShaderManager.glBufferData(GL15.GL_ARRAY_BUFFER, mesh.instanceDataBuffer, GL15.GL_DYNAMIC_DRAW);
				ShaderManager.glDrawElementsInstanced(GL11.GL_TRIANGLES, mesh.getVertexCount(), GL11.GL_UNSIGNED_INT, 0, mesh.curBufferPos);
				OpenGlHelper.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
				mesh.endRenderVBO1();
				mesh.endRender();
			}
		}
		else
		{
			
			BufferBuilder vertexbuffer = ParticleManagerEX.TESSELLATOR.getBuffer();
			vertexbuffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
			for (Particle particle : particles)
			{
				particle.renderParticle(vertexbuffer, entity, partialTicks, f, f4, f1, f2, f3);
				RotatingParticleManager.debugParticleRenderCount++;
			}
			ParticleManagerEX.TESSELLATOR.draw();
		}
        particles.clear();
	}

    /**
	 * Renders all current particles. Args player, partialTickTime
	 */
	@Override
	public void renderParticles(Entity entityIn, float partialTicks)
	{
		if (ConfigClient.enable_legacy_rendering)
		{
			super.renderParticles(entityIn, partialTicks);
			return;
		}
		
		boolean useParticleShaders = false;//RotatingParticleManager.useShaders && ConfigCoroUtil.particleShaders;
		Particle.interpPosX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double)partialTicks;
		Particle.interpPosY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double)partialTicks;
		Particle.interpPosZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double)partialTicks;
		Particle.cameraViewDir = entityIn.getLook(partialTicks);
		

		RotatingParticleManager.debugParticleRenderCount = 0;

		if (useParticleShaders)
		{
			MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.cloud256_test);
			MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.cloud256_fire);
			MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.cloud256);
			//foreground stuff
			MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.downfall3);
			MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.cloud256_6);
			MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.rain_white);
			MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.snow);
			MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.leaf);
			MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.debris_1);
			MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.debris_2);
			MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.debris_3);
			MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.tumbleweed);
		}

		Transformation transformation = null;
		Matrix4fe viewMatrix = null;


		int glCalls = 0;
		int trueRenderCount = 0;
		int particles = 0;

		if (useParticleShaders)
		{
			ShaderProgram shaderProgram = ShaderEngine.renderer.getShaderProgram("particle");
			transformation = ShaderEngine.renderer.transformation;
			shaderProgram.bind();
			Matrix4fe projectionMatrix = new Matrix4fe();
			FloatBuffer buf = BufferUtils.createFloatBuffer(16);
			GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, buf);
			buf.rewind();
			Matrix4fe.get(projectionMatrix, 0, buf);


			//testing determined i can save frames by baking projectionMatrix into modelViewMatrixCamera, might have to revert for more complex shaders
			//further testing its just barely faster, if at all...
			boolean alternateCameraCapture = true;
			if (alternateCameraCapture)
			{
				viewMatrix = new Matrix4fe();
				FloatBuffer buf2 = BufferUtils.createFloatBuffer(16);
				GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buf2);
				buf2.rewind();
				Matrix4fe.get(viewMatrix, 0, buf2);
			}

			Matrix4fe modelViewMatrix = projectionMatrix.mul(viewMatrix);
			shaderProgram.setUniformEfficient("modelViewMatrixCamera", modelViewMatrix, RotatingParticleManager.viewMatrixBuffer);
			shaderProgram.setUniform("texture_sampler", 0);
			int glFogMode = GL11.glGetInteger(GL11.GL_FOG_MODE);
			int modeIndex = glFogMode == GL11.GL_EXP2 ? 0 : glFogMode == GL11.GL_EXP ? 1 : 0;
			shaderProgram.setUniform("fogmode", modeIndex);
		}

		
		
		for (Map.Entry<TextureAtlasSprite, List<ArrayDeque<Particle>[][]>> entry1 : fxLayers.entrySet())
		{
			if (entry1.getKey() == null)
				continue;
			InstancedMeshParticle mesh = null;
			if (useParticleShaders)
			{
				mesh = MeshBufferManagerParticle.getMesh(entry1.getKey());
				if (mesh == null)
				{
					MeshBufferManagerParticle.setupMeshForParticle(entry1.getKey());
					mesh = MeshBufferManagerParticle.getMesh(entry1.getKey());
				}
			}

			if (mesh != null || !useParticleShaders)
				for (ArrayDeque<Particle>[][] entry : entry1.getValue())
					for(int i = 0; i < 3; i++)
						for (int j = 0; j < 2; j++)
							if (!entry[i][j].isEmpty())
							{
								for (final Particle particle : entry[i][j])
								{
									if (i != 1)
									{
										if (j == 1)
											layerA.add(particle);
										else
											layerC.add(particle);
									}
									else
									{
										if (j == 1)
											layerB.add(particle);
										else
											layerD.add(particle);
									}
									
									if (mesh != null)
										layerMesh.put(particle, mesh);
								}
							}
		}
		

		GlStateManager.pushMatrix();
		if (!layerA.isEmpty())
		{
			GlStateManager.depthMask(true);
			renderer.bindTexture(ParticleManagerEX.PARTICLE_TEXTURES);
			layerA.sort(COMPARE_DISTANCE);
			render(entityIn, viewMatrix, transformation, layerA, layerMesh, useParticleShaders, partialTicks);
		}
		if (!layerB.isEmpty())
		{
			GlStateManager.depthMask(true);
			renderer.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			layerB.sort(COMPARE_DISTANCE);
			render(entityIn, viewMatrix, transformation, layerB, layerMesh, useParticleShaders, partialTicks);
		}
		if (!layerC.isEmpty())
		{
			GlStateManager.depthMask(false);
			renderer.bindTexture(ParticleManagerEX.PARTICLE_TEXTURES);
			layerC.sort(COMPARE_DISTANCE);
			render(entityIn, viewMatrix, transformation, layerC, layerMesh, useParticleShaders, partialTicks);
		}
		if (!layerD.isEmpty())
		{
			GlStateManager.depthMask(false);
			renderer.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			layerD.sort(COMPARE_DISTANCE);
			render(entityIn, viewMatrix, transformation, layerD, layerMesh, useParticleShaders, partialTicks);
		}
		
		GlStateManager.popMatrix();
		
		if (useParticleShaders)
			ShaderEngine.renderer.getShaderProgram("particle").unbind();

		if (ConfigCoroUtil.debugShaders && world.getTotalWorldTime() % 60 == 0)
		{
			System.out.println("particles: " + particles);
			System.out.println("debugParticleRenderCount: " + RotatingParticleManager.debugParticleRenderCount);
			System.out.println("trueRenderCount: " + trueRenderCount);
			System.out.println("glCalls: " + glCalls);
		}
	}
}