package jammerbam.hbmgccompanion.compat.galacticraft;

import micdoodle8.mods.galacticraft.api.prefab.entity.EntityAutoRocket;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public final class HbmLaunchVentParticleService {
    private static final double VENT_OFFSET = 0.52D;
    private static final double VENT_Y_OFFSET = 0.62D;
    private static final double VENT_EXHAUST_SPEED = 0.62D;
    private static final int SIDE_VENT_LINGER_TICKS = 40;
    private static final int CENTER_PLUME_LINGER_TICKS = 70;

    private HbmLaunchVentParticleService() {
    }

    public static boolean replaceCountdownParticles(EntityAutoRocket rocket, boolean launched) {
        if (rocket == null || rocket.isDead || !isRocketOnHbmPad(rocket)) {
            return false;
        }

        if (launched) {
            spawnLiftoffParticles(rocket);
            return false;
        }

        if (rocket.ticksExisted % 2 == 0) {
            spawnSideVentParticles(rocket);
        }

        return true;
    }

    private static boolean isRocketOnHbmPad(EntityAutoRocket rocket) {
        return hasPadSourceInThisWorld(rocket) || GalacticraftLaunchPadReturnService.isRocketOnHbmPad(rocket);
    }

    private static boolean hasPadSourceInThisWorld(EntityAutoRocket rocket) {
        if (!(rocket instanceof RocketPadInventorySource)) {
            return false;
        }

        RocketPadInventorySource source = (RocketPadInventorySource) rocket;
        return source.hbmgc$hasPadInventorySource()
                && rocket.getEntityWorld() != null
                && rocket.getEntityWorld().provider.getDimension() == source.hbmgc$getPadInventorySourceDimension();
    }

    private static void spawnSideVentParticles(EntityAutoRocket rocket) {
        BlockPos padPos = getPadPos(rocket);
        double centerX = padPos.getX() + 0.5D;
        double centerY = padPos.getY() + VENT_Y_OFFSET;
        double centerZ = padPos.getZ() + 0.5D;
        EntityLivingBase rider = getLivingRider(rocket);

        spawnVent(rocket, centerX + VENT_OFFSET, centerY, centerZ, VENT_EXHAUST_SPEED, 0.04D, 0.0D, rider);
        spawnVent(rocket, centerX - VENT_OFFSET, centerY, centerZ, -VENT_EXHAUST_SPEED, 0.04D, 0.0D, rider);
        spawnVent(rocket, centerX, centerY, centerZ + VENT_OFFSET, 0.0D, 0.04D, VENT_EXHAUST_SPEED, rider);
        spawnVent(rocket, centerX, centerY, centerZ - VENT_OFFSET, 0.0D, 0.04D, -VENT_EXHAUST_SPEED, rider);
    }

    private static void spawnLiftoffParticles(EntityAutoRocket rocket) {
        if (rocket.timeSinceLaunch <= SIDE_VENT_LINGER_TICKS && rocket.ticksExisted % 2 == 0) {
            spawnSideVentParticles(rocket);
        }

        if (rocket.timeSinceLaunch <= CENTER_PLUME_LINGER_TICKS) {
            spawnCenterPlumeParticles(rocket);
        }
    }

    private static void spawnCenterPlumeParticles(EntityAutoRocket rocket) {
        BlockPos padPos = getPadPos(rocket);
        World world = rocket.getEntityWorld();
        double centerX = padPos.getX() + 0.5D;
        double centerY = padPos.getY() + VENT_Y_OFFSET;
        double centerZ = padPos.getZ() + 0.5D;
        EntityLivingBase rider = getLivingRider(rocket);

        double jitterX = (world.rand.nextDouble() - 0.5D) * 0.34D;
        double jitterZ = (world.rand.nextDouble() - 0.5D) * 0.34D;
        Vector3 position = new Vector3(centerX + jitterX, centerY, centerZ + jitterZ);
        Vector3 motion = new Vector3(jitterX * 0.08D, 0.34D + world.rand.nextDouble() * 0.12D, jitterZ * 0.08D);

        GalacticraftCore.proxy.spawnParticle("whiteSmokeLargeLaunched", position, motion, new Object[0]);

        if (rocket.ticksExisted % 2 == 0) {
            GalacticraftCore.proxy.spawnParticle("launchFlameLaunched", position, motion, new Object[] { rider });
        }
    }

    private static BlockPos getPadPos(EntityAutoRocket rocket) {
        if (rocket instanceof RocketPadInventorySource) {
            RocketPadInventorySource source = (RocketPadInventorySource) rocket;
            if (source.hbmgc$hasPadInventorySource()) {
                return source.hbmgc$getPadInventorySourcePos();
            }
        }

        return new BlockPos(
                MathHelper.floor(rocket.posX),
                MathHelper.floor(rocket.posY - 1.0D),
                MathHelper.floor(rocket.posZ)
        );
    }

    private static EntityLivingBase getLivingRider(EntityAutoRocket rocket) {
        if (!rocket.getPassengers().isEmpty() && rocket.getPassengers().get(0) instanceof EntityLivingBase) {
            return (EntityLivingBase) rocket.getPassengers().get(0);
        }

        return null;
    }

    private static void spawnVent(
            EntityAutoRocket rocket,
            double x,
            double y,
            double z,
            double motionX,
            double motionY,
            double motionZ,
            EntityLivingBase rider
    ) {
        World world = rocket.getEntityWorld();
        double jitterX = (world.rand.nextDouble() - 0.5D) * 0.08D;
        double jitterZ = (world.rand.nextDouble() - 0.5D) * 0.08D;
        Vector3 position = new Vector3(x + jitterX, y, z + jitterZ);
        Vector3 motion = new Vector3(
                motionX + jitterX * 0.4D,
                motionY + world.rand.nextDouble() * 0.03D,
                motionZ + jitterZ * 0.4D
        );

        GalacticraftCore.proxy.spawnParticle("whiteSmokeLargeLaunched", position, motion, new Object[0]);

        if (rocket.ticksExisted % 4 == 0) {
            GalacticraftCore.proxy.spawnParticle("launchFlameLaunched", position, motion, new Object[] { rider });
        }
    }
}
