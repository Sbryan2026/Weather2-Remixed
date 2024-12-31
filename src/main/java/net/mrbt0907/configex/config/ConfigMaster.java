package net.mrbt0907.configex.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig.Type;

public final class ConfigMaster {
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;
	public static final ForgeConfigSpec.ConfigValue<Boolean> debug_mode;
	private static final String CFG_NAME = "ConfigEX - Master";
	static  {
		BUILDER.push(CFG_NAME);
		debug_mode = BUILDER.comment("Enable debug mode? Default value is \'false\'.").define("Enable Debug", false);
		BUILDER.pop();
		SPEC = BUILDER.build();
	}
	public String getName() {
		return CFG_NAME;
	}
	public String getDescription()
	{
		return "This is the master config file for the config mod.";
	}
	public static void preInit() {
		ModLoadingContext.get().registerConfig(Type.COMMON, ConfigMaster.SPEC, "configex/Master.toml");		
	}
}
