package jammerbam.hbmgccompanion.compat.rocket;

import jammerbam.hbmgccompanion.compat.galacticraft.GalacticraftRocketFactory;
import jammerbam.hbmgccompanion.compat.hbm.HbmRocketPadAdapter;
import jammerbam.hbmgccompanion.compat.hbm.HbmRocketPadAdapters;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class RocketPlacementService {
    private static final Logger LOG = LogManager.getLogger("hbmgccompanion/RocketPlacement");
    private static final long SLOW_PLACEMENT_NS = 50_000_000L;

    private RocketPlacementService() {
    }

    public static boolean tryPlaceRocket(World world, BlockPos clickedPos, EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);
        if (!GalacticraftRocketFactory.isSupportedRocket(held)) {
            return false;
        }

        long started = System.nanoTime();
        HbmRocketPadAdapter pad = HbmRocketPadAdapters.find(world, clickedPos);
        if (pad == null || !pad.canAccept(held)) {
            return false;
        }

        long resolved = System.nanoTime();
        if (!world.isRemote) {
            placeServerSide(pad, held, player);
        }

        long finished = System.nanoTime();
        if (finished - started >= SLOW_PLACEMENT_NS) {
            LOG.warn("Slow rocket placement on {}: pad lookup {} ms, placement {} ms at {}",
                    world.isRemote ? "client" : "server",
                    (resolved - started) / 1_000_000L,
                    (finished - resolved) / 1_000_000L,
                    clickedPos);
        }

        return true;
    }

    private static void placeServerSide(HbmRocketPadAdapter pad, ItemStack rocketStack, EntityPlayer player) {
        if (!pad.hasRocket()) {
            pad.placeRocketStack(rocketStack, player);
        }
    }
}
