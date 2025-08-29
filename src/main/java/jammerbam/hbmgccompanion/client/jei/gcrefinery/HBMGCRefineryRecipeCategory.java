package jammerbam.hbmgccompanion.client.jei.gcrefinery;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;

import javax.annotation.Nonnull;

/**
 * PNG-driven refinery UI (HBM Kerosene -> GC Fuel).
 * No JEI fluid slots; we paint everything from the texture and animate via IDrawableAnimated,
 * same pattern Galacticraft uses in its JEI categories.
 */

public class HBMGCRefineryRecipeCategory implements IRecipeCategory {

    public static final String UID = "hbmgccompanion.refinery";

    // ---- sprite sheet ----
    private static final ResourceLocation refineryGuiTex =
            new ResourceLocation("hbmgccompanion", "textures/gui/refinery_recipe.png");

    private final IDrawable background;
    private final String localizedName;
    private final IDrawableAnimated keroseneBar;
    private final IDrawableAnimated fuelBar;


    public HBMGCRefineryRecipeCategory(IGuiHelper gui) {
        // static background
        this.background = gui.createDrawable(refineryGuiTex, 3, 4, 168, 64);
        this.localizedName = GCCoreUtil.translate("tile.refinery.name");

        IDrawableStatic progressBarDrawableOil = gui.createDrawable(refineryGuiTex, 176, 0, 16, 38);
        this.keroseneBar = gui.createAnimatedDrawable(progressBarDrawableOil, 70, IDrawableAnimated.StartDirection.TOP, true);
        IDrawableStatic progressBarDrawableFuel = gui.createDrawable(refineryGuiTex, 192, 0, 16, 38);
        this.fuelBar = gui.createAnimatedDrawable(progressBarDrawableFuel, 70, IDrawableAnimated.StartDirection.BOTTOM, false);
    }

    // ---- IRecipeCategory ----
    @Override @Nonnull public String getUid(){
        return UID;
    }

    @Override @Nonnull public String getTitle(){
        return this.localizedName;
    }

    @Override @Nonnull public String getModName(){
        return "HBMGCCompanion";
    }

    @Override @Nonnull public IDrawable getBackground(){
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper, IIngredients ingredients) {
        IGuiItemStackGroup itemstacks = recipeLayout.getItemStacks();

        itemstacks.init(0, true, 39, 2);
        itemstacks.init(1, false, 113, 2);

        itemstacks.set(ingredients);
    }

    @Override
    public void drawExtras(@Nonnull Minecraft minecraft) {
        this.keroseneBar.draw(minecraft, 40, 24);
        this.fuelBar.draw(minecraft, 114, 24);
    }
}
