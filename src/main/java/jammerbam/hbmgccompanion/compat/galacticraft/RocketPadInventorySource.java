package jammerbam.hbmgccompanion.compat.galacticraft;

import net.minecraft.util.math.BlockPos;

public interface RocketPadInventorySource {
    void hbmgc$setPadInventorySource(int dimension, BlockPos pos, int slot);

    boolean hbmgc$hasPadInventorySource();

    int hbmgc$getPadInventorySourceDimension();

    BlockPos hbmgc$getPadInventorySourcePos();

    int hbmgc$getPadInventorySourceSlot();
}
