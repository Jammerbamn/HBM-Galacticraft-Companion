package jammerbam.hbmgccompanion.client.jei.gcrefinery;

import javax.annotation.Nullable;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class HBMGCRefineryRecipeWrapper implements IRecipeWrapper {

    @Nullable
    private final FluidStack inputFluid;

    @Nullable
    private final ItemStack inputItem;

    private final ItemStack outputItem;

    /** Fluid input constructor (preferred, fork-agnostic) */
    public HBMGCRefineryRecipeWrapper(FluidStack inputFluid, ItemStack outputItem) {
        this.inputFluid = inputFluid;
        this.inputItem = null;
        this.outputItem = outputItem;
    }

    /** Item input constructor (optional: e.g., hbm:fluid_icon kerosene variant) */
    public HBMGCRefineryRecipeWrapper(ItemStack inputItem, ItemStack outputItem) {
        this.inputFluid = null;
        this.inputItem = inputItem;
        this.outputItem = outputItem;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        // Input: either Fluid OR Item (depending on which constructor was used)
        if (inputFluid != null && inputFluid.getFluid() != null) {
            ingredients.setInput(VanillaTypes.FLUID, inputFluid);
        } else if (inputItem != null && !inputItem.isEmpty()) {
            ingredients.setInput(VanillaTypes.ITEM, inputItem);
        }

        // Output: item
        if (outputItem != null && !outputItem.isEmpty()) {
            ingredients.setOutput(VanillaTypes.ITEM, outputItem);
        }
    }
}

