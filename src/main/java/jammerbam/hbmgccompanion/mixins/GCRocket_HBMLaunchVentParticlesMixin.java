package jammerbam.hbmgccompanion.mixins;

import jammerbam.hbmgccompanion.compat.galacticraft.HbmLaunchVentParticleService;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntityAutoRocket;
import micdoodle8.mods.galacticraft.core.entities.EntityTier1Rocket;
import micdoodle8.mods.galacticraft.planets.asteroids.entities.EntityTier3Rocket;
import micdoodle8.mods.galacticraft.planets.mars.entities.EntityTier2Rocket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {
        EntityTier1Rocket.class,
        EntityTier2Rocket.class,
        EntityTier3Rocket.class
}, remap = false)
abstract class GCRocket_HBMLaunchVentParticlesMixin {
    @Inject(method = "spawnParticles", at = @At("HEAD"), cancellable = true)
    private void hbmgc$redirectCountdownParticlesToHbmPadVents(boolean launched, CallbackInfo ci) {
        if (HbmLaunchVentParticleService.replaceCountdownParticles((EntityAutoRocket) (Object) this, launched)) {
            ci.cancel();
        }
    }
}
