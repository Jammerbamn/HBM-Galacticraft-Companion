package jammerbam.hbmgccompanion.mixins;

import com.hbm.inventory.gui.GUILaunchPadLarge;
import com.hbm.tileentity.bomb.TileEntityLaunchPadBase;
import jammerbam.hbmgccompanion.client.GalacticraftRocketGuiPreviewRenderer;
import jammerbam.hbmgccompanion.compat.hbm.HbmGalacticraftEnergy;
import jammerbam.hbmgccompanion.compat.hbm.HbmRocketFuelRelay;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntityAutoRocket;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SideOnly(Side.CLIENT)
@Mixin(value = GUILaunchPadLarge.class, remap = false)
abstract class HBMLaunchPadLarge_GCRocketPreviewMixin extends GuiContainer {
    @Shadow
    private TileEntityLaunchPadBase launchpad;

    private HBMLaunchPadLarge_GCRocketPreviewMixin() {
        super(null);
    }

    @Inject(method = { "drawGuiContainerBackgroundLayer", "func_146976_a" }, at = @At("TAIL"))
    private void hbmgc$renderGalacticraftRocketPreview(float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
        if (!HbmGalacticraftEnergy.isBasicLaunchPad(this.launchpad)) {
            return;
        }

        EntityAutoRocket rocket = HbmRocketFuelRelay.findRocketOnFullLaunchPad(this.launchpad);
        GalacticraftRocketGuiPreviewRenderer.render(
                rocket,
                this.guiLeft + 70,
                this.guiTop + 110,
                GalacticraftRocketGuiPreviewRenderer.HBM_PREVIEW_SCALE,
                partialTicks
        );
    }
}
