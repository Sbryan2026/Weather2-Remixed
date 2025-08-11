package net.mrbt0907.weather2remastered.particle;

import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;

public class CloudParticleFactory implements IParticleFactory<CloudParticleData> {

    private final TextureAtlasSprite sprite;

    public CloudParticleFactory(TextureAtlasSprite sprite) {
        this.sprite = sprite;
    }
    @Override
    public Particle createParticle(CloudParticleData data, ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        return new CloudParticle(world, x, y, z, xSpeed, ySpeed, zSpeed, data.getTint(), data.getDiameter(), sprite);
    }
}
