package jammerbam.hbmgccompanion.client;

import micdoodle8.mods.galacticraft.api.prefab.entity.EntityAutoRocket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class GalacticraftRocketGuiPreviewRenderer {
    public static final int HBM_PREVIEW_SCALE = 10;

    private GalacticraftRocketGuiPreviewRenderer() {
    }

    public static void render(EntityAutoRocket rocket, int x, int y, int scale, float partialTicks) {
        if (rocket == null) {
            return;
        }

        float rotationYaw = rocket.rotationYaw;
        float prevRotationYaw = rocket.prevRotationYaw;
        float rotationPitch = rocket.rotationPitch;
        float prevRotationPitch = rocket.prevRotationPitch;
        float spin = getPreviewSpin(partialTicks);

        try {
            rocket.rotationYaw = 0.0F;
            rocket.prevRotationYaw = 0.0F;
            rocket.rotationPitch = 0.0F;
            rocket.prevRotationPitch = 0.0F;

            GlStateManager.enableTexture2D();
            GlStateManager.enableColorMaterial();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) x, (float) y, 100.0F);
            GlStateManager.scale((float) scale, (float) scale, (float) scale);
            GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
            RenderHelper.enableStandardItemLighting();
            GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(12.5F, 1.0F, 0.0F, 0.0F);

            RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
            renderManager.setPlayerViewY(180.0F);
            renderManager.setRenderShadow(false);
            renderManager.renderEntity(rocket, 0.0D, 0.0D, 0.0D, spin, partialTicks, false);
            renderManager.setRenderShadow(true);

            GlStateManager.popMatrix();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.disableTexture2D();
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.enableTexture2D();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        } finally {
            rocket.rotationYaw = rotationYaw;
            rocket.prevRotationYaw = prevRotationYaw;
            rocket.rotationPitch = rotationPitch;
            rocket.prevRotationPitch = prevRotationPitch;
        }
    }

    private static float getPreviewSpin(float partialTicks) {
        Minecraft minecraft = Minecraft.getMinecraft();
        long ticks = minecraft.world == null ? 0L : minecraft.world.getTotalWorldTime();
        return (ticks + partialTicks) * 1.25F;
    }
}
