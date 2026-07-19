package jammerbam.hbmgccompanion.compat.galacticraft;

import micdoodle8.mods.galacticraft.api.entity.IRocketType;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntityAutoRocket;
import micdoodle8.mods.galacticraft.core.GCFluids;
import micdoodle8.mods.galacticraft.core.GCItems;
import micdoodle8.mods.galacticraft.core.entities.EntityTier1Rocket;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

public final class GalacticraftRocketFactory {
    private GalacticraftRocketFactory() {
    }

    public static boolean isSupportedRocket(ItemStack stack) {
        return isTier1Rocket(stack);
    }

    public static EntityAutoRocket createRocket(World world, ItemStack stack, double x, double y, double z) {
        if (isTier1Rocket(stack)) {
            return createTier1Rocket(world, stack, x, y, z);
        }

        return null;
    }

    private static boolean isTier1Rocket(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem() == GCItems.rocketTier1;
    }

    private static EntityTier1Rocket createTier1Rocket(World world, ItemStack stack, double x, double y, double z) {
        IRocketType.EnumRocketType rocketType = getRocketType(stack);
        if (rocketType == null) {
            return null;
        }

        EntityTier1Rocket rocket = new EntityTier1Rocket(world, x, y, z, rocketType);
        loadFuelFromStack(stack, rocket);
        return rocket;
    }

    private static IRocketType.EnumRocketType getRocketType(ItemStack stack) {
        IRocketType.EnumRocketType[] values = IRocketType.EnumRocketType.values();
        int meta = stack.getMetadata();
        return meta >= 0 && meta < values.length ? values[meta] : null;
    }

    private static void loadFuelFromStack(ItemStack stack, EntityTier1Rocket rocket) {
        if (rocket.rocketType.getPreFueled()) {
            rocket.fuelTank.fill(new FluidStack(GCFluids.fluidFuel, rocket.getMaxFuel()), true);
        } else if (stack.hasTagCompound() && stack.getTagCompound().hasKey("RocketFuel")) {
            rocket.fuelTank.fill(new FluidStack(GCFluids.fluidFuel, stack.getTagCompound().getInteger("RocketFuel")), true);
        }
    }
}
