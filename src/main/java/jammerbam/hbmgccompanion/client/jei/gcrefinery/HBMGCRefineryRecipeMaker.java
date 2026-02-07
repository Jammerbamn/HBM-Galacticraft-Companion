package jammerbam.hbmgccompanion.client.jei.gcrefinery;

import java.util.ArrayList;
import java.util.List;

import micdoodle8.mods.galacticraft.core.GCItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/**
 * Fork-agnostic JEI recipe maker.
 *
 * - Primary JEI input: Forge FluidStack "kerosene" (works on CE + EE if fluid is registered)
 * - Optional JEI input: HBM universal icon item "hbm:fluid_icon" variant that represents Kerosene
 *   (discovered dynamically; no com.hbm.* imports)
 *
 * NOTE: To use the optional ItemStack input recipes, your HBMGCRefineryRecipeWrapper must
 * support an ItemStack input as well (e.g., a second constructor taking ItemStack).
 */
public class HBMGCRefineryRecipeMaker {

    public static List<HBMGCRefineryRecipeWrapper> getRecipes() {
        List<HBMGCRefineryRecipeWrapper> recipes = new ArrayList<>();

        // Outputs (Galacticraft)
        final ItemStack gcFuelCan = new ItemStack(GCItems.fuelCanister, 1, 1);
        final ItemStack gcFuelBucket = new ItemStack(GCItems.bucketFuel);

        // Optional: show the HBM "fluid_icon" kerosene variant as an example input (if present)
        final ItemStack keroseneIcon = findKeroseneFluidIconStack();
        if (keroseneIcon != null && !keroseneIcon.isEmpty()) {
            // These two lines require your Wrapper to have an ItemStack-input constructor.
            addItem(recipes, keroseneIcon, gcFuelCan);
            addItem(recipes, keroseneIcon, gcFuelBucket);
        }

        return recipes;
    }

    // ---------------- helpers ----------------

    private static void addFluid(List<HBMGCRefineryRecipeWrapper> list, FluidStack in, ItemStack out) {
        if (list == null || in == null || in.getFluid() == null || out == null || out.isEmpty()) return;
        list.add(new HBMGCRefineryRecipeWrapper(in, out));
    }

    /**
     * Requires a second wrapper constructor: HBMGCRefineryRecipeWrapper(ItemStack in, ItemStack out)
     * If you don't add that, comment out the calls to addItem(...) above.
     */
    private static void addItem(List<HBMGCRefineryRecipeWrapper> list, ItemStack in, ItemStack out) {
        if (list == null || in == null || in.isEmpty() || out == null || out.isEmpty()) return;
        list.add(new HBMGCRefineryRecipeWrapper(in.copy(), out));
    }

    /**
     * Prefer Forge registry. Optional fallback to EE field via reflection (no import).
     */
    private static Fluid resolveKerosene() {
        Fluid f = FluidRegistry.getFluid("kerosene");
        if (f != null) return f;

        // Optional EE fallback if the fluid isn't registered under the plain name.
        try {
            Class<?> c = Class.forName("com.hbm.forgefluid.ModForgeFluids");
            Object v = c.getField("kerosene").get(null);
            if (v instanceof Fluid) return (Fluid) v;
        } catch (Throwable ignored) {}

        return null;
    }

    /**
     * Tries to find the HBM universal fluid icon stack that represents Kerosene, without importing HBM.
     *
     * Strategy:
     *  1) Look up registry item "hbm:fluid_icon"
     *  2) Ask it for subitems in CreativeTabs.SEARCH (best case: it enumerates all metas/NBT)
     *  3) Match by display name containing "kerosene"
     *  4) If no subitems, brute scan metas 0..255 and match by display name (bounded)
     */
    private static ItemStack findKeroseneFluidIconStack() {
        Item icon = ForgeRegistries.ITEMS.getValue(new ResourceLocation("hbm", "fluid_icon"));
        if (icon == null) return ItemStack.EMPTY;

        // 1) Try subitems first (fast + accurate if implemented)
        NonNullList<ItemStack> variants = NonNullList.create();
        try {
            icon.getSubItems(CreativeTabs.SEARCH, variants);
        } catch (Throwable t) {
            variants.clear();
            try {
                icon.getSubItems(CreativeTabs.MISC, variants);
            } catch (Throwable ignored) {}
        }

        for (ItemStack s : variants) {
            if (s == null || s.isEmpty()) continue;
            String name = safeLower(s.getDisplayName());
            if (name.contains("kerosene")) return s.copy();
        }

        // 2) Fallback: bounded meta scan (some items don’t enumerate variants)
        for (int meta = 0; meta < 256; meta++) {
            ItemStack s = new ItemStack(icon, 1, meta);
            if (s.isEmpty()) continue;
            String name = safeLower(s.getDisplayName());
            if (name.contains("kerosene")) return s;
        }

        return ItemStack.EMPTY;
    }

    private static String safeLower(String s) {
        return s == null ? "" : s.toLowerCase();
    }
}