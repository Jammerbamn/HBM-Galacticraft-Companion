package jammerbam.hbmgccompanion.compat.hbm;

import micdoodle8.mods.galacticraft.api.prefab.entity.EntityAutoRocket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import com.hbm.tileentity.bomb.TileEntityLaunchPadRusted;

import java.util.List;

final class RustedLaunchPadAdapter implements HbmRocketPadAdapter {
    private final TileEntityLaunchPadRusted pad;

    RustedLaunchPadAdapter(TileEntityLaunchPadRusted pad) {
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
        return this.getCorePos().getY() + 1.0D;
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
        List<EntityAutoRocket> rockets = this.getWorld().getEntitiesWithinAABB(EntityAutoRocket.class, this.getOccupancyBounds());
        return !rockets.isEmpty();
    }

    @Override
    public void afterRocketSpawned(EntityAutoRocket rocket) {
    }

    @Override
    public boolean placeRocketStack(ItemStack rocketStack, EntityPlayer player) {
        return HbmRocketInventoryLaunchService.placeHeldRocketInSlot(this.pad, rocketStack, player);
    }

    private AxisAlignedBB getOccupancyBounds() {
        BlockPos corePos = this.getCorePos();
        return new AxisAlignedBB(
                corePos.getX() - 1.0D,
                corePos.getY(),
                corePos.getZ() - 1.0D,
                corePos.getX() + 2.0D,
                corePos.getY() + 8.0D,
                corePos.getZ() + 2.0D
        );
    }
}
