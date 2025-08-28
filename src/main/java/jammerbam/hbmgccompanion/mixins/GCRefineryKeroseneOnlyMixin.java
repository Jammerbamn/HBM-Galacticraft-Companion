package jammerbam.hbmgccompanion.mixins;

import micdoodle8.mods.galacticraft.core.tile.TileEntityRefinery;
import micdoodle8.mods.galacticraft.core.util.FluidUtil;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = TileEntityRefinery.class, remap = false)
abstract class GCRefineryKeroseneOnlyMixin {

    /** Return true iff the fluid is exactly HBM's kerosene */
    private static boolean isHBMKerosene(FluidStack fs) {
        if (fs == null) return false;
        final Fluid kerosene = FluidRegistry.getFluid("kerosene");
        return kerosene != null && fs.getFluid() == kerosene;
    }

    // 1) Inventory → tank: restrict FluidUtil.isOil(liquid)
    @Redirect(
            method = "update",
            at = @At(
                    value = "INVOKE",
                    target = "Lmicdoodle8/mods/galacticraft/core/util/FluidUtil;isOil(Lnet/minecraftforge/fluids/FluidStack;)Z"
            )
    )
    private boolean hbmgc$onlyKeroseneInInventory(FluidStack liquid) {
        return isHBMKerosene(liquid);
    }

    // 2) Pipe input: restrict FluidUtil.testOil(name)
    @Redirect(
            method = "fill",
            at = @At(
                    value = "INVOKE",
                    target = "Lmicdoodle8/mods/galacticraft/core/util/FluidUtil;testOil(Ljava/lang/String;)Z"
            )
    )
    private boolean hbmgc$onlyKeroseneOnPipe(String name) {
        return "kerosene".equals(name);
    }

    // 3) Insertable items: restrict FluidUtil.isOilContainerAny(stack)
    @Redirect(
            method = "canInsertItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lmicdoodle8/mods/galacticraft/core/util/FluidUtil;isOilContainerAny(Lnet/minecraft/item/ItemStack;)Z"
            )
    )
    private boolean hbmgc$onlyKeroseneContainer(ItemStack stack) {
        final FluidStack contained = FluidUtil.getFluidContained(stack);
        return isHBMKerosene(contained);
    }
}
