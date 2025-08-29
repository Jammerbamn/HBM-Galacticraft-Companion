package jammerbam.hbmgccompanion.mixins;

import micdoodle8.mods.galacticraft.core.client.gui.container.GuiRefinery;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mixin(value = GuiRefinery.class, remap = false)
abstract class GuiRefinery_KeroseneTextMixin {

    // Background layer = drawGuiContainerBackgroundLayer(float, int, int) → (FII)V
    @ModifyArg(
            method = "func_146976_a(FII)V",
            at = @At(value = "INVOKE",
                    target = "Lmicdoodle8/mods/galacticraft/core/util/GCCoreUtil;translate(Ljava/lang/String;)Ljava/lang/String;"),
            index = 0
    )
    private String hbmgc$replaceOilKeysBG(String key) {
        if ("gui.message.oil.name".equals(key)) return "hbmgc.message.kerosene.name";
        if ("gui.oil_tank.desc.0".equals(key))  return "hbmgc.kerosene_tank.desc.0";
        if ("gui.oil_tank.desc.1".equals(key))  return "hbmgc.kerosene_tank.desc.1";
        return key;
    }

    // Foreground layer = drawGuiContainerForegroundLayer(int, int) → (II)V
    @ModifyArg(
            method = "func_146979_b(II)V",
            at = @At(value = "INVOKE",
                    target = "Lmicdoodle8/mods/galacticraft/core/util/GCCoreUtil;translate(Ljava/lang/String;)Ljava/lang/String;"),
            index = 0
    )
    private String hbmgc$replaceOilKeysFG(String key) {
        if ("gui.status.nooil.name".equals(key)) return "hbmgc.status.nokerosene.name";
        if ("gui.message.oil.name".equals(key))  return "hbmgc.message.kerosene.name";
        return key;
    }
}
