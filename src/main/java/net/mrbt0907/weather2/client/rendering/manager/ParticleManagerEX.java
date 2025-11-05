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
import net.mrbt0907.weather2.client.rendering.shaders.VolumetricRenderer;
import net.mrbt0907.weather2.config.ConfigClient;
import net.mrbt0907.weather2.util.Maths;

public class ParticleManagerEX extends RotatingParticleManager
{
    protected static final Minecraft MC = Minecraft.getMinecraft();
    protected static final Tessellator TESSELLATOR = Tessellator.getInstance();
    protected static final ResourceLocation PARTICLE_TEXTURES = new ResourceLocation("textures/particle/particles.png");
    protected final TextureManager renderer;
	private FloatBuffer projectionBuffer;
	private FloatBuffer modelViewBuffer;
	private final Map<Particle, Double> distanceCache = new HashMap<>(10000);
	public final Comparator<? super Particle> COMPARE_DISTANCE = (a, b) -> Double.compare(distanceCache.get(b), distanceCache.get(a));
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

	    // Precompute player interpolation
	    Particle.interpPosX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * partialTicks;
	    Particle.interpPosY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * partialTicks;
	    Particle.interpPosZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * partialTicks;
	    Particle.cameraViewDir = entityIn.getLook(partialTicks);
	    RotatingParticleManager.debugParticleRenderCount = 0;

	    boolean useParticleShaders = false; // RotatingParticleManager.useShaders && ConfigCoroUtil.particleShaders;
	    // Initialize shader context (reused buffers)
	    Matrix4fe viewMatrix = null;
	    Transformation transformation = null;
	    ShaderProgram shaderProgram = null;

	    if (useParticleShaders) {
	        shaderProgram = ShaderEngine.renderer.getShaderProgram("particle");
	        transformation = ShaderEngine.renderer.transformation;
	        shaderProgram.bind();

	        if (projectionBuffer == null) {
	            projectionBuffer = BufferUtils.createFloatBuffer(16);
	            modelViewBuffer = BufferUtils.createFloatBuffer(16);
	        }

	        projectionBuffer.clear();
	        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projectionBuffer);
	        projectionBuffer.rewind();

	        Matrix4fe projectionMatrix = new Matrix4fe();
	        Matrix4fe.get(projectionMatrix, 0, projectionBuffer);

