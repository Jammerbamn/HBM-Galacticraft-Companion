package jammerbam.hbmgccompanion.compat.galacticraft;

import jammerbam.hbmgccompanion.compat.hbm.HbmRocketPadAdapters;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntityAutoRocket;
import micdoodle8.mods.galacticraft.core.entities.player.GCPlayerStats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
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
        return findPadUnderRocket(rocket) != null;
    }

    public static BlockPos findPadUnderRocket(EntityAutoRocket rocket) {
        World world = rocket.getEntityWorld();
        if (world == null) {
            return null;
        }

        int rocketX = MathHelper.floor(rocket.posX);
        int rocketY = MathHelper.floor(rocket.posY);
        int rocketZ = MathHelper.floor(rocket.posZ);

        for (int y = rocketY - 3; y <= rocketY + 1; y++) {
            TileEntity pad = HbmRocketPadAdapters.findPadTile(world, new BlockPos(rocketX, y, rocketZ));
            if (pad != null) {
                return pad.getPos();
            }
        }

        return null;
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
