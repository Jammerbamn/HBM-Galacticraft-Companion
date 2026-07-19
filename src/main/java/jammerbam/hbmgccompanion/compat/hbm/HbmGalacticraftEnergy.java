package jammerbam.hbmgccompanion.compat.hbm;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.config.GeneralConfig;
import com.hbm.tileentity.bomb.TileEntityLaunchPadBase;
import micdoodle8.mods.galacticraft.core.energy.EnergyConfigHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public final class HbmGalacticraftEnergy {
    public static final int HEAVY_ALUMINUM_WIRE_TIER = 2;

    private HbmGalacticraftEnergy() {
    }

    public static float receiveElectricity(IEnergyReceiverMK2 receiver, float gcEnergy, boolean doReceive) {
        if (receiver == null || gcEnergy <= 0.0F || GeneralConfig.conversionRateHeToRF <= 0.0D) {
            return 0.0F;
        }

        long offeredHE = gcToHBMEnergy(gcEnergy);
        if (offeredHE <= 0L) {
            return 0.0F;
        }

        long rejectedHE = receiver.transferPower(offeredHE, !doReceive);
        long acceptedHE = offeredHE - rejectedHE;
        if (acceptedHE <= 0L) {
            return 0.0F;
        }

        return Math.min(gcEnergy, hbmToGCEnergy(acceptedHE));
    }

    public static float getRequest(IEnergyReceiverMK2 receiver) {
        if (receiver == null || GeneralConfig.conversionRateHeToRF <= 0.0D) {
            return 0.0F;
        }

        long requestedHE = Math.max(0L, receiver.getMaxPower() - receiver.getPower());
        return hbmToGCEnergy(requestedHE);
    }

    public static float hbmToGCEnergy(long hbmEnergy) {
        return (float) (hbmEnergy * GeneralConfig.conversionRateHeToRF * EnergyConfigHandler.RF_RATIO);
    }

    public static long gcToHBMEnergy(float gcEnergy) {
        double gcPerHE = GeneralConfig.conversionRateHeToRF * EnergyConfigHandler.RF_RATIO;
        if (gcPerHE <= 0.0D) {
            return 0L;
        }

        return (long) Math.ceil(gcEnergy / gcPerHE);
    }

    public static boolean isBasicLaunchPad(TileEntity tile) {
        if (!(tile instanceof TileEntityLaunchPadBase) || tile.getWorld() == null) {
            return false;
        }

        ResourceLocation registryName = tile.getWorld().getBlockState(tile.getPos()).getBlock().getRegistryName();
        return registryName != null
                && "hbm".equals(registryName.getNamespace())
                && "launch_pad".equals(registryName.getPath());
    }
}
