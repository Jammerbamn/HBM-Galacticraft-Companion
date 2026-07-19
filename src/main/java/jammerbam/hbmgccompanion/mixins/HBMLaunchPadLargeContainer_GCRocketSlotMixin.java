package jammerbam.hbmgccompanion.mixins;

import com.hbm.inventory.container.ContainerLaunchPadLarge;
import com.hbm.tileentity.bomb.TileEntityLaunchPadBase;
import jammerbam.hbmgccompanion.compat.galacticraft.GalacticraftRocketFactory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ContainerLaunchPadLarge.class, remap = false)
abstract class HBMLaunchPadLargeContainer_GCRocketSlotMixin {
    @Redirect(
            method = "transferStackInSlot",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/hbm/tileentity/bomb/TileEntityLaunchPadBase;isMissileValid(Lnet/minecraft/item/ItemStack;)Z"
            )
    )
    private boolean hbmgc$acceptGCRocketsForRocketSlot(TileEntityLaunchPadBase launchpad, ItemStack stack) {
        return launchpad.isMissileValid(stack) || GalacticraftRocketFactory.isSupportedRocket(stack);
    }
}
