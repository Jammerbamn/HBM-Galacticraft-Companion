package jammerbam.hbmgccompanion.client.jei.gcrefinery;

import javax.annotation.Nonnull;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

public class HBMGCRefineryRecipeWrapper implements IRecipeWrapper
{

    @Nonnull private final ItemStack input;
    @Nonnull private final ItemStack output;

    public HBMGCRefineryRecipeWrapper(@Nonnull ItemStack input, @Nonnull ItemStack output)
    {
        this.input = input;
        this.output = output;
    }

    @Override
    public void getIngredients(IIngredients ingredients)
    {
        ingredients.setInput(ItemStack.class, this.input);
        ingredients.setOutput(ItemStack.class, this.output);
    }
}
