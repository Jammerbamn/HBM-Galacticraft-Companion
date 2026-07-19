package jammerbam.hbmgccompanion.mixins;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.tileentity.bomb.TileEntityLaunchPadBase;
import jammerbam.hbmgccompanion.compat.hbm.HbmFullLaunchPadFuelService;
import jammerbam.hbmgccompanion.compat.hbm.HbmGalacticraftEnergy;
import jammerbam.hbmgccompanion.compat.hbm.HbmRocketInventoryLaunchService;
import micdoodle8.mods.galacticraft.api.entity.IFuelable;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TileEntityLaunchPadBase.class, remap = false)
abstract class HBMLaunchPadBase_GCFuelableMixin implements IFuelable {
    @Shadow
    public FluidTankNTM[] tanks;

    @Shadow
    public abstract long getPower();

    @Shadow
    public abstract void setPower(long power);

    @Override
    public int addFuel(FluidStack fuel, boolean doFill) {
        return HbmFullLaunchPadFuelService.addFuel((TileEntityLaunchPadBase) (Object) this, this.tanks, fuel, doFill);
    }

    @Override
    public FluidStack removeFuel(int amount) {
        return HbmFullLaunchPadFuelService.removeFuel((TileEntityLaunchPadBase) (Object) this, this.tanks, amount);
    }

    public long getDemand(FluidType type, int pressure) {
        long demand = HbmFullLaunchPadFuelService.getDemand((TileEntityLaunchPadBase) (Object) this, this.tanks, type);
        if (demand >= 0L) {
            return demand;
        }

        return hbmgc$getTypedTankDemand(type);
    }

    public long transferFluid(FluidType type, int pressure, long fluid) {
        long rejected = HbmFullLaunchPadFuelService.transferFluid(
                (TileEntityLaunchPadBase) (Object) this,
                this.tanks,
                type,
                fluid
        );
        if (rejected >= 0L) {
            return rejected;
        }

        return hbmgc$transferTypedTankFluid(type, fluid);
    }

    @Inject(method = "getReceivingTanks", at = @At("HEAD"), cancellable = true)
    private void hbmgc$getReceivingTanksOnlyWhenGCRocketPresent(CallbackInfoReturnable<FluidTankNTM[]> cir) {
        TileEntityLaunchPadBase pad = (TileEntityLaunchPadBase) (Object) this;
        if (HbmGalacticraftEnergy.isBasicLaunchPad(pad)) {
            cir.setReturnValue(HbmFullLaunchPadFuelService.getReceivingTanks(pad, this.tanks));
        }
    }

    @Inject(method = "func_73660_a", at = @At("HEAD"))
    private void hbmgc$prepareGCFuelTanks(CallbackInfo ci) {
        HbmRocketInventoryLaunchService.syncFullPad((TileEntityLaunchPadBase) (Object) this);
        HbmFullLaunchPadFuelService.prepareFuelTanks((TileEntityLaunchPadBase) (Object) this, this.tanks);
    }

    @Inject(method = "func_73660_a", at = @At("RETURN"))
    private void hbmgc$fuelGCRocketFromPadTanks(CallbackInfo ci) {
        long power = this.getPower();
        long used = HbmFullLaunchPadFuelService.fuelRocketFromPad(
                (TileEntityLaunchPadBase) (Object) this,
                this.tanks,
                power
        );
        if (used > 0L) {
            this.setPower(Math.max(0L, power - used));
        }
    }

    private long hbmgc$getTypedTankDemand(FluidType type) {
        if (type == null) {
            return 0L;
        }

        long demand = 0L;
        for (FluidTankNTM tank : this.tanks) {
            if (tank.getTankType() == type) {
                demand += Math.max(0, tank.getMaxFill() - tank.getFill());
            }
        }

        return demand;
    }

    private long hbmgc$transferTypedTankFluid(FluidType type, long amount) {
        if (type == null || amount <= 0L) {
            return amount;
        }

        long remaining = amount;
        for (FluidTankNTM tank : this.tanks) {
            if (remaining <= 0L) {
                break;
            }

            if (tank.getTankType() == type) {
                int accepted = tank.fill(type, hbmgc$clampToInt(remaining), true);
                remaining -= accepted;
            }
        }

        return remaining;
    }

    private int hbmgc$clampToInt(long amount) {
        if (amount <= 0L) {
            return 0;
        }

        return amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
    }
}
