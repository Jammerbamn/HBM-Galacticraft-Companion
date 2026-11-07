package jammerbam.hbmgccompanion.mixins;

import com.hbm.tileentity.network.energy.TileEntityCableBaseNT;
import micdoodle8.mods.galacticraft.api.transmission.tile.IConductor;
import micdoodle8.mods.galacticraft.api.transmission.tile.IElectrical;
import micdoodle8.mods.galacticraft.core.tile.TileEntityAdvanced;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TileEntityAdvanced.class, remap = false)
abstract class GCTileEntityAdvanced_HBMCableRefreshMixin {

    @Shadow
    public int ticks;

    @Inject(method = { "update", "func_73660_a" }, at = @At("HEAD"))
    private void hbmgc$refreshAdjacentCopperOnFirstTick(CallbackInfo ci) {
        TileEntity self = (TileEntity) (Object) this;
        World world = self.getWorld();
        if (this.ticks != 0 || world == null || !world.isRemote || !(self instanceof IElectrical)) {
            return;
        }

        for (EnumFacing side : EnumFacing.VALUES) {
            BlockPos neighborPos = self.getPos().offset(side);
            if (!world.isBlockLoaded(neighborPos)) {
                continue;
            }

            TileEntity neighbor = world.getTileEntity(neighborPos);
            if (neighbor instanceof TileEntityCableBaseNT && neighbor instanceof IConductor
                    && ((IConductor) neighbor).canTransmit()) {
                ((IConductor) neighbor).onNetworkChanged();
                ((TileEntityCableBaseNT) neighbor).invalidateConnectionCache();
            }
        }
    }
}
