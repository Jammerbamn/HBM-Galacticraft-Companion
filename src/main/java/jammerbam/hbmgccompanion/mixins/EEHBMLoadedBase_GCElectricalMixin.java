package jammerbam.hbmgccompanion.mixins;

import api.hbm.energy.IEnergyGenerator;
import api.hbm.energy.IEnergyUser;
import com.hbm.config.GeneralConfig;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityLoadedBase;
import micdoodle8.mods.galacticraft.api.transmission.NetworkType;
import micdoodle8.mods.galacticraft.api.transmission.tile.IElectrical;
import micdoodle8.mods.galacticraft.core.energy.EnergyConfigHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = TileEntityLoadedBase.class, remap = false)
abstract class EEHBMLoadedBase_GCElectricalMixin implements IElectrical {

    @Unique
    private static final int hbmgc$HEAVY_ALUMINUM_WIRE_TIER = 2;

    @Override
    public float receiveElectricity(EnumFacing from, float receive, int tier, boolean doReceive) {
        IEnergyUser energyUser = hbmgc$energyUser();
        if (energyUser == null || !this.canConnect(from, NetworkType.POWER)
                || receive <= 0.0F || GeneralConfig.conversionRateHeToRF <= 0.0D) {
            return 0.0F;
        }

        long offeredHE = hbmgc$gcToHBMEnergy(receive);
        if (offeredHE <= 0L) {
            return 0.0F;
        }

        long currentPower = energyUser.getPower();
        long acceptedHE = Math.min(offeredHE, Math.max(0L, energyUser.getMaxPower() - currentPower));
        if (doReceive && acceptedHE > 0L) {
            energyUser.setPower(currentPower + acceptedHE);
        }

        return acceptedHE > 0L ? Math.min(receive, hbmgc$hbmToGCEnergy(acceptedHE)) : 0.0F;
    }

    @Override
    public float provideElectricity(EnumFacing from, float request, boolean doProvide) {
        IEnergyUser energyUser = hbmgc$energyUser();
        if (!(energyUser instanceof IEnergyGenerator)
                || !this.canConnect(from, NetworkType.POWER)
                || request <= 0.0F
                || GeneralConfig.conversionRateHeToRF <= 0.0D) {
            return 0.0F;
        }

        long providedHE = Math.min(energyUser.getPower(), hbmgc$gcToHBMEnergy(request));
        if (providedHE <= 0L) {
            return 0.0F;
        }

        if (doProvide) {
            energyUser.setPower(Math.max(0L, energyUser.getPower() - providedHE));
        }

        return Math.min(request, hbmgc$hbmToGCEnergy(providedHE));
    }

    @Override
    public float getRequest(EnumFacing direction) {
        IEnergyUser energyUser = hbmgc$energyUser();
        if (energyUser == null || !this.canConnect(direction, NetworkType.POWER)
                || GeneralConfig.conversionRateHeToRF <= 0.0D) {
            return 0.0F;
        }

        long requestedHE = Math.max(0L, energyUser.getMaxPower() - energyUser.getPower());
        return hbmgc$hbmToGCEnergy(requestedHE);
    }

    @Override
    public float getProvide(EnumFacing direction) {
        IEnergyUser energyUser = hbmgc$energyUser();
        if (!(energyUser instanceof IEnergyGenerator) || !this.canConnect(direction, NetworkType.POWER)
                || GeneralConfig.conversionRateHeToRF <= 0.0D) {
            return 0.0F;
        }

        return hbmgc$hbmToGCEnergy(Math.max(0L, energyUser.getPower()));
    }

    @Override
    public int getTierGC() {
        return hbmgc$HEAVY_ALUMINUM_WIRE_TIER;
    }

    @Override
    public boolean canConnect(EnumFacing direction, NetworkType type) {
        IEnergyUser energyUser = hbmgc$energyUser();
        return type == NetworkType.POWER && direction != null && energyUser != null
                && energyUser.canConnect(ForgeDirection.getOrientation(direction));
    }

    @Unique
    private IEnergyUser hbmgc$energyUser() {
        Object self = this;
        return self instanceof IEnergyUser ? (IEnergyUser) self : null;
    }

    @Unique
    private float hbmgc$hbmToGCEnergy(long hbmEnergy) {
        return (float) (hbmEnergy * GeneralConfig.conversionRateHeToRF * EnergyConfigHandler.RF_RATIO);
    }

    @Unique
    private long hbmgc$gcToHBMEnergy(float gcEnergy) {
        double gcPerHE = GeneralConfig.conversionRateHeToRF * EnergyConfigHandler.RF_RATIO;
        if (gcPerHE <= 0.0D) {
            return 0L;
        }

        return (long) Math.ceil(gcEnergy / gcPerHE);
    }
}
