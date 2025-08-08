package net.mrbt0907.weather2remastered.util.coro;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;
import net.mrbt0907.weather2remastered.Weather2Remastered;

import java.lang.reflect.Method;

public class CoroCompat {

    private static boolean tanInstalled = false;
    private static boolean checkTAN = true;

    private static boolean sereneSeasonsInstalled = false;
    private static boolean checksereneSeasons = true;

    @SuppressWarnings("rawtypes")
	private static Class class_TAN_ASMHelper = null;
    private static Method method_TAN_getFloatTemperature = null;

    @SuppressWarnings("rawtypes")
	private static Class class_SereneSeasons_ASMHelper = null;
    private static Method method_sereneSeasons_getFloatTemperature = null;

    public static boolean shouldSnowAt(World world, BlockPos pos) {
        /**
         * ISeasonData data = SeasonHelper.getSeasonData(world);
         * boolean canSnow = SeasonASMHelper.canSnowAtInSeason(world, pos, false, data.getSeason());
         *
         * or:
         *
         * float temp = SeasonASMHelper.getFloatTemperature(world, pos);
         * boolean canSnow = temp <= 0;
         */
        return false;
    }

    @SuppressWarnings("unchecked")
	public static float getAdjustedTemperature(World world, Biome biome, BlockPos pos) {

        //TODO: consider caching results in a blockpos,float hashmap for a second or 2
        if (isTANInstalled()) {
            try {
                if (method_TAN_getFloatTemperature == null) {
                    method_TAN_getFloatTemperature = class_TAN_ASMHelper.getDeclaredMethod("getTemperature", Biome.class, BlockPos.class);
                }
                return (float) method_TAN_getFloatTemperature.invoke(null, biome, pos);
            } catch (Exception ex) {
                ex.printStackTrace();
                //prevent error spam
                tanInstalled = false;
                return biome.getTemperature(pos);
            }
        } else if (isSereneSeasonsInstalled()) {
            try {
                if (method_sereneSeasons_getFloatTemperature == null) {
                    method_sereneSeasons_getFloatTemperature = class_SereneSeasons_ASMHelper.getDeclaredMethod("getTemperature", World.class, Biome.class, BlockPos.class);
                }
                return (float) method_sereneSeasons_getFloatTemperature.invoke(null, world, biome, pos);
            } catch (Exception ex) {
                ex.printStackTrace();
                //prevent error spam
                sereneSeasonsInstalled = false;
                return biome.getTemperature(pos);
            }
        } else {
            return biome.getTemperature(pos);
        }
    }

    /**
     * Check if tough as nails is installed
     *
     * @return
     */
    public static boolean isTANInstalled() {
        if (checkTAN) {
            try {
                checkTAN = false;
                class_TAN_ASMHelper = Class.forName("toughasnails.season.SeasonASMHelper");
                if (class_TAN_ASMHelper != null) {
                    tanInstalled = true;
                }
            } catch (Exception ex) {
                //not installed
                //ex.printStackTrace();
            }

            Weather2Remastered.info("CoroCompat detected Tough As Nails Seasons " + (tanInstalled ? "Installed" : "Not Installed") + " for use");
        }

        return tanInstalled;
    }

    /**
     * Check if Serene Seasons is installed
     *
     * @return
     */
    public static boolean isSereneSeasonsInstalled() {
        if (checksereneSeasons) {
            try {
                checksereneSeasons = false;
                class_SereneSeasons_ASMHelper = Class.forName("sereneseasons.api.season.BiomeHooks");
                if (class_SereneSeasons_ASMHelper != null) {
                    sereneSeasonsInstalled = true;
                }
            } catch (Exception ex) {
                //not installed
                //ex.printStackTrace();
            }

            Weather2Remastered.info("CoroCompat detected Serene Seasons " + (sereneSeasonsInstalled ? "Installed" : "Not Installed") + " for use");
        }

        return sereneSeasonsInstalled;
    }

    public static boolean canTornadoGrabBlockRefinedRules(BlockState state) {
        ResourceLocation registeredName = state.getBlock().getRegistryName();
        if (registeredName.toString().toLowerCase().contains("dynamictrees")) {
            if (registeredName.toString().toLowerCase().contains("rooty") || registeredName.toString().toLowerCase().contains("branch")) {
                return false;
            }
        }
        return true;
    }
    /*public static Block setUnlocalizedNameAndTexture(Block block, String nameTex) {
	block.setBlockName(nameTex);
	//block.setTextureName(nameTex);
	return block;
	}*/
	
	public static boolean isAir(Block parBlock) {
		Material mat = parBlock.defaultBlockState().getMaterial();
		if (mat == Material.AIR) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isEqual(Block parBlock, Block parBlock2) {
		return parBlock == parBlock2;
	}
	
	public static boolean isEqualMaterial(Block parBlock, Material parMaterial) {
		return parBlock.defaultBlockState().getMaterial() == parMaterial;
	}
	
	public static Block getBlockByName(String name) {
		try {
			return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(name));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	/*public static String getNameByItem(Item item) {
		return Block.blockRegistry.getNameForObject(item);
	}*/
	
	public static String getNameByBlock(Block item) {
		ResourceLocation name = ForgeRegistries.BLOCKS.getKey(item);
	    return name != null ? name.toString() : "unknown";
	}

}