package jammerbam.hbmgccompanion.compat;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public final class HBMGCFluids {
    public static final String KEROSENE = "kerosene";

    private HBMGCFluids() {
    }

    public static Fluid getKerosene() {
        return FluidRegistry.getFluid(KEROSENE);
    }

    public static boolean isKerosene(FluidStack stack) {
        Fluid kerosene = getKerosene();
        return stack != null && kerosene != null && stack.getFluid() == kerosene;
    }

    public static boolean isKeroseneName(String name) {
        return KEROSENE.equals(name);
    }
}
