package jammerbam.hbmgccompanion.client.jei.gcrefinery;

import java.util.ArrayList;
import java.util.List;

import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.items.machine.ItemFluidTank;
import com.hbm.items.tool.ItemFluidCanister;

import micdoodle8.mods.galacticraft.core.GCItems; // keep your existing import
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class HBMGCRefineryRecipeMaker {

    public static List<HBMGCRefineryRecipeWrapper> getRecipes() {
        List<HBMGCRefineryRecipeWrapper> recipes = new ArrayList<>();

        // --- Resolve the kerosene Fluid from HBM (handles lowercase/uppercase field names) ---
        final Fluid KEROSENE = resolveKerosene();

        // --- Outputs (Galacticraft) ---
        final ItemStack GC_FUEL_CAN    = new ItemStack(GCItems.fuelCanister, 1, 1);
        final ItemStack GC_FUEL_BUCKET = new ItemStack(GCItems.bucketFuel);

        if (KEROSENE != null) {
            // --- Inputs (HBM containers prefilled with kerosene) ---
            final ItemStack HBM_KEROSENE_CAN        = ItemFluidCanister.getFullCanister(KEROSENE);
            final ItemStack HBM_KEROSENE_TANK       = ItemFluidTank.getFullTank(KEROSENE);
            final ItemStack HBM_KEROSENE_BARREL     = ItemFluidTank.getFullBarrel(KEROSENE);

            // Canister → GC fuel
            add(recipes, HBM_KEROSENE_CAN, GC_FUEL_CAN);
            add(recipes, HBM_KEROSENE_CAN, GC_FUEL_BUCKET);

            // Universal tank → GC fuel
            add(recipes, HBM_KEROSENE_TANK, GC_FUEL_CAN);
            add(recipes, HBM_KEROSENE_TANK, GC_FUEL_BUCKET);

            // Fluid barrel → GC fuel
            add(recipes, HBM_KEROSENE_BARREL, GC_FUEL_CAN);
            add(recipes, HBM_KEROSENE_BARREL, GC_FUEL_BUCKET);
        }

        return recipes;
    }

    // --- helpers ---

    private static void add(List<HBMGCRefineryRecipeWrapper> list, ItemStack in, ItemStack out) {
        if (in != null && !in.isEmpty() && out != null && !out.isEmpty()) {
            list.add(new HBMGCRefineryRecipeWrapper(in, out));
        }
    }

    private static Fluid resolveKerosene() {
        Fluid f = null;
        try { f = (Fluid) ModForgeFluids.class.getField("kerosene").get(null); } catch (Throwable ignored) {}
        if (f == null) f = FluidRegistry.getFluid("kerosene");
        return f;
    }
}
