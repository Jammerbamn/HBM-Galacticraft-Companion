package jammerbam.hbmgccompanion.compat.hbm;

import com.hbm.capability.NTMFluidCapabilityHandler;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.tileentity.TileEntityLoadedBase;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntityAutoRocket;
import micdoodle8.mods.galacticraft.core.GCFluids;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class HbmFullLaunchPadFuelService {
    private static final Logger LOG = LogManager.getLogger("hbmgccompanion/HBM-Full-Launch-Pad");
    private static final int TRANSFER_PER_TICK = 2;
    private static final int HE_PER_MB_FUEL_LOADED = 1;
    private static final FluidTankNTM[] NO_RECEIVING_TANKS = new FluidTankNTM[0];

    private HbmFullLaunchPadFuelService() {
    }

    public static int addFuel(TileEntity pad, FluidTankNTM[] tanks, FluidStack fuel, boolean doFill) {
        if (!canAcceptFuel(pad, fuel)) {
            return 0;
        }

        int remaining = fuel.amount;
        int filled = 0;
        FluidType storageType = getStorageFuelType();
        boolean changed = ensureGCFuelTankTypes(tanks);
        for (FluidTankNTM tank : tanks) {
            if (remaining <= 0) {
                break;
            }

            int accepted = tank.fill(storageType, remaining, doFill);
            filled += accepted;
            remaining -= accepted;
        }

        if (doFill && (filled > 0 || changed)) {
            syncPad(pad);
        }

        return filled;
    }

    public static FluidStack removeFuel(TileEntity pad, FluidTankNTM[] tanks, int amount) {
        if (!HbmGalacticraftEnergy.isBasicLaunchPad(pad) || amount <= 0) {
            return null;
        }

        FluidStack removed = null;
        int remaining = amount;
        for (FluidTankNTM tank : tanks) {
            if (remaining <= 0) {
                break;
            }

            FluidStack drained = tank.drain(remaining, true);
            if (drained == null || drained.amount <= 0) {
                continue;
            }

            if (removed == null) {
                removed = drained.copy();
            } else if (removed.isFluidEqual(drained)) {
                removed.amount += drained.amount;
            }

            remaining -= drained.amount;
        }

        return removed;
    }

    public static FluidTankNTM[] getReceivingTanks(TileEntity pad, FluidTankNTM[] tanks) {
        if (!HbmGalacticraftEnergy.isBasicLaunchPad(pad) || findRocket(pad) == null) {
            return NO_RECEIVING_TANKS;
        }

        if (ensureGCFuelTankTypes(tanks)) {
            syncPad(pad);
        }
        return tanks;
    }

    public static long getDemand(TileEntity pad, FluidTankNTM[] tanks, FluidType type) {
        if (!canAcceptFluidType(pad, type)) {
            return -1L;
        }

        if (ensureGCFuelTankTypes(tanks)) {
            syncPad(pad);
        }

        long demand = 0L;
        for (FluidTankNTM tank : tanks) {
            if (tank.getTankType() == Fluids.NONE || tank.getTankType() == type) {
                demand += Math.max(0, tank.getMaxFill() - tank.getFill());
            }
        }

        return demand;
    }

    public static long transferFluid(TileEntity pad, FluidTankNTM[] tanks, FluidType type, long pressure) {
        if (!canAcceptFluidType(pad, type) || pressure <= 0L) {
            return -1L;
        }

        boolean changed = ensureGCFuelTankTypes(tanks);

        long remaining = pressure;
        for (FluidTankNTM tank : tanks) {
            if (remaining <= 0L) {
                break;
            }

            int accepted = tank.fill(type, clampToInt(remaining), true);
            remaining -= accepted;
        }

        if (changed || remaining != pressure) {
            syncPad(pad);
        }

        return remaining;
    }

    public static void prepareFuelTanks(TileEntity pad, FluidTankNTM[] tanks) {
        if (!HbmGalacticraftEnergy.isBasicLaunchPad(pad)) {
            return;
        }

        if (findRocket(pad) != null) {
            if (ensureGCFuelTankTypes(tanks)) {
                logTankState(pad, "prepared GC fuel tanks", tanks);
                syncPad(pad);
            }
        } else {
            if (resetEmptyGCFuelTanks(tanks)) {
                logTankState(pad, "reset empty GC fuel tanks", tanks);
                syncPad(pad);
            }
        }
    }

    public static long fuelRocketFromPad(TileEntity pad, FluidTankNTM[] tanks, long power) {
        if (!HbmGalacticraftEnergy.isBasicLaunchPad(pad) || pad.getWorld() == null || pad.getWorld().isRemote) {
            return 0L;
        }

        EntityAutoRocket rocket = findRocket(pad);
        if (rocket == null) {
            return 0L;
        }

        if (ensureGCFuelTankTypes(tanks)) {
            logTankState(pad, "prepared GC fuel tanks before rocket fueling", tanks);
            syncPad(pad);
        }

        if (power < HE_PER_MB_FUEL_LOADED) {
            return 0L;
        }

        FluidStack available = drainPadFuel(tanks, TRANSFER_PER_TICK, false);
        if (available == null || available.amount <= 0) {
            return 0L;
        }

        int affordable = (int) Math.min(available.amount, power / HE_PER_MB_FUEL_LOADED);
        if (affordable <= 0) {
            return 0L;
        }

        FluidStack offered = new FluidStack(available.getFluid(), affordable);
        int accepted = rocket.addFuel(offered, true);
        if (accepted <= 0) {
            return 0L;
        }

        FluidStack drained = drainPadFuel(tanks, accepted, true);
        if (drained != null && drained.amount > 0) {
            syncPad(pad);
            return (long) drained.amount * HE_PER_MB_FUEL_LOADED;
        }

        return 0L;
    }

    private static boolean canAcceptFuel(TileEntity pad, FluidStack fuel) {
        return HbmGalacticraftEnergy.isBasicLaunchPad(pad)
                && findRocket(pad) != null
                && isGalacticraftFuel(fuel)
                && getStorageFuelType() != Fluids.NONE;
    }

    private static boolean canAcceptFluidType(TileEntity pad, FluidType type) {
        return HbmGalacticraftEnergy.isBasicLaunchPad(pad)
                && findRocket(pad) != null
                && type != null
                && (type == getStorageFuelType() || type == getMappedGCFuelType())
                && type != Fluids.NONE;
    }

    private static boolean isGalacticraftFuel(FluidStack fuel) {
        if (fuel == null || fuel.getFluid() == null || fuel.amount <= 0) {
            return false;
        }

        Fluid fluid = fuel.getFluid();
        return fluid == GCFluids.fluidFuel
                || fluid == FluidRegistry.getFluid("fuel")
                || fluid == FluidRegistry.getFluid("fuelgc")
                || "fuel".equals(fluid.getName())
                || "fuelgc".equals(fluid.getName());
    }

    public static boolean hasRocket(TileEntity pad) {
        return findRocket(pad) != null;
    }

    private static EntityAutoRocket findRocket(TileEntity pad) {
        return HbmRocketFuelRelay.findRocketOnFullLaunchPad(pad);
    }

    private static FluidStack drainPadFuel(FluidTankNTM[] tanks, int amount, boolean doDrain) {
        FluidStack removed = null;
        int remaining = amount;
        for (FluidTankNTM tank : tanks) {
            if (remaining <= 0) {
                break;
            }

            if (!isStorageFuelTank(tank) || tank.getFill() <= 0) {
                continue;
            }

            int drainedAmount = Math.min(remaining, tank.getFill());
            if (doDrain) {
                tank.setFill(tank.getFill() - drainedAmount);
            }

            FluidStack drained = new FluidStack(getGalacticraftFuelFluid(), drainedAmount);
            if (removed == null) {
                removed = drained.copy();
            } else {
                removed.amount += drained.amount;
            }

            remaining -= drained.amount;
        }

        return removed;
    }

    private static boolean ensureGCFuelTankTypes(FluidTankNTM[] tanks) {
        FluidType gcFuel = getStorageFuelType();
        if (gcFuel == Fluids.NONE) {
            LOG.warn("Could not prepare HBM launch pad tanks because no HBM storage fuel type is available.");
            return false;
        }

        boolean changed = false;
        for (FluidTankNTM tank : tanks) {
            if (tank.getTankType() == Fluids.NONE || tank.getFill() <= 0) {
                if (tank.getTankType() != gcFuel) {
                    changed = true;
                }
                tank.setTankType(gcFuel);
            }
        }

        return changed;
    }

    private static boolean resetEmptyGCFuelTanks(FluidTankNTM[] tanks) {
        FluidType gcFuel = getStorageFuelType();
        boolean changed = false;
        for (FluidTankNTM tank : tanks) {
            if (tank.getFill() <= 0 && tank.getTankType() == gcFuel) {
                tank.resetTank();
                changed = true;
            }
        }

        return changed;
    }

    private static FluidType getStorageFuelType() {
        FluidType registered = HbmGalacticraftFluids.getGCFuelType();
        if (registered != Fluids.NONE) {
            return registered;
        }

        FluidType mapped = getMappedGCFuelType();
        return mapped == Fluids.NONE ? Fluids.NONE : mapped;
    }

    private static FluidType getMappedGCFuelType() {
        if (GCFluids.fluidFuel == null) {
            return Fluids.NONE;
        }

        FluidType type = NTMFluidCapabilityHandler.getFluidType(GCFluids.fluidFuel);
        return type == null ? Fluids.NONE : type;
    }

    private static Fluid getGalacticraftFuelFluid() {
        return HbmGalacticraftFluids.getGCFuelFluid();
    }

    private static boolean isStorageFuelTank(FluidTankNTM tank) {
        return tank.getTankType() == getStorageFuelType();
    }

    private static int clampToInt(long amount) {
        if (amount <= 0L) {
            return 0;
        }

        return amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
    }

    private static void syncPad(TileEntity pad) {
        if (pad == null || pad.getWorld() == null || pad.getWorld().isRemote) {
            return;
        }

        pad.markDirty();
        if (pad instanceof TileEntityLoadedBase) {
            ((TileEntityLoadedBase) pad).markChanged();
        }
    }

    private static void logTankState(TileEntity pad, String action, FluidTankNTM[] tanks) {
        if (pad == null || pad.getWorld() == null || pad.getWorld().isRemote) {
            return;
        }

        StringBuilder message = new StringBuilder();
        message.append("Full launch pad ").append(action).append(": pad=").append(pad.getPos());
        for (int i = 0; i < tanks.length; i++) {
            FluidTankNTM tank = tanks[i];
            message.append(" tank").append(i)
                    .append("=")
                    .append(tank.getTankType().getName())
                    .append(" ")
                    .append(tank.getFill())
                    .append("/")
                    .append(tank.getMaxFill());
        }

        LOG.info(message.toString());
    }
}
