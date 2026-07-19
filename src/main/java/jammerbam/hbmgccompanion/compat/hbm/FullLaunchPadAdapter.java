package jammerbam.hbmgccompanion.compat.hbm;

import com.hbm.tileentity.bomb.TileEntityLaunchPad;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntityAutoRocket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

final class FullLaunchPadAdapter implements HbmRocketPadAdapter {
    private final TileEntityLaunchPad pad;

    FullLaunchPadAdapter(TileEntityLaunchPad pad) {
        this.pad = pad;
    }

    @Override
    public World getWorld() {
        return this.pad.getWorld();
    }

    @Override
    public BlockPos getCorePos() {
        return this.pad.getPos();
    }

    @Override
    public double getRocketX() {
        return this.getCorePos().getX() + 0.5D;
    }

    @Override
    public double getRocketY() {
        return this.getCorePos().getY() + this.pad.getLaunchOffset();
    }

    @Override
    public double getRocketZ() {
        return this.getCorePos().getZ() + 0.5D;
    }

    @Override
    public boolean canAccept(ItemStack rocketStack) {
        return this.pad.inventory.getStackInSlot(0).isEmpty();
    }

    @Override
    public boolean hasRocket() {
        return HbmRocketFuelRelay.findRocketOnFullLaunchPad(this.pad) != null;
    }

    @Override
    public void afterRocketSpawned(EntityAutoRocket rocket) {
    }

    @Override
    public boolean placeRocketStack(ItemStack rocketStack, EntityPlayer player) {
        return HbmRocketInventoryLaunchService.placeHeldRocketInSlot(this.pad, rocketStack, player);
    }
}
