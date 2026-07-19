package jammerbam.hbmgccompanion.mixins;

import jammerbam.hbmgccompanion.compat.galacticraft.GalacticraftLaunchPadReturnService;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntityAutoRocket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityAutoRocket.class, remap = false)
abstract class GCEntityAutoRocket_HBMPadReturnMixin {

    @Inject(method = "onLaunch", at = @At("RETURN"))
    private void hbmgc$returnGalacticraftPadsFromHbmLaunch(CallbackInfo ci) {
        GalacticraftLaunchPadReturnService.recordPadReturnForHbmLaunch((EntityAutoRocket) (Object) this);
    }
}
