package jammerbam.hbmgccompanion.mixins;

import com.hbm.inventory.fluid.Fluids;
import jammerbam.hbmgccompanion.compat.hbm.HbmGalacticraftFluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Fluids.class, remap = false)
abstract class HBMFluids_GCFuelRegistrationMixin {
    @Inject(method = "init", at = @At("HEAD"))
    private static void hbmgc$registerGCFuelFluidType(CallbackInfo ci) {
        HbmGalacticraftFluids.registerFluidListener();
    }
}
