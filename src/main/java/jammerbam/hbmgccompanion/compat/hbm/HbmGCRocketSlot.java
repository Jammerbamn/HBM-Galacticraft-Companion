package jammerbam.hbmgccompanion.compat.hbm;

import jammerbam.hbmgccompanion.compat.galacticraft.GalacticraftRocketFactory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class HbmGCRocketSlot extends SlotItemHandler {
    public HbmGCRocketSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return GalacticraftRocketFactory.isSupportedRocket(stack);
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return 1;
    }
}
