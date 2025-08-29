package jammerbam.hbmgccompanion.mixins;

import micdoodle8.mods.galacticraft.core.client.gui.container.GuiRefinery;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mixin(value = GuiRefinery.class, remap = false)
abstract class GuiRefinery_TextureSwapMixin {

    // Shadow the target's private static final field so we can reassign it.
    @Shadow @Final @Mutable
    private static ResourceLocation refineryTexture;

    // Run after the class’s static initializer finishes
    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void hbmgc$swapRefineryTexture(CallbackInfo ci) {
        // Point it at your texture (domain = your modid)
        refineryTexture = new ResourceLocation("hbmgccompanion", "textures/gui/refinery.png");
    }
}

