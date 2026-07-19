package jammerbam.hbmgccompanion.compat.rocket;

import jammerbam.hbmgccompanion.compat.galacticraft.GalacticraftRocketFactory;
import jammerbam.hbmgccompanion.compat.hbm.HbmRocketPadAdapter;
import jammerbam.hbmgccompanion.compat.hbm.HbmRocketPadAdapters;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntityAutoRocket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class RocketPlacementService {
    private RocketPlacementService() {
    }

    public static boolean tryPlaceRocket(World world, BlockPos clickedPos, EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);
        if (!GalacticraftRocketFactory.isSupportedRocket(held)) {
            return false;
        }

        HbmRocketPadAdapter pad = HbmRocketPadAdapters.find(world, clickedPos);
        if (pad == null || !pad.canAccept(held)) {
            return false;
        }

        if (!world.isRemote) {
            placeServerSide(pad, held, player);
        }

        return true;
    }

    private static void placeServerSide(HbmRocketPadAdapter pad, ItemStack rocketStack, EntityPlayer player) {
        if (pad.hasRocket()) {
            return;
        }

        if (pad.placeRocketStack(rocketStack, player)) {
            return;
        }

        EntityAutoRocket rocket = GalacticraftRocketFactory.createRocket(
                pad.getWorld(),
                rocketStack,
                pad.getRocketX(),
                pad.getRocketY(),
                pad.getRocketZ()
        );
        if (rocket == null) {
            return;
        }

        pad.getWorld().spawnEntity(rocket);
        pad.afterRocketSpawned(rocket);

        if (!player.capabilities.isCreativeMode) {
            rocketStack.shrink(1);
        }
    }
}
