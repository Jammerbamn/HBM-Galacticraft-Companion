package jammerbam.hbmgccompanion.mixins;

import com.hbm.inventory.gui.GUILaunchPadRusted;
import com.hbm.tileentity.bomb.TileEntityLaunchPadRusted;
import jammerbam.hbmgccompanion.client.GalacticraftRocketGuiPreviewRenderer;
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
@Mixin(value = GUILaunchPadRusted.class, remap = false)
abstract class HBMLaunchPadRusted_GCRocketPreviewMixin extends GuiContainer {
    @Shadow
    private TileEntityLaunchPadRusted launchpad;

    private HBMLaunchPadRusted_GCRocketPreviewMixin() {
        super(null);
    }

    @Inject(method = { "drawGuiContainerBackgroundLayer", "func_146976_a" }, at = @At("TAIL"))
    private void hbmgc$renderGalacticraftRocketPreview(float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
        EntityAutoRocket rocket = HbmRocketFuelRelay.findRocket(this.launchpad);
        GalacticraftRocketGuiPreviewRenderer.render(
                rocket,
                this.guiLeft + 70,
                this.guiTop + 110,
                GalacticraftRocketGuiPreviewRenderer.HBM_PREVIEW_SCALE,
                partialTicks
        );
    }
}
