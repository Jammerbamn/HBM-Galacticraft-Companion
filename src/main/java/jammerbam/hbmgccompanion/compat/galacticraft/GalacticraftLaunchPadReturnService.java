package jammerbam.hbmgccompanion.compat.galacticraft;

import jammerbam.hbmgccompanion.compat.hbm.HbmRocketPadAdapter;
import jammerbam.hbmgccompanion.compat.hbm.HbmRocketPadAdapters;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntityAutoRocket;
import micdoodle8.mods.galacticraft.core.entities.player.GCPlayerStats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public final class GalacticraftLaunchPadReturnService {
    private GalacticraftLaunchPadReturnService() {
    }

    public static void recordPadReturnForHbmLaunch(EntityAutoRocket rocket) {
        World world = rocket.getEntityWorld();
        if (world.isRemote || !isRocketOnHbmPad(rocket)) {
            return;
        }

        EntityPlayerMP rider = getPlayerRider(rocket);
        if (rider == null) {
            return;
        }

        RocketLandingPadStorage storage = (RocketLandingPadStorage) rocket;
        ItemStack landingPads = storage.hbmgc$getLandingPadStack();
        if (landingPads.isEmpty()) {
            return;
        }

        GCPlayerStats.get(rider).setLaunchpadStack(landingPads.copy());
        storage.hbmgc$setLandingPadStack(ItemStack.EMPTY);
    }

    public static boolean isRocketOnHbmPad(EntityAutoRocket rocket) {
        World world = rocket.getEntityWorld();
        int rocketX = MathHelper.floor(rocket.posX);
        int rocketY = MathHelper.floor(rocket.posY);
        int rocketZ = MathHelper.floor(rocket.posZ);

        for (int y = rocketY - 3; y <= rocketY + 1; y++) {
            for (int x = rocketX - 1; x <= rocketX + 1; x++) {
                for (int z = rocketZ - 1; z <= rocketZ + 1; z++) {
                    HbmRocketPadAdapter pad = HbmRocketPadAdapters.find(world, new BlockPos(x, y, z));
                    if (pad != null) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static EntityPlayerMP getPlayerRider(EntityAutoRocket rocket) {
        for (Entity passenger : rocket.getPassengers()) {
            if (passenger instanceof EntityPlayerMP) {
                return (EntityPlayerMP) passenger;
            }
        }

        return null;
    }
}
