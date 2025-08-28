package jammerbam.hbmgccompanion.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import micdoodle8.mods.galacticraft.core.util.FluidUtil;

@Mixin(value = FluidUtil.class, remap = false) // targeting a mod class → keep remap=false
public abstract class gcrefineryMixin {

    // FluidUtil.testOil(String name) → boolean
    @Inject(method = "testOil", at = @At("HEAD"), cancellable = true)
    private static void hbmCompat$acceptKerosene(String name, CallbackInfoReturnable<Boolean> cir) {
        if (name != null && name.equals("kerosene")) {
            cir.setReturnValue(true);
        }
    }
}
