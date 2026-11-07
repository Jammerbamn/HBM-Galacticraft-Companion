package jammerbam.hbmgccompanion.compat.hbm;

import jammerbam.hbmgccompanion.compat.galacticraft.GalacticraftRocketFactory;
import jammerbam.hbmgccompanion.compat.galacticraft.RocketPadInventorySource;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntityAutoRocket;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public final class HbmRocketInventoryLaunchService {
    private static final Logger LOG = LogManager.getLogger("hbmgccompanion/RocketPlacement");
    private static final long SLOW_SPAWN_NS = 50_000_000L;
    private static final int ROCKET_SLOT = 0;
    private static final String ROCKET_FUEL_KEY = "RocketFuel";

    private HbmRocketInventoryLaunchService() {
    }

    public static boolean placeHeldRocketInSlot(TileEntity pad, ItemStack held, EntityPlayer player) {
        return placeHeldRocketInSlot(pad, HbmRocketPadAdapters.getInventory(pad), held, player);
    }

    public static boolean placeHeldRocketInSlot(TileEntity pad, ItemStackHandler inventory, ItemStack held, EntityPlayer player) {
        if (pad == null || pad.getWorld() == null || pad.getWorld().isRemote || !GalacticraftRocketFactory.isSupportedRocket(held)) {
            return false;
        }

        if (inventory == null || !inventory.getStackInSlot(ROCKET_SLOT).isEmpty() || findInventoryRocket(pad) != null) {
            return false;
        }

        ItemStack stored = held.copy();
        stored.setCount(1);
        inventory.setStackInSlot(ROCKET_SLOT, stored);
        pad.markDirty();

        spawnRocketFromSlot(pad, inventory, getRocketY(pad));
        if (!player.capabilities.isCreativeMode) {
            held.shrink(1);
        }

        return true;
    }

    public static void syncPad(TileEntity pad) {
        syncPad(pad, HbmRocketPadAdapters.getInventory(pad), getRocketY(pad));
    }

    public static void syncFullPad(TileEntity pad) {
        syncPad(pad);
    }

    public static void syncRustedPad(TileEntity pad) {
        syncPad(pad);
    }

    public static void syncPad(TileEntity pad, ItemStackHandler inventory, double rocketY) {
        if (pad == null || pad.getWorld() == null || pad.getWorld().isRemote || inventory == null) {
            return;
        }

        ItemStack stack = inventory.getStackInSlot(ROCKET_SLOT);
        EntityAutoRocket rocket = findInventoryRocket(pad);
        if (!GalacticraftRocketFactory.isSupportedRocket(stack)) {
            if (rocket != null && !rocket.getLaunched()) {
                rocket.setDead();
            }
            return;
        }

        if (rocket == null) {
            spawnRocketFromSlot(pad, inventory, rocketY);
        } else {
            syncFuelToStack(pad, inventory, rocket);
        }
    }

    public static void clearSourceSlotWhenRocketLeavesPad(EntityAutoRocket rocket) {
        if (!(rocket instanceof RocketPadInventorySource)) {
            return;
        }

        RocketPadInventorySource source = (RocketPadInventorySource) rocket;
        World world = rocket.getEntityWorld();
        if (!source.hbmgc$hasPadInventorySource()
                || world == null
                || world.isRemote
                || world.provider.getDimension() != source.hbmgc$getPadInventorySourceDimension()) {
            return;
        }

        TileEntity tile = world.getTileEntity(source.hbmgc$getPadInventorySourcePos());
        ItemStackHandler inventory = HbmRocketPadAdapters.getInventory(tile);
        int slot = source.hbmgc$getPadInventorySourceSlot();
        if (inventory != null && slot >= 0 && slot < inventory.getSlots()) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (GalacticraftRocketFactory.isSupportedRocket(stack)) {
                inventory.setStackInSlot(slot, ItemStack.EMPTY);
                tile.markDirty();
            }
        }
    }

    public static void destroyRocketForPadBlock(World world, BlockPos pos, IBlockState oldState) {
        if (world == null || world.isRemote || pos == null || oldState == null) {
            return;
        }

        ResourceLocation blockName = oldState.getBlock().getRegistryName();
        if (blockName == null || !"hbm".equals(blockName.getNamespace())
                || (!"launch_pad".equals(blockName.getPath())
                && !"launch_pad_rusted".equals(blockName.getPath()))) {
            return;
        }

        AxisAlignedBB bounds = new AxisAlignedBB(pos).grow(3.0D, 8.0D, 3.0D);
        List<EntityAutoRocket> rockets = world.getEntitiesWithinAABB(EntityAutoRocket.class, bounds);
        for (EntityAutoRocket rocket : rockets) {
            if (!rocket.getLaunched() && isInventoryRocketFromPadPos(rocket, world, pos)) {
                rocket.setDead();
            }
        }
    }

    public static boolean isInventoryRocketFromPad(EntityAutoRocket rocket, TileEntity pad) {
        if (!(rocket instanceof RocketPadInventorySource) || pad == null || pad.getWorld() == null) {
            return false;
        }

        return isInventoryRocketFromPadPos(rocket, pad.getWorld(), pad.getPos());
    }

    private static boolean isInventoryRocketFromPadPos(EntityAutoRocket rocket, World world, BlockPos pos) {
        if (!(rocket instanceof RocketPadInventorySource)) {
            return false;
        }

        RocketPadInventorySource source = (RocketPadInventorySource) rocket;
        return source.hbmgc$hasPadInventorySource()
                && source.hbmgc$getPadInventorySourceDimension() == world.provider.getDimension()
                && source.hbmgc$getPadInventorySourceSlot() == ROCKET_SLOT
                && pos.equals(source.hbmgc$getPadInventorySourcePos());
    }

    private static void spawnRocketFromSlot(TileEntity pad, ItemStackHandler inventory, double rocketY) {
        ItemStack stack = inventory.getStackInSlot(ROCKET_SLOT);
        long started = System.nanoTime();
        EntityAutoRocket existingRocket = findAnyRocket(pad);
        if (!GalacticraftRocketFactory.isSupportedRocket(stack) || existingRocket != null) {
            return;
        }

        long checked = System.nanoTime();
        EntityAutoRocket rocket = GalacticraftRocketFactory.createRocket(
                pad.getWorld(),
                stack,
                pad.getPos().getX() + 0.5D,
                rocketY,
                pad.getPos().getZ() + 0.5D
        );
        if (rocket == null) {
            return;
        }

        long created = System.nanoTime();
        ((RocketPadInventorySource) rocket).hbmgc$setPadInventorySource(
                pad.getWorld().provider.getDimension(),
                pad.getPos(),
                ROCKET_SLOT
        );
        pad.getWorld().spawnEntity(rocket);
        long finished = System.nanoTime();
        if (finished - started >= SLOW_SPAWN_NS) {
            LOG.warn("Slow rocket spawn: occupancy {} ms, construction {} ms, world spawn {} ms at {}",
                    (checked - started) / 1_000_000L,
                    (created - checked) / 1_000_000L,
                    (finished - created) / 1_000_000L,
                    pad.getPos());
        }
    }

    private static void syncFuelToStack(TileEntity pad, ItemStackHandler inventory, EntityAutoRocket rocket) {
        ItemStack stack = inventory.getStackInSlot(ROCKET_SLOT);
        if (stack.isEmpty()) {
            return;
        }

        int fuelAmount = rocket.fuelTank.getFluidAmount();
        int storedAmount = stack.hasTagCompound() ? stack.getTagCompound().getInteger(ROCKET_FUEL_KEY) : 0;
        if (fuelAmount == storedAmount) {
            return;
        }

        ItemStack updated = stack.copy();
        NBTTagCompound tag = updated.hasTagCompound() ? updated.getTagCompound().copy() : new NBTTagCompound();
        tag.setInteger(ROCKET_FUEL_KEY, fuelAmount);
        updated.setTagCompound(tag);
        inventory.setStackInSlot(ROCKET_SLOT, updated);
        pad.markDirty();
    }

    private static EntityAutoRocket findInventoryRocket(TileEntity pad) {
        EntityAutoRocket rocket = findAnyRocket(pad);
        return rocket != null && isInventoryRocketFromPad(rocket, pad) ? rocket : null;
    }

    private static EntityAutoRocket findAnyRocket(TileEntity pad) {
        return HbmRocketPadAdapters.getLaunchOffset(pad) != null
                ? HbmRocketFuelRelay.findRocketOnFullLaunchPad(pad)
                : HbmRocketFuelRelay.findRocket(pad);
    }

    private static double getRocketY(TileEntity pad) {
        Integer launchOffset = HbmRocketPadAdapters.getLaunchOffset(pad);
        return pad.getPos().getY() + (launchOffset == null ? 1.0D : launchOffset.doubleValue());
    }
}
