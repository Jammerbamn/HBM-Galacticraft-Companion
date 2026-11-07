package jammerbam.hbmgccompanion.mixins;

import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.tileentity.network.energy.TileEntityCableBaseNT;
import micdoodle8.mods.galacticraft.api.transmission.NetworkType;
import micdoodle8.mods.galacticraft.api.transmission.tile.IConnector;
import micdoodle8.mods.galacticraft.api.transmission.tile.IConductor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Library.class, remap = false)
abstract class EEHBMLibrary_GCConnectorMixin {

    @Inject(method = "canConnect", at = @At("HEAD"), cancellable = true)
    private static void hbmgc$connectHBMCablesToGCConnectors(
            IBlockAccess world,
            BlockPos pos,
            ForgeDirection direction,
            CallbackInfoReturnable<Boolean> cir
    ) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof IConductor && !(tile instanceof TileEntityCableBaseNT)) {
            cir.setReturnValue(false);
            return;
        }
        if (tile instanceof IConnector
                && ((IConnector) tile).canConnect(direction.getOpposite().toEnumFacing(), NetworkType.POWER)) {
            cir.setReturnValue(true);
        }
    }
}
