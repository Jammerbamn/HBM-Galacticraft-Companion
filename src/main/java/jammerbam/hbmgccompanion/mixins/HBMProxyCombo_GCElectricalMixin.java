package jammerbam.hbmgccompanion.mixins;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.bomb.TileEntityLaunchPadBase;
import jammerbam.hbmgccompanion.compat.hbm.HbmFullLaunchPadFuelService;
import jammerbam.hbmgccompanion.compat.hbm.HbmGalacticraftEnergy;
import micdoodle8.mods.galacticraft.api.entity.IFuelable;
import micdoodle8.mods.galacticraft.api.transmission.NetworkType;
import micdoodle8.mods.galacticraft.api.transmission.tile.IElectrical;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = TileEntityProxyCombo.class, remap = false)
abstract class HBMProxyCombo_GCElectricalMixin implements IElectrical, IFuelable {
    @Shadow
    public abstract TileEntity getTile();

    @Override
    public float receiveElectricity(EnumFacing from, float receive, int tierGC, boolean doReceive) {
        if (!hbmgc$isBasicLaunchPadProxy()) {
            return 0.0F;
        }

        return HbmGalacticraftEnergy.receiveElectricity((IEnergyReceiverMK2) (Object) this, receive, doReceive);
    }

    @Override
    public float provideElectricity(EnumFacing from, float request, boolean doProvide) {
        return 0.0F;
    }

    @Override
    public float getRequest(EnumFacing direction) {
        if (!hbmgc$isBasicLaunchPadProxy()) {
            return 0.0F;
        }

        return HbmGalacticraftEnergy.getRequest((IEnergyReceiverMK2) (Object) this);
    }

    @Override
    public float getProvide(EnumFacing direction) {
        return 0.0F;
    }

    @Override
    public int getTierGC() {
        return HbmGalacticraftEnergy.HEAVY_ALUMINUM_WIRE_TIER;
    }

    @Override
    public boolean canConnect(EnumFacing direction, NetworkType type) {
        if (direction == null || !direction.getAxis().isHorizontal() || !hbmgc$isBasicLaunchPadProxy()) {
            return false;
        }

        if (type == NetworkType.POWER) {
            return true;
        }

        return type == NetworkType.FLUID;
    }

    @Override
    public int addFuel(FluidStack fuel, boolean doFill) {
        TileEntity core = this.getTile();
        if (!(core instanceof TileEntityLaunchPadBase) || !HbmGalacticraftEnergy.isBasicLaunchPad(core)) {
            return 0;
        }

        return HbmFullLaunchPadFuelService.addFuel(core, hbmgc$getTanks(core), fuel, doFill);
    }

    @Override
    public FluidStack removeFuel(int amount) {
        TileEntity core = this.getTile();
        if (!(core instanceof TileEntityLaunchPadBase) || !HbmGalacticraftEnergy.isBasicLaunchPad(core)) {
            return null;
        }

        return HbmFullLaunchPadFuelService.removeFuel(core, hbmgc$getTanks(core), amount);
    }

    @Unique
    private boolean hbmgc$isBasicLaunchPadProxy() {
        return HbmGalacticraftEnergy.isBasicLaunchPad(this.getTile());
    }

    @Unique
    private FluidTankNTM[] hbmgc$getTanks(TileEntity core) {
        return ((TileEntityLaunchPadBase) core).tanks;
    }
}
