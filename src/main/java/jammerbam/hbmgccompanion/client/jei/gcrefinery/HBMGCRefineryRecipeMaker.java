package jammerbam.hbmgccompanion.client.jei.gcrefinery;

import jammerbam.hbmgccompanion.compat.HBMGCFluids;
import java.util.ArrayList;
import java.util.List;

import micdoodle8.mods.galacticraft.core.GCItems;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

/**
 * Fork-agnostic JEI recipe maker.
 *
 * Primary JEI input: Forge FluidStack "kerosene" (works on CE + EE if fluid is registered).
 */
public class HBMGCRefineryRecipeMaker {

    public static List<HBMGCRefineryRecipeWrapper> getRecipes() {
        List<HBMGCRefineryRecipeWrapper> recipes = new ArrayList<>();

        ItemStack gcFuelCan = new ItemStack(GCItems.fuelCanister, 1, 1);
        ItemStack gcFuelBucket = new ItemStack(GCItems.bucketFuel);

        Fluid kerosene = resolveKerosene();
        if (kerosene != null) {
            addFluid(recipes, new FluidStack(kerosene, 1000), gcFuelCan);
            addFluid(recipes, new FluidStack(kerosene, 1000), gcFuelBucket);
        }

        return recipes;
    }

    private static void addFluid(List<HBMGCRefineryRecipeWrapper> list, FluidStack in, ItemStack out) {
        if (list == null || in == null || in.getFluid() == null || out == null || out.isEmpty()) return;
        list.add(new HBMGCRefineryRecipeWrapper(in, out));
    }

    /**
     * Prefer Forge registry. Optional fallback to EE field via reflection.
     */
    private static Fluid resolveKerosene() {
        Fluid f = HBMGCFluids.getKerosene();
        if (f != null) return f;

        try {
            Class<?> c = Class.forName("com.hbm.forgefluid.ModForgeFluids");
            Object v = c.getField("kerosene").get(null);
            if (v instanceof Fluid) return (Fluid) v;
        } catch (Throwable ignored) {}

        return null;
    }
}
