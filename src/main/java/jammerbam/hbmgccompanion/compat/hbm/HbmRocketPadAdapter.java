package jammerbam.hbmgccompanion.compat.hbm;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface HbmRocketPadAdapter {
    boolean canAccept(ItemStack rocketStack);

    boolean hasRocket();

    boolean placeRocketStack(ItemStack rocketStack, EntityPlayer player);
}
