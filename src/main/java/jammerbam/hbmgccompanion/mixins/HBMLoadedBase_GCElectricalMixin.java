package jammerbam.hbmgccompanion.mixins;

import com.hbm.api.energymk2.IEnergyHandlerMK2;
import com.hbm.api.energymk2.IEnergyProviderMK2;
import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.lib.ForgeDirection;
import jammerbam.hbmgccompanion.compat.hbm.HbmGalacticraftEnergy;
import micdoodle8.mods.galacticraft.api.transmission.NetworkType;
import micdoodle8.mods.galacticraft.api.transmission.tile.IElectrical;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = TileEntityLoadedBase.class, remap = false)
abstract class HBMLoadedBase_GCElectricalMixin implements IElectrical {

    @Override
    public float receiveElectricity(EnumFacing from, float receive, int tierGC, boolean doReceive) {
        Object self = this;
        if (!(self instanceof IEnergyReceiverMK2) || !this.canConnect(from, NetworkType.POWER)) {
            return 0.0F;
        }
        return HbmGalacticraftEnergy.receiveElectricity((IEnergyReceiverMK2) self, receive, doReceive);
    }

    @Override
    public float provideElectricity(EnumFacing from, float request, boolean doProvide) {
        Object self = this;
        if (!(self instanceof IEnergyProviderMK2) || !this.canConnect(from, NetworkType.POWER)
                || request <= 0.0F) {
            return 0.0F;
        }

        IEnergyProviderMK2 provider = (IEnergyProviderMK2) self;
        long offeredHE = Math.min(Math.max(0L, provider.getPower()), provider.getProviderSpeed());
        long providedHE = Math.min(offeredHE, HbmGalacticraftEnergy.gcToHBMEnergy(request));
        if (providedHE <= 0L) {
            return 0.0F;
        }
        if (doProvide) {
            provider.usePower(providedHE);
        }
        return Math.min(request, HbmGalacticraftEnergy.hbmToGCEnergy(providedHE));
    }

    @Override
    public float getRequest(EnumFacing direction) {
        Object self = this;
        if (!(self instanceof IEnergyReceiverMK2) || !this.canConnect(direction, NetworkType.POWER)) {
            return 0.0F;
        }
        return HbmGalacticraftEnergy.getRequest((IEnergyReceiverMK2) self);
    }

    @Override
    public float getProvide(EnumFacing direction) {
        Object self = this;
        if (!(self instanceof IEnergyProviderMK2) || !this.canConnect(direction, NetworkType.POWER)) {
            return 0.0F;
        }
        IEnergyProviderMK2 provider = (IEnergyProviderMK2) self;
        return HbmGalacticraftEnergy.hbmToGCEnergy(
                Math.min(Math.max(0L, provider.getPower()), provider.getProviderSpeed())
        );
    }

    @Override
    public int getTierGC() {
        return HbmGalacticraftEnergy.HEAVY_ALUMINUM_WIRE_TIER;
    }

    @Override
    public boolean canConnect(EnumFacing direction, NetworkType type) {
        IEnergyHandlerMK2 handler = hbmgc$energyHandler();
        return type == NetworkType.POWER && direction != null && handler != null
                && handler.canConnect(ForgeDirection.getOrientation(direction));
    }

    @Unique
    private IEnergyHandlerMK2 hbmgc$energyHandler() {
        Object self = this;
        return self instanceof IEnergyHandlerMK2 ? (IEnergyHandlerMK2) self : null;
    }
}
