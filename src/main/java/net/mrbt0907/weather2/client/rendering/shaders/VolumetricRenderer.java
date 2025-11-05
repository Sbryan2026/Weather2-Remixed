package net.mrbt0907.weather2.client.rendering.shaders;

import java.nio.FloatBuffer;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import extendedrenderer.particle.entity.EntityRotFX;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.client.entity.particle.ExtendedEntityRotFX;
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
        if (VolumetricRenderer.shader != null && VolumetricRenderer.shader.valid) return;
        VolumetricRenderer.shader = new VolumetricsShader(VolumetricRenderer.VERTEX_SHADER_PATH, VolumetricRenderer.FRAGMENT_SHADER_PATH);
        if (!VolumetricRenderer.shader.valid)
        {
            VolumetricRenderer.shader = null;
            VolumetricRenderer.mesh = null;
            return;
        }
        
        VolumetricRenderer.mesh = new SimpleVolumetricMesh(10);
        //VolumetricRenderer.createParameterTexture();
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
        if (VolumetricRenderer.shader == null) return;

        particles.removeIf(particle -> !(particle instanceof EntityRotFX) || particle instanceof ExtendedEntityRotFX && !((ExtendedEntityRotFX)particle).isVolumetric());
        //VolumetricRenderer.updateParameterTexture(entity, particles, partialTicks);
        
        GlStateManager.pushMatrix();
        VolumetricRenderer.shader.startShader();
        //GL13.glActiveTexture(GL13.GL_TEXTURE0);
        //GL11.glBindTexture(GL11.GL_TEXTURE_2D, VolumetricRenderer.texture_id);
        //GL20.glUniform1i(VolumetricRenderer.shader.getParameter("particle_data"), 0);
        GL20.glUniform3f(VolumetricRenderer.shader.getParameter("camera"),(float) entity.posX, (float) entity.posY, (float) entity.posZ);
        GL20.glUniform1i(VolumetricRenderer.shader.getParameter("quality"), VolumetricRenderer.mesh.quality);
        //GL20.glUniform1i(VolumetricRenderer.shader.getParameter("height"), VolumetricRenderer.texture_height);
        //GL20.glUniform1i(VolumetricRenderer.shader.getParameter("width"), VolumetricRenderer.texture_width);
        
        VolumetricRenderer.mesh.bindVBO();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,GlStateManager.DestFactor.ZERO
);
        GlStateManager.depthMask(false);
        GlStateManager.enableLighting();
        int size = particles.size();
        for (int i = 0; i < size; i++)
        {
            EntityRotFX fx = (EntityRotFX) particles.get(i);
            int packed = fx.getBrightnessForRender(partialTicks);
            int blockLight = (packed >> 4) & 0xF;
            float brightness = Math.max(blockLight / 15.0f, fx.world.getSunBrightnessBody(partialTicks));
            
            //float corrected_brightness = 1.0F - (float)Math.pow(1.0F - brightness, Minecraft.getMinecraft().gameSettings.gammaSetting);
            GL20.glUniform3f(VolumetricRenderer.shader.getParameter("particle_pos"), (float) fx.posX, (float) fx.posY, (float) fx.posZ);
            GL20.glUniform1f(VolumetricRenderer.shader.getParameter("particle_height"), fx.particleScale * 0.04F);
            GL20.glUniform1f(VolumetricRenderer.shader.getParameter("particle_width"), fx.particleScale * 0.04F);
            GL20.glUniform2f(VolumetricRenderer.shader.getParameter("particle_rotation"), fx.rotationYaw, fx.rotationPitch);
            GL20.glUniform4f(VolumetricRenderer.shader.getParameter("color"), fx.getRedColorF(), fx.getGreenColorF(), fx.getBlueColorF(), fx.getAlphaF());
            GL20.glUniform1f(VolumetricRenderer.shader.getParameter("brightness"), brightness);
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, VolumetricRenderer.mesh.length);
        }
        GlStateManager.disableLighting();
        GlStateManager.depthMask(true);
        VolumetricRenderer.mesh.unbindVBO();
        //GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        VolumetricRenderer.shader.stopShader();
        GlStateManager.popMatrix();
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
            ix = particle.posX - entity.posX;
            iy = particle.posY - entity.posY;
            iz = particle.posZ - entity.posZ;
            buffer.put((float) ix).put((float) iy).put((float) iz);
            buffer.put(particle.height).put(particle.width);
            buffer.put(fx.particleRed).put(fx.particleGreen).put(fx.particleBlue).put(fx.particleAlpha);
            buffer.put(1).put(1.0F).put(1.0F);
        }
        buffer.flip();

        // Update texture with new parameters
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, VolumetricRenderer.texture_id);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA16, VolumetricRenderer.texture_width, VolumetricRenderer.texture_height, 0, GL11.GL_RGBA, GL11.GL_FLOAT, buffer);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }
}