	        viewMatrix = new Matrix4fe();
	        modelViewBuffer.clear();
	        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelViewBuffer);
	        modelViewBuffer.rewind();
	        Matrix4fe.get(viewMatrix, 0, modelViewBuffer);

	        Matrix4fe modelViewMatrix = projectionMatrix.mul(viewMatrix);
	        shaderProgram.setUniformEfficient("modelViewMatrixCamera", modelViewMatrix, RotatingParticleManager.viewMatrixBuffer);
	        shaderProgram.setUniform("texture_sampler", 0);
	        int glFogMode = GL11.glGetInteger(GL11.GL_FOG_MODE);
	        int modeIndex = (glFogMode == GL11.GL_EXP2) ? 0 : (glFogMode == GL11.GL_EXP ? 1 : 0);
	        shaderProgram.setUniform("fogmode", modeIndex);
	    }

	    // Gather and classify particles
	    layerA.clear();
	    layerB.clear();
	    layerC.clear();
	    layerD.clear();
	    layerMesh.clear();

	    for (Map.Entry<TextureAtlasSprite, List<ArrayDeque<Particle>[][]>> entry1 : fxLayers.entrySet()) {
	        TextureAtlasSprite key = entry1.getKey();
	        if (key == null) continue;
	
	        InstancedMeshParticle mesh = useParticleShaders ? MeshBufferManagerParticle.getMesh(key) : null;
	        if (useParticleShaders && mesh == null) {
	            MeshBufferManagerParticle.setupMeshForParticle(key);
	            mesh = MeshBufferManagerParticle.getMesh(key);
	        }

	        for (ArrayDeque<Particle>[][] entry : entry1.getValue()) {
	            for (int i = 0; i < 3; i++) {
	                for (int j = 0; j < 2; j++) {
	                    if (entry[i][j].isEmpty()) continue;
	                    for (Particle particle : entry[i][j]) {
	                        if (i != 1) {
	                            if (j == 1) layerA.add(particle);
	                            else layerC.add(particle);
	                        } else {
	                            if (j == 1) layerB.add(particle);
	                            else layerD.add(particle);
	                        }
	                        if (mesh != null) layerMesh.put(particle, mesh);
	                    }
	                }
	            }
	        }
	    }

	    // Compute distance cache for all layers
	    computeDistanceCache(entityIn, layerA, layerB, layerC, layerD);

	    // Render all layers efficiently
	    GlStateManager.pushMatrix();
	    renderLayer(entityIn, partialTicks, viewMatrix, transformation, layerA, ParticleManagerEX.PARTICLE_TEXTURES, true, useParticleShaders);
	    renderLayer(entityIn, partialTicks, viewMatrix, transformation, layerB, TextureMap.LOCATION_BLOCKS_TEXTURE, true, useParticleShaders);
	    renderLayer(entityIn, partialTicks, viewMatrix, transformation, layerC, ParticleManagerEX.PARTICLE_TEXTURES, false, useParticleShaders);
	    renderLayer(entityIn, partialTicks, viewMatrix, transformation, layerD, TextureMap.LOCATION_BLOCKS_TEXTURE, false, useParticleShaders);
	    GlStateManager.popMatrix();

	    if (useParticleShaders && shaderProgram != null)
	        shaderProgram.unbind();
	}

	

	private void computeDistanceCache(Entity player, List<Particle>... layers) {
	    distanceCache.clear();
	    double px = player.posX, py = player.posY, pz = player.posZ;
	    for (List<Particle> list : layers) {
	        for (Particle p : list) {
	            distanceCache.put(p, Maths.distance(px, py, pz, p.posX, p.posY, p.posZ));
	        }
	    }
	}

	private void renderLayer(Entity entity, float partialTicks, Matrix4fe viewMatrix, Transformation transformation, List<Particle> particles, ResourceLocation texture, boolean depthMask, boolean useParticleShaders) {
	    if (particles.isEmpty()) return;

	    GlStateManager.depthMask(depthMask);
	    renderer.bindTexture(texture);

	    // Sort using cached distances (descending)
	    particles.sort(COMPARE_DISTANCE);

		if (ConfigClient.enable_volumetrics)
			VolumetricRenderer.render(entity, new ArrayList<>(particles), partialTicks);
			
	    if (useParticleShaders)
		{
	        // Group particles by mesh to minimize GL calls
	        Map<InstancedMeshParticle, List<Particle>> grouped = new HashMap<>();
	        for (Particle p : particles) {
	            InstancedMeshParticle mesh = layerMesh.get(p);
	            if (mesh == null) continue;
	            grouped.computeIfAbsent(mesh, k -> new ArrayList<>()).add(p);
	        }

	        for (Map.Entry<InstancedMeshParticle, List<Particle>> e : grouped.entrySet()) {
	            InstancedMeshParticle mesh = e.getKey();
	            mesh.initRender();
	            mesh.initRenderVBO1();
	            mesh.instanceDataBuffer.clear();
	            mesh.curBufferPos = 0;

	            for (Particle p : e.getValue()) {
	                if (p instanceof EntityRotFX)
	                    ((EntityRotFX) p).renderParticleForShader(mesh, transformation, viewMatrix, entity, partialTicks, ActiveRenderInfo.getRotationX(), ActiveRenderInfo.getRotationXZ(), ActiveRenderInfo.getRotationZ(), ActiveRenderInfo.getRotationYZ(), ActiveRenderInfo.getRotationXY());
	            }

	            mesh.instanceDataBuffer.limit(mesh.curBufferPos * InstancedMeshParticle.INSTANCE_SIZE_FLOATS);
	            OpenGlHelper.glBindBuffer(GL15.GL_ARRAY_BUFFER, mesh.instanceDataVBO);
	            ShaderManager.glBufferData(GL15.GL_ARRAY_BUFFER, mesh.instanceDataBuffer, GL15.GL_DYNAMIC_DRAW);
	            ShaderManager.glDrawElementsInstanced(GL11.GL_TRIANGLES, mesh.getVertexCount(), GL11.GL_UNSIGNED_INT, 0, mesh.curBufferPos);
	            OpenGlHelper.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	            mesh.endRenderVBO1();
	            mesh.endRender();
	        }
	    } else {
	        BufferBuilder vertexbuffer = ParticleManagerEX.TESSELLATOR.getBuffer();
	        vertexbuffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
	        for (Particle p : particles)
			{
	            p.renderParticle(vertexbuffer, entity, partialTicks, ActiveRenderInfo.getRotationX(), ActiveRenderInfo.getRotationXZ(), ActiveRenderInfo.getRotationZ(), ActiveRenderInfo.getRotationYZ(), ActiveRenderInfo.getRotationXY());
	            RotatingParticleManager.debugParticleRenderCount++;
	        }
	        ParticleManagerEX.TESSELLATOR.draw();
	    }

	    particles.clear();
	}
}