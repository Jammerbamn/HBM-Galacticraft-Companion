package jammerbam.hbmgccompanion.compat.hbm;

import micdoodle8.mods.galacticraft.api.prefab.entity.EntityAutoRocket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import jammerbam.hbmgccompanion.compat.galacticraft.RocketPadInventorySource;

import java.util.List;

public final class HbmRocketFuelRelay {
    private HbmRocketFuelRelay() {
    }

    public static TileEntity resolveAdjacentFuelableCore(World world, BlockPos origin, EnumFacing side, TileEntity direct) {
        if (world == null || origin == null || side == null) {
            return direct;
        }

        BlockPos adjacentPos = origin.offset(side);
        TileEntity padCore = HbmRocketPadAdapters.findPadTileNear(world, adjacentPos, 2, 1);
        if (padCore != null) {
            return padCore;
        }

        return direct;
    }

    public static int addFuel(TileEntity pad, FluidStack fuel, boolean doFill) {
        EntityAutoRocket rocket = findRocket(pad);
        if (rocket == null) {
            HbmRocketInventoryLaunchService.syncPad(pad);
            rocket = findRocket(pad);
        }

        return rocket == null ? 0 : rocket.addFuel(fuel, doFill);
    }

    public static FluidStack removeFuel(TileEntity pad, int amount) {
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

        for (EntityAutoRocket rocket : rockets) {
            if (rocket instanceof RocketPadInventorySource) {
                RocketPadInventorySource source = (RocketPadInventorySource) rocket;
                if (source.hbmgc$hasPadInventorySource()
                        && source.hbmgc$getPadInventorySourceDimension() == pad.getWorld().provider.getDimension()
                        && pad.getPos().equals(source.hbmgc$getPadInventorySourcePos())) {
                    return rocket;
                }
            }
        }

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
