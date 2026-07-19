package jammerbam.hbmgccompanion.client.jei.gcrefinery;

import jammerbam.hbmgccompanion.compat.HBMGCFluids;
import java.util.ArrayList;
import java.util.List;

import micdoodle8.mods.galacticraft.core.GCItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/**
 * Fork-agnostic JEI recipe maker.
 *
 * Shows the HBM kerosene fluid icon as the refinery input. The actual refinery mixin still
 * accepts Forge fluid kerosene; JEI uses the icon because it matches HBM's in-game UI.
 */
public class HBMGCRefineryRecipeMaker {

    public static List<HBMGCRefineryRecipeWrapper> getRecipes() {
        List<HBMGCRefineryRecipeWrapper> recipes = new ArrayList<>();

        ItemStack gcFuelCan = new ItemStack(GCItems.fuelCanister, 1, 1);
        ItemStack gcFuelBucket = new ItemStack(GCItems.bucketFuel);

        ItemStack keroseneIcon = findKeroseneFluidIconStack();
        if (!keroseneIcon.isEmpty()) {
            addItem(recipes, keroseneIcon, gcFuelCan);
            addItem(recipes, keroseneIcon, gcFuelBucket);
        }

        return recipes;
    }

    private static void addItem(List<HBMGCRefineryRecipeWrapper> list, ItemStack in, ItemStack out) {
        if (list == null || in == null || in.isEmpty() || out == null || out.isEmpty()) return;
        list.add(new HBMGCRefineryRecipeWrapper(in.copy(), out));
    }

    private static ItemStack findKeroseneFluidIconStack() {
        Item icon = ForgeRegistries.ITEMS.getValue(new ResourceLocation("hbm", "fluid_icon"));
        if (icon == null) return ItemStack.EMPTY;

        NonNullList<ItemStack> variants = NonNullList.create();
        try {
            icon.getSubItems(CreativeTabs.SEARCH, variants);
        } catch (Throwable t) {
            variants.clear();
            try {
                icon.getSubItems(CreativeTabs.MISC, variants);
            } catch (Throwable ignored) {}
        }

        for (ItemStack stack : variants) {
            if (isKeroseneIcon(stack)) return stack.copy();
        }

        for (int meta = 0; meta < 256; meta++) {
            ItemStack stack = new ItemStack(icon, 1, meta);
            if (isKeroseneIcon(stack)) return stack;
        }

        return ItemStack.EMPTY;
    }

    private static boolean isKeroseneIcon(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        String name = stack.getDisplayName();
        return name != null && name.toLowerCase(java.util.Locale.ROOT).contains(HBMGCFluids.KEROSENE);
    }
}
