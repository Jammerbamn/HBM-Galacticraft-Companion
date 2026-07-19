package jammerbam.hbmgccompanion.client.jei.gcrefinery;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class HBMGCRefineryRecipeWrapper implements IRecipeWrapper {

    private final FluidStack inputFluid;
    private final ItemStack outputItem;

    public HBMGCRefineryRecipeWrapper(FluidStack inputFluid, ItemStack outputItem) {
        this.inputFluid = inputFluid;
        this.outputItem = outputItem;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        if (inputFluid != null && inputFluid.getFluid() != null) {
            ingredients.setInput(VanillaTypes.FLUID, inputFluid);
        }

        if (outputItem != null && !outputItem.isEmpty()) {
            ingredients.setOutput(VanillaTypes.ITEM, outputItem);
        }
    }
}

