package jammerbam.hbmgccompanion.mixins;

import jammerbam.hbmgccompanion.compat.HBMGCFluids;
import micdoodle8.mods.galacticraft.core.tile.TileEntityRefinery;
import micdoodle8.mods.galacticraft.core.util.FluidUtil;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = TileEntityRefinery.class, remap = false)
abstract class GCRefineryKeroseneOnlyMixin {

    private static boolean isHBMKerosene(FluidStack fs) {
        return HBMGCFluids.isKerosene(fs);
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
        return HBMGCFluids.isKeroseneName(name);
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
