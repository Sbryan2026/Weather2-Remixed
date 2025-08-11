package net.mrbt0907.weather2remastered.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class CoroRotatingParticleManager {

    private final Minecraft mc = Minecraft.getInstance();

    private final List<CoroRotatingParticle> particles = new ArrayList<>();
    private final static ResourceLocation particleTexture = new ResourceLocation("weather2remastered", "textures/particles/particle.png");

    public void update() {
        for (int i = 0; i < particles.size(); i++) {
            CoroRotatingParticle particle = particles.get(i);
            particle.update();
            if (particle.isDead()) {
                particles.remove(i);
                i--;
            }
        }
    }

    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, float partialTicks) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        mc.getTextureManager().bind(particleTexture);

        TextureAtlasSprite sprite = mc.getTextureAtlas(particleTexture).apply(particleTexture);//(AtlasTexture.LOCATION_PARTICLES_TEXTURE).apply(particleTexture);

        for (CoroRotatingParticle particle : particles) {
            if (!particle.isDead()) {
                particle.render(matrixStack, buffer, partialTicks, sprite);
            }
        }

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    public void addParticle(CoroRotatingParticle particle) {
        particles.add(particle);
    }

    public static class CoroRotatingParticle {

        private double posX, posY, posZ;
        private double prevPosX, prevPosY, prevPosZ;
        private float rotation; // degrees
        private float rotationSpeed; // degrees per tick
        private int lifetime;
        private int age;
        private float size;

        public CoroRotatingParticle(double x, double y, double z, float rotationSpeed, int lifetime, float size) {
            this.posX = x;
            this.posY = y;
            this.posZ = z;
            this.prevPosX = x;
            this.prevPosY = y;
            this.prevPosZ = z;
            this.rotationSpeed = rotationSpeed;
            this.lifetime = lifetime;
            this.age = 0;
            this.size = size;
            this.rotation = 0f;
        }

        public void update() {
            prevPosX = posX;
            prevPosY = posY;
            prevPosZ = posZ;

            rotation += rotationSpeed;
            age++;
        }

        public boolean isDead() {
            return age >= lifetime;
        }

        public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, float partialTicks, TextureAtlasSprite sprite) {
            float interpX = (float)(prevPosX + (posX - prevPosX) * partialTicks);
            float interpY = (float)(prevPosY + (posY - prevPosY) * partialTicks);
            float interpZ = (float)(prevPosZ + (posZ - prevPosZ) * partialTicks);

            matrixStack.pushPose();

            matrixStack.translate(interpX, interpY, interpZ);
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(rotation));

            IVertexBuilder builder = buffer.getBuffer(RenderType.entityCutoutNoCull(particleTexture));

            float minU = sprite.getU0();
            float maxU = sprite.getU1();
            float minV = sprite.getV0();
            float maxV = sprite.getV1();

            float halfSize = size / 2f;

            Matrix4f matrix = matrixStack.last().pose();

            // Render a quad for the particle
            builder.vertex(matrix, -halfSize, 0, -halfSize).color(255, 255, 255, 255).uv(minU, maxV).overlayCoords(0, 0).uv2(15728880).normal(0, 1, 0).endVertex();
            builder.vertex(matrix, halfSize, 0, -halfSize).color(255, 255, 255, 255).uv(maxU, maxV).overlayCoords(0, 0).uv2(15728880).normal(0, 1, 0).endVertex();
            builder.vertex(matrix, halfSize, 0, halfSize).color(255, 255, 255, 255).uv(maxU, minV).overlayCoords(0, 0).uv2(15728880).normal(0, 1, 0).endVertex();
            builder.vertex(matrix, -halfSize, 0, halfSize).color(255, 255, 255, 255).uv(minU, minV).overlayCoords(0, 0).uv2(15728880).normal(0, 1, 0).endVertex();

            matrixStack.popPose();
        }
    }
}
