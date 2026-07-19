package jammerbam.hbmgccompanion.compat.galacticraft;

import micdoodle8.mods.galacticraft.core.GCBlocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class LandingPadSlot extends Slot {
    public static final int X = 26;
    public static final int Y = 17;
    private static final int MAX_PAD_STACK = 9;

    public LandingPadSlot(IInventory inventory) {
        super(inventory, 0, X, Y);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return isGalacticraftLandingPad(stack);
    }

    @Override
    public int getSlotStackLimit() {
        return MAX_PAD_STACK;
    }

    public static boolean isGalacticraftLandingPad(ItemStack stack) {
        return !stack.isEmpty()
                && stack.getItem() == Item.getItemFromBlock(GCBlocks.landingPad)
                && stack.getMetadata() == 0;
    }
}
