package jammerbam.hbmgccompanion.mixins;

import jammerbam.hbmgccompanion.compat.galacticraft.LandingPadSlot;
import jammerbam.hbmgccompanion.compat.galacticraft.GalacticraftLaunchPadReturnService;
import jammerbam.hbmgccompanion.compat.galacticraft.RocketLandingPadInventory;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntityAutoRocket;
import micdoodle8.mods.galacticraft.core.inventory.ContainerRocketInventory;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ContainerRocketInventory.class, remap = false)
abstract class GCContainerRocketInventory_LandingPadSlotMixin extends Container {
    @Shadow
    private IInventory spaceshipInv;

    @Inject(method = "addSlotsNoInventory", at = @At("HEAD"))
    private void hbmgc$addLandingPadSlotNoCargo(CallbackInfo ci) {
        this.hbmgc$addLandingPadSlot();
    }

    @Inject(method = "addSlotsWithInventory", at = @At("HEAD"))
    private void hbmgc$addLandingPadSlotWithCargo(int inventorySize, CallbackInfo ci) {
        this.hbmgc$addLandingPadSlot();
    }

    private void hbmgc$addLandingPadSlot() {
        if (this.spaceshipInv instanceof EntityAutoRocket
                && GalacticraftLaunchPadReturnService.isRocketOnHbmPad((EntityAutoRocket) this.spaceshipInv)) {
            this.addSlotToContainer(new LandingPadSlot(new RocketLandingPadInventory(
                    (EntityAutoRocket) this.spaceshipInv
            )));
        }
    }
}
