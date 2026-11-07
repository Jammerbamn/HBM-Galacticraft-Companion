package jammerbam.hbmgccompanion.mixins;

import jammerbam.hbmgccompanion.compat.hbm.HbmRocketInventoryLaunchService;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntityAutoRocket;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntitySpaceshipBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntitySpaceshipBase.class, remap = false)
abstract class GCEntitySpaceshipBase_PadSourceDropMixin {
    @Inject(method = "dropShipAsItem", at = @At("HEAD"))
    private void hbmgc$clearPadSlotWhenRocketDrops(CallbackInfo ci) {
        if ((Object) this instanceof EntityAutoRocket) {
            HbmRocketInventoryLaunchService.clearSourceSlotWhenRocketLeavesPad((EntityAutoRocket) (Object) this);
        }
    }
}
