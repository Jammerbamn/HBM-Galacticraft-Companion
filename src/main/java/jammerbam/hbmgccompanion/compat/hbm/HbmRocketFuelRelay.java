package jammerbam.hbmgccompanion.compat.hbm;

import com.hbm.blocks.BlockDummyable;
import com.hbm.tileentity.bomb.TileEntityLaunchPadRusted;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntityAutoRocket;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public final class HbmRocketFuelRelay {
    private HbmRocketFuelRelay() {
    }

    public static TileEntity resolveAdjacentFuelableCore(World world, BlockPos origin, EnumFacing side, TileEntity direct) {
        if (direct != null) {
            return direct;
        }

        BlockPos adjacentPos = origin.offset(side);
        Block block = world.getBlockState(adjacentPos).getBlock();
        if (block instanceof BlockDummyable) {
            return ((BlockDummyable) block).findCoreTE(world, adjacentPos);
        }

        return null;
    }

    public static int addFuel(TileEntityLaunchPadRusted pad, FluidStack fuel, boolean doFill) {
        EntityAutoRocket rocket = findRocket(pad);
        return rocket == null ? 0 : rocket.addFuel(fuel, doFill);
    }

    public static FluidStack removeFuel(TileEntityLaunchPadRusted pad, int amount) {
        EntityAutoRocket rocket = findRocket(pad);
        return rocket == null ? null : rocket.removeFuel(amount);
    }

    public static EntityAutoRocket findRocket(TileEntity pad) {
        return findRocket(pad, 1.0D, 2.0D, 0.0D, 8.0D);
    }

    public static EntityAutoRocket findRocketOnFullLaunchPad(TileEntity pad) {
        return findRocket(pad, 3.0D, 4.0D, -2.0D, 14.0D);
    }

    private static EntityAutoRocket findRocket(TileEntity pad, double minHorizontal, double maxHorizontal, double minY, double maxY) {
        if (pad == null || pad.getWorld() == null) {
            return null;
        }

        AxisAlignedBB bounds = getRocketBounds(pad.getPos(), minHorizontal, maxHorizontal, minY, maxY);
        List<EntityAutoRocket> rockets = pad.getWorld().getEntitiesWithinAABB(
                EntityAutoRocket.class,
                bounds
        );

        return rockets.isEmpty() ? null : rockets.get(0);
    }

    private static AxisAlignedBB getRocketBounds(
            BlockPos corePos,
            double minHorizontal,
            double maxHorizontal,
            double minY,
            double maxY
    ) {
        return new AxisAlignedBB(
                corePos.getX() - minHorizontal,
                corePos.getY() + minY,
                corePos.getZ() - minHorizontal,
                corePos.getX() + maxHorizontal,
                corePos.getY() + maxY,
                corePos.getZ() + maxHorizontal
        );
    }
}
