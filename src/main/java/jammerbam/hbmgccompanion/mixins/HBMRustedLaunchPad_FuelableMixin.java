package jammerbam.hbmgccompanion.mixins;

import com.hbm.tileentity.bomb.TileEntityLaunchPadRusted;
import jammerbam.hbmgccompanion.compat.hbm.HbmRocketInventoryLaunchService;
import jammerbam.hbmgccompanion.compat.hbm.HbmRocketFuelRelay;
import micdoodle8.mods.galacticraft.api.entity.IFuelable;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TileEntityLaunchPadRusted.class, remap = false)
abstract class HBMRustedLaunchPad_FuelableMixin implements IFuelable {

    @Override
    public int addFuel(FluidStack fuel, boolean doFill) {
        return HbmRocketFuelRelay.addFuel((TileEntityLaunchPadRusted) (Object) this, fuel, doFill);
    }

    @Override
    public FluidStack removeFuel(int amount) {
        return HbmRocketFuelRelay.removeFuel((TileEntityLaunchPadRusted) (Object) this, amount);
    }

    @Inject(method = "func_73660_a", at = @At("RETURN"))
    private void hbmgc$syncInventoryRocket(CallbackInfo ci) {
        HbmRocketInventoryLaunchService.syncRustedPad((TileEntityLaunchPadRusted) (Object) this);
    }
}
