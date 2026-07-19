package jammerbam.hbmgccompanion.mixins;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.tileentity.bomb.TileEntityLaunchPadBase;
import jammerbam.hbmgccompanion.compat.hbm.HbmFullLaunchPadFuelService;
import jammerbam.hbmgccompanion.compat.hbm.HbmGalacticraftEnergy;
import micdoodle8.mods.galacticraft.api.transmission.NetworkType;
import micdoodle8.mods.galacticraft.api.transmission.tile.IElectrical;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = TileEntityLaunchPadBase.class, remap = false)
abstract class HBMLaunchPadBase_GCElectricalMixin implements IElectrical {
    @Override
    public float receiveElectricity(EnumFacing from, float receive, int tierGC, boolean doReceive) {
        if (!hbmgc$isBasicLaunchPad()) {
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
        if (!hbmgc$isBasicLaunchPad()) {
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
        if (direction == null || !direction.getAxis().isHorizontal() || !hbmgc$isBasicLaunchPad()) {
            return false;
        }

        if (type == NetworkType.POWER) {
            return true;
        }

        return type == NetworkType.FLUID;
    }

    @Unique
    private boolean hbmgc$isBasicLaunchPad() {
        return HbmGalacticraftEnergy.isBasicLaunchPad((TileEntity) (Object) this);
    }
}
