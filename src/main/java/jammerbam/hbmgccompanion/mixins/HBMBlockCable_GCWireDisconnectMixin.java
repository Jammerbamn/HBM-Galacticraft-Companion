package jammerbam.hbmgccompanion.mixins;

import com.hbm.blocks.network.energy.BlockCable;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.network.energy.TileEntityCableBaseNT;
import micdoodle8.mods.galacticraft.api.transmission.tile.IConductor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BlockCable.class, remap = false)
abstract class HBMBlockCable_GCWireDisconnectMixin {

    @Inject(method = "computeConnectToNeighbor", at = @At("HEAD"), cancellable = true)
    private static void hbmgc$skipGCWires(
            IBlockAccess world,
            BlockPos pos,
            ForgeDirection direction,
            CallbackInfoReturnable<Boolean> cir
    ) {
        TileEntity adjacent = world.getTileEntity(pos);
        if (adjacent instanceof IConductor && !(adjacent instanceof TileEntityCableBaseNT)) {
            cir.setReturnValue(false);
        }
    }
}
