package jammerbam.hbmgccompanion.client.jei.gcrefinery;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

public class HBMGCRefineryRecipeWrapper implements IRecipeWrapper {

    private final ItemStack inputItem;
    private final ItemStack outputItem;

    public HBMGCRefineryRecipeWrapper(ItemStack inputItem, ItemStack outputItem) {
        this.inputItem = inputItem;
        this.outputItem = outputItem;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        if (inputItem != null && !inputItem.isEmpty()) {
            ingredients.setInput(VanillaTypes.ITEM, inputItem);
        }

        if (outputItem != null && !outputItem.isEmpty()) {
            ingredients.setOutput(VanillaTypes.ITEM, outputItem);
        }
    }
}

