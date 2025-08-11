package net.mrbt0907.weather2remastered.particle;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class CloudParticle extends SpriteTexturedParticle
{
  private TextureAtlasSprite sprite;
  private int ticksFadeInMax = 8;
  private int ticksFadeOutMax = 80;
  private int ticksLived = 0;
  private int eid = 0;
  public float avoidTerrainAngle = 0;
  public boolean facePlayer = true;
  private int count = 0;
  /**
   * Construct a new CloudParticle at the given [x,y,z] position, with the given initial velocity, color tint, diameter,
   * and a single TextureAtlasSprite.
   */
  public CloudParticle(ClientWorld world, double x, double y, double z,
                       double velocityX, double velocityY, double velocityZ,
                       Color tint, double diameter,
                       TextureAtlasSprite sprite)
  {
    super(world, x, y, z, velocityX, velocityY, velocityZ);
    if (sprite == null) sprite = net.mrbt0907.weather2remastered.registry.ParticleRegistry.cloudSprite;
    super.setSprite(sprite);
    this.eid = world.random.nextInt(100000);
    setColor(tint.getRed()/255.0F, tint.getGreen()/255.0F, tint.getBlue()/255.0F);
    setSize((float)diameter, (float)diameter);    // size of collision box
    System.out.println("Spawn success! @ " + x + " " + y + " " + z);
    final float PARTICLE_SCALE_FOR_ONE_METRE = 1.2F;
    quadSize = PARTICLE_SCALE_FOR_ONE_METRE * (float)diameter; // rendering size
    count++;
    lifetime = 4000;  // lifetime in ticks

    alpha = 0.0F;

    // Undo the vanilla random velocity variation applied in Particle constructor:
    xd = velocityX;
    yd = velocityY;
    zd = velocityZ;
  }

  @Override
  public void render(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks) {
      float x = (float)(MathHelper.lerp(partialTicks, this.xo, this.x) - renderInfo.getPosition().x);
      float y = (float)(MathHelper.lerp(partialTicks, this.yo, this.y) - renderInfo.getPosition().y);
      float z = (float)(MathHelper.lerp(partialTicks, this.zo, this.z) - renderInfo.getPosition().z);

      float halfSize = this.quadSize / 1.0F;
      int light = 240; // full light

      float minU = 0.0f;
      float maxU = 1.0f;
      float minV = 0.0f;
      float maxV = 1.0f;

      float r = 1.0F, g = 1.0F, b = 1.0F, a = this.alpha;

      // Upside-down quad: reverse winding order and flip V coords
      buffer.vertex(x - halfSize, y, z - halfSize).uv(minU, maxV).color(r, g, b, a).uv2(light).endVertex();
      buffer.vertex(x + halfSize, y, z - halfSize).uv(maxU, maxV).color(r, g, b, a).uv2(light).endVertex();
      buffer.vertex(x + halfSize, y, z + halfSize).uv(maxU, minV).color(r, g, b, a).uv2(light).endVertex();
      buffer.vertex(x - halfSize, y, z + halfSize).uv(minU, minV).color(r, g, b, a).uv2(light).endVertex();
  }
  @Override
  public void setSprite(TextureAtlasSprite sprite) {
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
  public IParticleRenderType getRenderType() {
      return new IParticleRenderType() {
          @Override
          public void begin(BufferBuilder buffer, TextureManager textureManager) {
              RenderSystem.depthMask(true);
              RenderSystem.enableBlend();
              RenderSystem.blendFuncSeparate(
                  GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA,
                  GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA
              );
              RenderSystem.enableDepthTest();
              RenderSystem.depthMask(false);
              textureManager.bind(new ResourceLocation("weather2remastered", "textures/particles/cloud256.png")); // bind your own texture
              buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE);
          }

          @Override
          public void end(Tessellator tessellator) {
              tessellator.end();
              RenderSystem.disablePolygonOffset();
          }
      };
  }
  public TextureAtlasSprite getSprite() {
	  return this.sprite;
  }
  @Override
  public void tick()
  {
	  //System.out.println("Im at "+ x  + "" + y + "" + z + " with texture " + net.mrbt0907.weather2remastered.registry.ParticleRegistry.cloudSprite.getName());

	  //super.tick();
	  ticksLived++;
	  // Fade in
	  if (ticksLived < ticksFadeInMax) {
	      this.alpha = (float) ticksLived / ticksFadeInMax;
	  }
	  // Fade out
	  else if (ticksLived > lifetime - ticksFadeOutMax) {
	      int ticksSinceFadeOutStarted = ticksLived - (lifetime - ticksFadeOutMax);
	      this.alpha = 1.0F - ((float) ticksSinceFadeOutStarted / ticksFadeOutMax);
	      if (alpha < 0) alpha = 0;
	  }
  }
  public void setTicksFadeInMax(int ticks) {
	    this.ticksFadeInMax = ticks;
	}
  public void setTicksFadeOutMax(int ticks) {
	    this.ticksFadeOutMax = ticks;
	}

	public double getX() {
		return x;
	}
	public double getZ() {
		return z;
	}

	public double getY() {
		return y;
	}

	public float getAlpha() {
		return alpha;
	}
	@Override
	public void setAlpha(float f) {
		this.alpha = f;
	}

	public float getScale() {
		return quadSize;
	}
	
	public double getMotionX() {
		return xd;
	}

	public double getMotionY() {
		return yd;
	}

	public double getMotionZ() {
		return zd;
	}

	public void setMotionZ(double d) {
		zd = d;
	}

	public void setMotionY(double d) {
		yd = d;
	}

	public void setMotionX(double d) {
		xd = d;
	}

	public void setRoll(float r) {
		roll = r;
	}

	public int getEntityId() {
		return eid;
	}

	 public void setGravity(float par) {
	    	gravity = par;
	    }

	public void setRotYaw(int i) {
		this.roll = (float) Math.toRadians(i);
	}
}