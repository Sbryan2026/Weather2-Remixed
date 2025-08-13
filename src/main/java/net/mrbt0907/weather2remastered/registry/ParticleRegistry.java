package net.mrbt0907.weather2remastered.registry;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.mrbt0907.weather2remastered.Weather2Remastered;
import net.mrbt0907.weather2remastered.particle.CloudParticleData;
@Mod.EventBusSubscriber(modid = Weather2Remastered.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ParticleRegistry {


    public static TextureAtlasSprite cloudSprite;

    public static ParticleType<CloudParticleData> CloudParticleType;

	public static TextureAtlasSprite chicken;
	public static TextureAtlasSprite cloud256_meso;
	public static TextureAtlasSprite cloud256_meso_wall;
	public static TextureAtlasSprite potato;
	public static TextureAtlasSprite debris_1,debris_2,debris_3;
	public static TextureAtlasSprite cloud256_light, tornado256, leaf, cloud256_fire,distant_downfall;
    @SubscribeEvent
    public static void onRegisterParticles(RegistryEvent.Register<ParticleType<?>> event) {
        CloudParticleType = new net.mrbt0907.weather2remastered.particle.CloudParticleType();
        CloudParticleType.setRegistryName("weather2remastered", "cloudparticle");
        event.getRegistry().register(CloudParticleType);
        System.out.println("REGISTERED PARTICLE " + CloudParticleType);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onTextureStitchPre(TextureStitchEvent.Pre event) {
        event.addSprite(new ResourceLocation("weather2remastered", "particles/cloud256"));
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onTextureStitchPost(TextureStitchEvent.Post event) {
        cloudSprite = event.getMap().getSprite(new ResourceLocation("weather2remastered", "particles/cloud256"));
    	chicken = cloudSprite;
    	cloud256_meso = cloudSprite;
    	cloud256_meso_wall = cloudSprite;
    	cloud256_fire = cloudSprite;
    	potato = cloudSprite;
    	debris_1 = cloudSprite;
    	debris_2 = cloudSprite;
    	debris_3 = cloudSprite;
    	cloud256_light = cloudSprite;
    	tornado256 = cloudSprite;
    	leaf = cloudSprite;
    	distant_downfall = cloudSprite;
    	
        if (cloudSprite == null) {
            Weather2Remastered.fatal("cloudSprite is null after stitching!");
        }
    }
}