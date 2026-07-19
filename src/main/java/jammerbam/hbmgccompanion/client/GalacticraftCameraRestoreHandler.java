package jammerbam.hbmgccompanion.client;

import micdoodle8.mods.galacticraft.api.entity.ICameraZoomEntity;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntityAutoRocket;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntitySpaceshipBase;
import micdoodle8.mods.galacticraft.core.entities.EntityLander;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class GalacticraftCameraRestoreHandler {
    private boolean wasRidingGalacticraftCameraEntity;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.player == null || minecraft.gameSettings == null) {
            this.wasRidingGalacticraftCameraEntity = false;
            return;
        }

        boolean isRidingGalacticraftCameraEntity = isGalacticraftCameraEntity(minecraft.player.getRidingEntity());
        if (this.wasRidingGalacticraftCameraEntity && !isRidingGalacticraftCameraEntity) {
            minecraft.gameSettings.thirdPersonView = 0;
        }

        this.wasRidingGalacticraftCameraEntity = isRidingGalacticraftCameraEntity;
    }

    private static boolean isGalacticraftCameraEntity(Entity entity) {
        return entity instanceof EntitySpaceshipBase
                || entity instanceof EntityAutoRocket
                || entity instanceof EntityLander
                || entity instanceof ICameraZoomEntity && isGalacticraftEntity(entity);
    }

    private static boolean isGalacticraftEntity(Entity entity) {
        return entity.getClass().getName().startsWith("micdoodle8.mods.galacticraft.");
    }
}
