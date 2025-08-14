package net.mrbt0907.weather2remastered.particle;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;
import net.mrbt0907.weather2remastered.util.Maths.Vec;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class CloudParticle extends SpriteTexturedParticle {
    private TextureAtlasSprite sprite;
    private int ticksFadeInMax = 8;
    private int ticksFadeOutMax = 80;
    private int ticksLived = 0;
    private int eid = 0;
    public float avoidTerrainAngle = 0;
    public boolean facePlayer = true;
    private int light = 240; // default fullbright
    private int tickCounter = 0;
    private BlockPos lastLightPos = null;
    private float pitch;
    private float yaw;
    public boolean rotateAroundLocation = false;
    public double rotationRadius = 0.0;   // radius of orbit
    public double rotationSpeed = 0.05;   // radians per tick (orbit speed)
    private double rotationAngle = 0.0;   // current orbit angle
    private double rotationCenterX, rotationCenterZ; // center of orbit

    /**
     * Construct a new CloudParticle at the given [x,y,z] position, with the given initial velocity, color tint, diameter,
     * and a single TextureAtlasSprite.
     */
    public CloudParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, Color tint, double diameter, TextureAtlasSprite sprite)
    {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        if (sprite == null) sprite = net.mrbt0907.weather2remastered.registry.ParticleRegistry.cloudSprite;
        super.setSprite(sprite);
        this.eid = world.random.nextInt(100000);
        if (rotateAroundLocation) {
            rotationCenterX = x;
            rotationCenterZ = z;
            rotationRadius = rotationRadius > 0 ? rotationRadius : 1.0;
        }
        setColor(tint.getRed() / 255.0F, tint.getGreen() / 255.0F, tint.getBlue() / 255.0F);
        setSize((float) diameter, (float) diameter); // size of collision box
        //    System.out.println("Spawn success! @ " + x + " " + y + " " + z);
        final float PARTICLE_SCALE_FOR_ONE_METRE = 2.5F;
        quadSize = PARTICLE_SCALE_FOR_ONE_METRE * (float) diameter; // rendering size
        lifetime = 4000; // lifetime in ticks

        alpha = 0.0F;

        // Undo the vanilla random velocity variation applied in Particle constructor:
        xd = velocityX;
        yd = velocityY;
        zd = velocityZ;
    }

    @Override
    public void render(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks) {
        // Interpolate position for smooth motion
        float interpX = (float)MathHelper.lerp(partialTicks, this.xo, this.x);
        float interpY = (float)MathHelper.lerp(partialTicks, this.yo, this.y);
        float interpZ = (float)MathHelper.lerp(partialTicks, this.zo, this.z);

        // Offset by camera
        float xPos = interpX - (float)renderInfo.getPosition().x;
        float yPos = interpY - (float)renderInfo.getPosition().y;
        float zPos = interpZ - (float)renderInfo.getPosition().z;

        // Distance-based alpha
        double dx = interpX - renderInfo.getPosition().x;
        double dy = interpY - renderInfo.getPosition().y;
        double dz = interpZ - renderInfo.getPosition().z;
        double distance = Math.sqrt(dx*dx + dy*dy + dz*dz);
        double maxDistance = Minecraft.getInstance().options.renderDistance * 16 * 32;
        float distanceAlpha = (float)MathHelper.clamp(1.0 - (distance / maxDistance), 0.0, 1.0);
        float a = this.alpha * distanceAlpha;

        // Quad size
        float halfSize = this.quadSize / 2f;
        float[] vx = new float[4];
        float[] vy = new float[4];
        float[] vz = new float[4];

        if (rotateAroundLocation) {
            // Vertical quad along Y, width along X
            float halfHeight = halfSize;
            vx[0] = -halfSize; vy[0] = -halfHeight; vz[0] = 0;
            vx[1] =  halfSize; vy[1] = -halfHeight; vz[1] = 0;
            vx[2] =  halfSize; vy[2] =  halfHeight;  vz[2] = 0;
            vx[3] = -halfSize; vy[3] = halfHeight;  vz[3] = 0;

            // Rotate quad to face tangent of orbit
            float yawRad = (float)Math.toRadians(this.yaw);
            float cosYaw = (float)Math.cos(yawRad);
            float sinYaw = (float)Math.sin(yawRad);

            for (int i = 0; i < 4; i++) {
                float xRot = vx[i] * cosYaw - vz[i] * sinYaw;
                float zRot = vx[i] * sinYaw + vz[i] * cosYaw;
                vx[i] = xRot + xPos;
                vy[i] += yPos;
                vz[i] = zRot + zPos;
            }
        } else {
            // Horizontal quad along XZ, flat on Y
            vx[0] = -halfSize; vy[0] = 0; vz[0] = -halfSize;
            vx[1] =  halfSize; vy[1] = 0; vz[1] = -halfSize;
            vx[2] =  halfSize; vy[2] = 0; vz[2] =  halfSize;
            vx[3] = -halfSize; vy[3] = 0; vz[3] =  halfSize;

            // For horizontal particles, no yaw rotation, just translate
            for (int i = 0; i < 4; i++) {
                vx[i] += xPos;
                vy[i] += yPos;
                vz[i] += zPos;
            }
        }

        // Draw quad
        buffer.vertex(vx[0], vy[0], vz[0]).uv(0,1).color(1f,1f,1f,a).uv2(light).endVertex();
        buffer.vertex(vx[1], vy[1], vz[1]).uv(1,1).color(1f,1f,1f,a).uv2(light).endVertex();
        buffer.vertex(vx[2], vy[2], vz[2]).uv(1,0).color(1f,1f,1f,a).uv2(light).endVertex();
        buffer.vertex(vx[3], vy[3], vz[3]).uv(0,0).color(1f,1f,1f,a).uv2(light).endVertex();
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (++this.ticksLived >= this.lifetime) {
            this.remove();
            return;
        }

        if (rotateAroundLocation && rotationRadius > 0) {
        	rotationSpeed=0.015;
        	rotationRadius=900;
        	this.quadSize = 600F;
        	this.y = 450;
        	this.facePlayer = true;
            if (this.rotationCenterZ > 100) rotationAngle -= rotationSpeed; else rotationAngle += rotationSpeed;

            this.x = rotationCenterX + rotationRadius * Math.cos(rotationAngle);
            this.z = rotationCenterZ + rotationRadius * Math.sin(rotationAngle);
            this.y += this.yd;

            float dx = (float)(this.x - rotationCenterX);
            float dz = (float)(this.z - rotationCenterZ);
            this.yaw = (float)Math.toDegrees(Math.atan2(dz, dx)) + 90f;

            if (rotationAngle > Math.PI * 2) rotationAngle -= Math.PI * 2;
        } else {
            this.x += this.xd;
            this.y += this.yd;
            this.z += this.zd;
        }

        this.yd -= 0.04D * this.gravity;

        if (ticksLived < ticksFadeInMax) {
            this.alpha = (float) ticksLived / ticksFadeInMax;
        } else if (ticksLived > lifetime - ticksFadeOutMax) {
            int ticksSinceFadeOut = ticksLived - (lifetime - ticksFadeOutMax);
            this.alpha = 1.0F - ((float) ticksSinceFadeOut / ticksFadeOutMax);
        }

        getLightForRender();
    }

    @Override
    public void setSprite(TextureAtlasSprite sprite)
    {
        super.setSprite(sprite);
    }
    @Override
    protected int getLightColor(float partialTick)
    {
        final int BLOCK_LIGHT = 15;
        final int SKY_LIGHT = 15;
        return LightTexture.pack(BLOCK_LIGHT, SKY_LIGHT);
    }

    @Override
    public IParticleRenderType getRenderType()
    {
        return new IParticleRenderType()
        {
            @Override
            public void begin(BufferBuilder buffer, TextureManager textureManager)
            {
            	
                RenderSystem.depthMask(true);
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(
                    GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA,
                    GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA
                );
                RenderSystem.enableDepthTest();
                RenderSystem.depthMask(false);
                textureManager.bind(new ResourceLocation("weather2remastered", "textures/particles/cloud256.png")); // bind your own texture
                RenderSystem.disableCull(); // add this before buffer.begin(...)
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE);
            }

            @Override
            public void end(Tessellator tessellator)
            {
                tessellator.end();
                RenderSystem.disablePolygonOffset();
            }
        };
    }
    public TextureAtlasSprite getSprite()
    {
        return this.sprite;
    }

    public void setTicksFadeInMax(int ticks)
    {
        this.ticksFadeInMax = ticks;
    }
    public void setTicksFadeOutMax(int ticks)
    {
        this.ticksFadeOutMax = ticks;
    }

    public double getX()
    {
        return this.x;
    }
    public double getZ()
    {
        return this.z;
    }

    public double getY()
    {
        return this.y;
    }

    public float getAlpha()
    {
        return alpha;
    }
    @Override
    public void setAlpha(float f)
    {
        alpha = f;
    }

    public float getScale()
    {
        return this.quadSize;
    }

    public double getMotionX()
    {
        return this.xd;
    }

    public double getMotionY()
    {
        return this.yd;
    }

    public double getMotionZ()
    {
        return this.zd;
    }

    public void setMotionZ(double d)
    {
    	this.zd = d;
    }

    public void setMotionY(double d)
    {
        this.yd = d;
    }

    public void setMotionX(double d)
    {
    	this.xd = d;
    }

    public void setRoll(float r)
    {
    	this.roll = r;
    }

    public int getEntityId()
    {
        return eid;
    }

    public void setGravity(float par)
    {
    	this.gravity = par;
    }

    public void setRotYaw(int i)
    {
    	this.yaw = i;
        //this.roll = (float) Math.toRadians(i);
    }
    public int getRotYaw()
    {
    	return (int) this.yaw;
    }
    public void setPitch(int i) {
    	this.pitch = i;
    }
    public void setRotationState(boolean state) {
    	this.rotateAroundLocation = state;
    }
    public void setRotation(Vec pos, int radius, double speed) {
    	this.rotationCenterX = pos.posX;
    	this.rotationCenterZ = pos.posZ;
    	this.rotationRadius = radius * 1.0F;
    	this.rotationSpeed = speed;
    	if (radius > 0)System.out.println("Setting rotation "+ radius);
    }
    private void getLightForRender()
    {
        tickCounter++;
        if (tickCounter % 5 == 0)
        {
            BlockPos pos = new BlockPos(this.x, this.y, this.z);
            if (lastLightPos == null || !pos.equals(lastLightPos))
            {
                lastLightPos = pos;
                int blockLight = level.getBrightness(LightType.BLOCK, pos);
                int skyLight = level.getBrightness(LightType.SKY, pos);
                light = (skyLight << 20) | (blockLight << 4);
            }
        }
    }
    @Override
    public void setLifetime(int ticks) {
    	this.lifetime = ticks;
    }
}