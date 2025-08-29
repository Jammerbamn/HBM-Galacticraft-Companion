package jammerbam.hbmgccompanion.client.jei;

import jammerbam.hbmgccompanion.client.jei.gcrefinery.HBMGCRefineryRecipeCategory;
import jammerbam.hbmgccompanion.client.jei.gcrefinery.HBMGCRefineryRecipeMaker;
import mezz.jei.api.*;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IGuiHelper;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

/**
 * JEI plugin that hides Galacticraft's "Refinery" recipe category.
 * Minecraft 1.12.2  |  JEI 4.x
 */
@JEIPlugin
public final class JeiCategoryHandler implements IModPlugin {
    private static final Logger LOG = LogManager.getLogger("HBMGCcompanion/JEI-Handler");

    // Set true once to log all categories (helps you confirm the exact UID in logs).
    private static final boolean DUMP_CATEGORIES_ON_LOAD = false;

    // Optional fast-path guesses (harmless if wrong).
    private static final String[] POSSIBLE_UIDS = new String[] {
            "galacticraft.refinery",
            "galacticraftcore.refinery",
            "micdoodle8.galacticraft.refinery",
            "micdoodle8.mods.galacticraft.refinery",
            "galacticraft:refinery"
    };

    @Override public void registerIngredients(@Nonnull IModIngredientRegistration registry) { /* no-op */ }
    @Override public void registerCategories(@Nonnull IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(new HBMGCRefineryRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
    }
    @Override public void register(@Nonnull IModRegistry registry) {

        registry.addRecipes(HBMGCRefineryRecipeMaker.getRecipes(), HBMGCRefineryRecipeCategory.UID);

        try {
            Item refinery = Item.getByNameOrId("galacticraftcore:refinery");
            if (refinery != null) {
                registry.addRecipeCatalyst(new ItemStack(refinery), HBMGCRefineryRecipeCategory.UID);
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        final IRecipeRegistry rr = jeiRuntime.getRecipeRegistry();

        if (DUMP_CATEGORIES_ON_LOAD) {
            for (IRecipeCategory<?> c : rr.getRecipeCategories()) {
                LOG.info("JEI Category: title='{}', uid='{}', mod='{}'", c.getTitle(), c.getUid(), c.getModName());
            }
        }

        int hidden = 0;

        // 1) Try known UIDs first.
        for (String uid : POSSIBLE_UIDS) {
            try {
                rr.hideRecipeCategory(uid);
                LOG.info("JEI: attempted hide by UID '{}'", uid);
                hidden++;
            } catch (Throwable ignored) {
                // UID simply doesn't exist in this environment — safe to ignore
            }
        }

        // 2) Fallback: find the GC category whose title/uid mentions "refinery".
        try {
            for (IRecipeCategory<?> c : rr.getRecipeCategories()) {
                final String title = toLower(c.getTitle());
                final String mod   = toLower(c.getModName());
                final String uid   = toLower(c.getUid());

                if ((title.contains("refinery") || uid.contains("refinery")) && mod.contains("galacticraft")) {
                    rr.hideRecipeCategory(c.getUid());
                    LOG.info("JEI: hidden '{}' (uid='{}', mod='{}')", c.getTitle(), c.getUid(), c.getModName());
                    hidden++;
                }
            }
        } catch (Throwable t) {
            LOG.warn("JEI: discovery-based hide failed: {}", t.toString());
        }

        if (hidden == 0) {
            LOG.warn("JEI: no Refinery category found to hide. Enable DUMP_CATEGORIES_ON_LOAD to discover the exact uid.");
        }
    }

    private static String toLower(String s) {
        return s == null ? "" : s.toLowerCase(java.util.Locale.ROOT);
    }
}

