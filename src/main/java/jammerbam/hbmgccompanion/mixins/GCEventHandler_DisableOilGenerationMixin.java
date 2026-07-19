package jammerbam.hbmgccompanion.mixins;

import micdoodle8.mods.galacticraft.core.event.EventHandlerGC;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(value = EventHandlerGC.class, remap = false)
abstract class GCEventHandler_DisableOilGenerationMixin {
    @Inject(method = "generateOil", at = @At("HEAD"), cancellable = true)
    private static void hbmgc$disableGalacticraftOilGeneration(
            World world,
            Random random,
            int chunkX,
            int chunkZ,
            boolean retrogen,
            CallbackInfo ci
    ) {
        ci.cancel();
    }
}
