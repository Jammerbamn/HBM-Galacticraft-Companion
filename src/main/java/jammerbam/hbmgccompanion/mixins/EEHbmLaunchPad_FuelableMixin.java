package jammerbam.hbmgccompanion.mixins;

import com.hbm.tileentity.bomb.TileEntityLaunchPad;
import jammerbam.hbmgccompanion.compat.hbm.HbmRocketFuelRelay;
import jammerbam.hbmgccompanion.compat.hbm.HbmRocketInventoryLaunchService;
import micdoodle8.mods.galacticraft.api.entity.IFuelable;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TileEntityLaunchPad.class, remap = false)
abstract class EEHbmLaunchPad_FuelableMixin implements IFuelable {

    @Override
    public int addFuel(FluidStack fuel, boolean doFill) {
        return HbmRocketFuelRelay.addFuel((TileEntityLaunchPad) (Object) this, fuel, doFill);
    }

    @Override
    public FluidStack removeFuel(int amount) {
        return HbmRocketFuelRelay.removeFuel((TileEntityLaunchPad) (Object) this, amount);
    }

    @Inject(method = "update", at = @At("RETURN"))
    private void hbmgc$syncInventoryRocket(CallbackInfo ci) {
        HbmRocketInventoryLaunchService.syncPad((TileEntityLaunchPad) (Object) this);
    }
}
