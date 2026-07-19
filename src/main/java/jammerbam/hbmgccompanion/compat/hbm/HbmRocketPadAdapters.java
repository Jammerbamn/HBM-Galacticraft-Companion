package jammerbam.hbmgccompanion.compat.hbm;

import com.hbm.blocks.BlockDummyable;
import com.hbm.tileentity.bomb.TileEntityLaunchPad;
import com.hbm.tileentity.bomb.TileEntityLaunchPadRusted;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class HbmRocketPadAdapters {
    private HbmRocketPadAdapters() {
    }

    public static HbmRocketPadAdapter find(World world, BlockPos clickedPos) {
        TileEntity direct = world.getTileEntity(clickedPos);
        HbmRocketPadAdapter directAdapter = fromTile(direct);
        if (directAdapter != null) {
            return directAdapter;
        }

        Block block = world.getBlockState(clickedPos).getBlock();
        if (block instanceof BlockDummyable) {
            TileEntity core = ((BlockDummyable) block).findCoreTE(world, clickedPos);
            return fromTile(core);
        }

        return null;
    }

    private static HbmRocketPadAdapter fromTile(TileEntity tile) {
        if (tile instanceof TileEntityLaunchPadRusted) {
            return new RustedLaunchPadAdapter((TileEntityLaunchPadRusted) tile);
        }

        if (tile instanceof TileEntityLaunchPad && HbmGalacticraftEnergy.isBasicLaunchPad((TileEntity) tile)) {
            return new FullLaunchPadAdapter((TileEntityLaunchPad) tile);
        }

        return null;
    }
}
