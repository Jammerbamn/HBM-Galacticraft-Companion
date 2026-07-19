package jammerbam.hbmgccompanion.mixins;

import com.hbm.blocks.bomb.LaunchPadRusted;
import jammerbam.hbmgccompanion.compat.rocket.RocketPlacementService;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LaunchPadRusted.class, remap = false)
abstract class HBMRustedLaunchPad_RocketPlacementMixin {

    @Inject(method = "func_180639_a", at = @At("HEAD"), cancellable = true)
    private void hbmgc$placeTier1Rocket(
            World world,
            BlockPos pos,
            IBlockState state,
            EntityPlayer player,
            EnumHand hand,
            EnumFacing facing,
            float hitX,
            float hitY,
            float hitZ,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (RocketPlacementService.tryPlaceRocket(world, pos, player, hand)) {
            cir.setReturnValue(true);
        }
    }
}
