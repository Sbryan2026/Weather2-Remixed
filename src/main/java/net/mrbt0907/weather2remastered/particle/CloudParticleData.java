package net.mrbt0907.weather2remastered.particle;

import java.awt.Color;
import java.util.Locale;

import javax.annotation.Nonnull;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.math.MathHelper;
import net.mrbt0907.weather2remastered.registry.ParticleRegistry;

public class CloudParticleData implements IParticleData {

	  public CloudParticleData(Color tint, double diameter) {
	    this.tint = tint;
	    this.diameter = constrainDiameterToValidRange(diameter);
	  }

	  public Color getTint() {
	    return tint;
	  }

	  /**
	   * @return get diameter of particle in metres
	   */
	  public double getDiameter() {
	    return diameter;
	  }

	  @Nonnull
	  @Override
	  public ParticleType<CloudParticleData> getType() {
	    return ParticleRegistry.CloudParticleType;
	  }

	  // write the particle information to a PacketBuffer, ready for transmission to a client
	  @Override
	  public void writeToNetwork(PacketBuffer buf) {
	    buf.writeInt(tint.getRed());
	    buf.writeInt(tint.getGreen());
	    buf.writeInt(tint.getBlue());
	    buf.writeDouble(diameter);
	  }

	  private static double constrainDiameterToValidRange(double diameter) {
	    final double MIN_DIAMETER = 0.05;
	    final double MAX_DIAMETER = 1.0;
	    return MathHelper.clamp(diameter, MIN_DIAMETER, MAX_DIAMETER);
	  }

	  private Color tint;
	  private double diameter;
	  public static final Codec<CloudParticleData> CODEC = RecordCodecBuilder.create(
	            instance -> instance.group(
	              Codec.INT.fieldOf("tint").forGetter(d -> d.tint.getRGB()),
	              Codec.DOUBLE.fieldOf("diameter").forGetter(d -> d.diameter)
	            ).apply(instance, CloudParticleData::new)
	          );

	  private CloudParticleData(int tintRGB, double diameter) {
	    this.tint = new Color(tintRGB);
	    this.diameter = constrainDiameterToValidRange(diameter);
	  }
	  public static final IDeserializer<CloudParticleData> DESERIALIZER = new IDeserializer<CloudParticleData>() {
	    @Override
	    public CloudParticleData fromCommand(@Nonnull ParticleType<CloudParticleData> type, StringReader reader) throws CommandSyntaxException {
		      reader.expect(' ');
		      double diameter = constrainDiameterToValidRange(reader.readDouble());

		      final int MIN_COLOUR = 0;
		      final int MAX_COLOUR = 255;
		      reader.expect(' ');
		      int red = MathHelper.clamp(reader.readInt(), MIN_COLOUR, MAX_COLOUR);
		      reader.expect(' ');
		      int green = MathHelper.clamp(reader.readInt(), MIN_COLOUR, MAX_COLOUR);
		      reader.expect(' ');
		      int blue = MathHelper.clamp(reader.readInt(), MIN_COLOUR, MAX_COLOUR);
		      Color color = new Color(red, green, blue);

		      return new CloudParticleData(color, diameter);
	    }
	    // read the particle information from a PacketBuffer after the client has received it from the server
	    @Override
	    public CloudParticleData fromNetwork(@Nonnull ParticleType<CloudParticleData> type, PacketBuffer buf) {
	      // warning! never trust the data read in from a packet buffer.

	      final int MIN_COLOUR = 0;
	      final int MAX_COLOUR = 255;
	      int red = MathHelper.clamp(buf.readInt(), MIN_COLOUR, MAX_COLOUR);
	      int green = MathHelper.clamp(buf.readInt(), MIN_COLOUR, MAX_COLOUR);
	      int blue = MathHelper.clamp(buf.readInt(), MIN_COLOUR, MAX_COLOUR);
	      Color color = new Color(red, green, blue);

	      double diameter = constrainDiameterToValidRange(buf.readDouble());

	      return new CloudParticleData(color, diameter);
	    }
	  };

	@Override
	public String writeToString() {
	    return String.format(Locale.ROOT, "%s %f %d %d %d",
	            getType().getRegistryName(), // or your modid:cloud_particle string
	            diameter,
	            tint.getRed(),
	            tint.getGreen(),
	            tint.getBlue()
	        );
	}
	}
