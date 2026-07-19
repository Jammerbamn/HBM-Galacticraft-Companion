package jammerbam.hbmgccompanion.compat.hbm;

import com.hbm.api.fluidmk2.IFluidRegisterListener;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.render.misc.EnumSymbol;
import micdoodle8.mods.galacticraft.core.GCFluids;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;

public final class HbmGalacticraftFluids {
    public static final String GC_FUEL_NAME = "GC_FUEL";
    private static final String GC_FUEL_FRIENDLY_NAME = "Galacticraft Fuel";

    private static final Logger LOG = LogManager.getLogger("hbmgccompanion/HBM-GC-Fluids");
    private static final IFluidRegisterListener REGISTER_LISTENER = HbmGalacticraftFluids::registerGCFuelType;

    private static boolean listenerRegistered;
    private static FluidType gcFuelType;

    private HbmGalacticraftFluids() {
    }

    public static void registerFluidListener() {
        if (listenerRegistered || Fluids.additionalListeners == null) {
            return;
        }

        Fluids.additionalListeners.add(REGISTER_LISTENER);
        listenerRegistered = true;
    }

    public static FluidType getGCFuelType() {
        FluidType type = gcFuelType;
        if (type != null && type != Fluids.NONE) {
            return type;
        }

        type = Fluids.fromName(GC_FUEL_NAME);
        if (type != null && type != Fluids.NONE) {
            gcFuelType = type;
            return type;
        }

        return Fluids.NONE;
    }

    public static Fluid getGCFuelFluid() {
        if (GCFluids.fluidFuel != null) {
            return GCFluids.fluidFuel;
        }

        Fluid fuel = FluidRegistry.getFluid("fuelgc");
        return fuel == null ? FluidRegistry.getFluid("fuel") : fuel;
    }

    private static void registerGCFuelType() {
        Fluid forgeFuel = getGCFuelFluid();
        if (forgeFuel == null) {
            LOG.warn("Skipping HBM GC fuel registration because Galacticraft fuel is not registered yet.");
            return;
        }

        FluidType existing = Fluids.fromName(GC_FUEL_NAME);
        if (existing != null && existing != Fluids.NONE) {
            gcFuelType = existing;
            gcFuelType.setFFNameOverride(forgeFuel.getName());
            setFriendlyName(gcFuelType);
            LOG.info("Using existing HBM GC fuel FluidType mapped to Forge fluid '{}'.", forgeFuel.getName());
            return;
        }

        int id = nextFreeFluidId();
        gcFuelType = new FluidType(
                GC_FUEL_NAME,
                id,
                0xD1A136,
                0,
                2,
                0,
                EnumSymbol.NONE,
                new ResourceLocation("galacticraftcore", "textures/blocks/fluids/fuel_still.png")
        ).setFFNameOverride(forgeFuel.getName()).addTraits(Fluids.LIQUID);
        setFriendlyName(gcFuelType);

        LOG.info("Registered HBM GC fuel FluidType id={} mapped to Forge fluid '{}'.", id, forgeFuel.getName());
    }

    private static int nextFreeFluidId() {
        int max = 0;
        for (FluidType type : Fluids.getAll()) {
            if (type != null) {
                max = Math.max(max, type.getID());
            }
        }

        return max + 1;
    }

    private static void setFriendlyName(FluidType type) {
        try {
            Field field = FluidType.class.getDeclaredField("localizedOverride");
            field.setAccessible(true);
            field.set(type, GC_FUEL_FRIENDLY_NAME);
        } catch (ReflectiveOperationException e) {
            LOG.warn("Could not set friendly name for HBM GC fuel FluidType.", e);
        }
    }
}
