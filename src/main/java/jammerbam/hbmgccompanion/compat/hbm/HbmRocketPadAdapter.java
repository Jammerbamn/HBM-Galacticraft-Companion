package jammerbam.hbmgccompanion.compat.hbm;

import micdoodle8.mods.galacticraft.api.prefab.entity.EntityAutoRocket;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface HbmRocketPadAdapter {
    World getWorld();

    BlockPos getCorePos();

    double getRocketX();

    double getRocketY();

    double getRocketZ();

    boolean canAccept(ItemStack rocketStack);

    boolean hasRocket();

    void afterRocketSpawned(EntityAutoRocket rocket);

    default boolean placeRocketStack(ItemStack rocketStack, net.minecraft.entity.player.EntityPlayer player) {
        return false;
    }
}
