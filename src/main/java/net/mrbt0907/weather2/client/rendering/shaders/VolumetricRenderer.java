package net.mrbt0907.weather2.client.rendering.shaders;

import java.nio.FloatBuffer;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import extendedrenderer.particle.entity.EntityRotFX;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.client.rendering.shaders.mesh.SimpleVolumetricMesh;

public class VolumetricRenderer
{
    public static final String FRAGMENT_SHADER_PATH = "/assets/" + Weather2.OLD_MODID + "/shaders/program/volumetric_clouds.fsh";
    public static final String VERTEX_SHADER_PATH = "/assets/" + Weather2.OLD_MODID + "/shaders/program/volumetric_clouds.vsh";
    public static VolumetricsShader shader;
    public static SimpleVolumetricMesh mesh;
    public static int texture_id;
    public static final int texture_width = 3;
    public static int texture_height;

    public static void startShader()
    {
        VolumetricRenderer.shader = new VolumetricsShader(VolumetricRenderer.VERTEX_SHADER_PATH, VolumetricRenderer.FRAGMENT_SHADER_PATH);
        if (!VolumetricRenderer.shader.valid)
        {
            VolumetricRenderer.shader = null;
            VolumetricRenderer.mesh = null;
            return;
        }
        VolumetricRenderer.mesh = new SimpleVolumetricMesh(20);
        VolumetricRenderer.createParameterTexture();
    }

    public static void stopShader()
    {
        if (VolumetricRenderer.shader != null)
        {
            VolumetricRenderer.shader.deleteShader();
            VolumetricRenderer.shader = null;
        }
        if (VolumetricRenderer.mesh != null)
        {
            VolumetricRenderer.mesh.delete();
            VolumetricRenderer.mesh = null;
        }
    }

    public static void render(Entity entity, List<Particle> particles, float partialTicks)
    {
        if (VolumetricRenderer.shader == null || VolumetricRenderer.mesh == null) return;
        particles.removeIf(particle -> !(particle instanceof EntityRotFX));
        VolumetricRenderer.updateParameterTexture(entity, particles, partialTicks);

        VolumetricRenderer.shader.startShader();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, VolumetricRenderer.texture_id);
        GL20.glUniform1i(VolumetricRenderer.shader.getParameter("particle_data"), 0);
        GL20.glUniform3f(VolumetricRenderer.shader.getParameter("camera"),(float) entity.posX, (float) entity.posY, (float) entity.posZ);
        GL20.glUniform1i(VolumetricRenderer.shader.getParameter("quality"), VolumetricRenderer.mesh.length);
        GL20.glUniform1i(VolumetricRenderer.shader.getParameter("height"), VolumetricRenderer.texture_height);
        GL20.glUniform1i(VolumetricRenderer.shader.getParameter("width"), VolumetricRenderer.texture_width);
        VolumetricRenderer.mesh.bindVBO();
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, VolumetricRenderer.mesh.length);
        VolumetricRenderer.mesh.unbindVBO();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        VolumetricRenderer.shader.stopShader();
    }

    public static void createParameterTexture()
    {
        VolumetricRenderer.texture_height = 1000;

        // Generate texture to hold all the parameters
        VolumetricRenderer.texture_id = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, VolumetricRenderer.texture_id);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, VolumetricRenderer.texture_width, VolumetricRenderer.texture_height, 0, GL11.GL_RGBA, GL11.GL_FLOAT, (FloatBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    public static void updateParameterTexture(Entity entity, List<Particle> particles, float partialTicks)
    {
        // Gather all parameters needed to render a particle within the shader
        VolumetricRenderer.texture_height = particles.size();
        
        FloatBuffer buffer = BufferUtils.createFloatBuffer(VolumetricRenderer.texture_width * VolumetricRenderer.texture_height * 4);
        EntityRotFX fx;
        double ix, iy, iz;
        for (Particle particle : particles)
        {
            fx = (EntityRotFX) particle;
            ix = particle.prevPosX + (particle.posX - particle.prevPosX) * partialTicks;
            iy = particle.prevPosY + (particle.posY - particle.prevPosY) * partialTicks;
            iz = particle.prevPosZ + (particle.posZ - particle.prevPosZ) * partialTicks;
            buffer.put((float) (ix - entity.posX)).put((float) (iy - entity.posY)).put((float) (iz - entity.posZ));
            buffer.put(particle.height).put(particle.width);
            buffer.put(fx.particleRed).put(fx.particleGreen).put(fx.particleBlue).put(fx.particleAlpha);
            buffer.put(fx.getBrightnessForRender(partialTicks)).put(0.0F).put(0.0F);
        }
        buffer.flip();

        // Update texture with new parameters
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, VolumetricRenderer.texture_id);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, VolumetricRenderer.texture_width, VolumetricRenderer.texture_height, 0, GL11.GL_RGBA, GL11.GL_FLOAT, buffer);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }
}