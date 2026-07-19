package jammerbam.hbmgccompanion.mixins;

import jammerbam.hbmgccompanion.compat.hbm.HbmRocketFuelRelay;
import micdoodle8.mods.galacticraft.api.vector.BlockVec3;
import micdoodle8.mods.galacticraft.core.tile.TileEntityFuelLoader;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = TileEntityFuelLoader.class, remap = false)
abstract class GCFluidLoader_HBMDummyCoreMixin {

    @Redirect(
            method = "update",
            at = @At(
                    value = "INVOKE",
                    target = "Lmicdoodle8/mods/galacticraft/api/vector/BlockVec3;getTileEntityOnSide(Lnet/minecraft/world/World;Lnet/minecraft/util/EnumFacing;)Lnet/minecraft/tileentity/TileEntity;"
            )
    )
    private TileEntity hbmgc$resolveHBMDummyCore(BlockVec3 loaderPos, World world, EnumFacing side) {
        TileEntity direct = loaderPos.getTileEntityOnSide(world, side);
        return HbmRocketFuelRelay.resolveAdjacentFuelableCore(world, new BlockPos(loaderPos.x, loaderPos.y, loaderPos.z), side, direct);
    }
}
