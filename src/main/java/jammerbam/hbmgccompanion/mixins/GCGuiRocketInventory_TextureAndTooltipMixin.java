package jammerbam.hbmgccompanion.mixins;

import jammerbam.hbmgccompanion.compat.galacticraft.GalacticraftLaunchPadReturnService;
import jammerbam.hbmgccompanion.compat.galacticraft.LandingPadSlot;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntityAutoRocket;
import micdoodle8.mods.galacticraft.core.client.gui.container.GuiContainerGC;
import micdoodle8.mods.galacticraft.core.client.gui.container.GuiRocketInventory;
import micdoodle8.mods.galacticraft.core.client.gui.element.GuiElementInfoRegion;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
@Mixin(value = GuiRocketInventory.class, remap = false)
abstract class GCGuiRocketInventory_TextureAndTooltipMixin extends GuiContainer {
    @Unique
    private static final ResourceLocation[] hbmgc$ROCKET_TEXTURES = new ResourceLocation[] {
            new ResourceLocation("hbmgccompanion", "textures/gui/galacticraft/rocket_0.png"),
            new ResourceLocation("hbmgccompanion", "textures/gui/galacticraft/rocket_18.png"),
            new ResourceLocation("hbmgccompanion", "textures/gui/galacticraft/rocket_36.png"),
            new ResourceLocation("hbmgccompanion", "textures/gui/galacticraft/rocket_54.png")
    };

    private GCGuiRocketInventory_TextureAndTooltipMixin() {
        super(null);
    }

    @ModifyArg(
            method = "func_146976_a(FII)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/texture/TextureManager;func_110577_a(Lnet/minecraft/util/ResourceLocation;)V"
            ),
            index = 0
    )
    private ResourceLocation hbmgc$useHbmPadTextureWhenApplicable(ResourceLocation originalTexture) {
        if (!this.hbmgc$isCurrentRocketOnHbmPad()) {
            return originalTexture;
        }

        String path = originalTexture.toString();
        if (path.endsWith("rocket_18.png")) {
            return hbmgc$ROCKET_TEXTURES[1];
        }
        if (path.endsWith("rocket_36.png")) {
            return hbmgc$ROCKET_TEXTURES[2];
        }
        if (path.endsWith("rocket_54.png")) {
            return hbmgc$ROCKET_TEXTURES[3];
        }

        return hbmgc$ROCKET_TEXTURES[0];
    }

    @ModifyArg(
            method = "func_146979_b(II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lmicdoodle8/mods/galacticraft/core/util/GCCoreUtil;translate(Ljava/lang/String;)Ljava/lang/String;",
                    ordinal = 0
            ),
            index = 0
    )
    private String hbmgc$renameFuelLabelForHbmPadGui(String key) {
        if (this.hbmgc$isCurrentRocketOnHbmPad() && "gui.message.fuel.name".equals(key)) {
            return "hbmgc.rocket_landing_pad_slot.label";
        }

        return key;
    }

    @ModifyArg(
            method = "func_146979_b(II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/FontRenderer;func_78276_b(Ljava/lang/String;III)I",
                    ordinal = 0
            ),
            index = 1
    )
    private int hbmgc$moveLaunchPadsLabelLeft(int x) {
        return this.hbmgc$isCurrentRocketOnHbmPad() ? x - 2 : x;
    }

    @Inject(method = "func_73866_w_", at = @At("RETURN"))
    private void hbmgc$addLandingPadSlotTooltip(CallbackInfo ci) {
        if (!this.hbmgc$isCurrentRocketOnHbmPad()) {
            return;
        }

        List<String> tooltip = new ArrayList<String>();
        tooltip.add(GCCoreUtil.translate("hbmgc.rocket_landing_pad_slot.desc.0"));
        tooltip.add(GCCoreUtil.translate("hbmgc.rocket_landing_pad_slot.desc.1"));

        int guiLeft = (this.width - this.xSize) / 2;
        int guiTop = (this.height - this.ySize) / 2;
        GuiContainerGC parent = (GuiContainerGC) (Object) this;
        parent.infoRegions.add(new GuiElementInfoRegion(
                guiLeft + LandingPadSlot.X,
                guiTop + LandingPadSlot.Y,
                18,
                18,
                tooltip,
                this.width,
                this.height,
                parent
        ));
    }

    @Unique
    private boolean hbmgc$isCurrentRocketOnHbmPad() {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.player == null) {
            return false;
        }

        Entity riding = minecraft.player.getRidingEntity();
        return riding instanceof EntityAutoRocket
                && GalacticraftLaunchPadReturnService.isRocketOnHbmPad((EntityAutoRocket) riding);
    }
}
