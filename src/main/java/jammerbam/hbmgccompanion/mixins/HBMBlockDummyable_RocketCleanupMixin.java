package jammerbam.hbmgccompanion.mixins;

import com.hbm.blocks.BlockDummyable;
import jammerbam.hbmgccompanion.compat.hbm.HbmRocketInventoryLaunchService;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlockDummyable.class, remap = false)
abstract class HBMBlockDummyable_RocketCleanupMixin {
    @Inject(method = "breakBlock", at = @At("HEAD"))
    private void hbmgc$destroyRocketWhenDummyablePadBreaks(World world, BlockPos pos, IBlockState state, CallbackInfo ci) {
        HbmRocketInventoryLaunchService.destroyRocketForPadBlock(world, pos, state);
    }
}
