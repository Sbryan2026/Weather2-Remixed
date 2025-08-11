package net.mrbt0907.weather2remastered.particle;

import com.mojang.serialization.Codec;

import net.minecraft.particles.ParticleType;

public class CloudParticleType extends ParticleType<CloudParticleData> {
	  private static boolean ALWAYS_SHOW_REGARDLESS_OF_DISTANCE_FROM_PLAYER = false;
	  public CloudParticleType() {
	    super(ALWAYS_SHOW_REGARDLESS_OF_DISTANCE_FROM_PLAYER, CloudParticleData.DESERIALIZER);
	  }

	  @Override
	  public Codec<CloudParticleData> codec() {
	    return CloudParticleData.CODEC;
	  }
}