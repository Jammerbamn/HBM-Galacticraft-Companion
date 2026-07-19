package jammerbam.hbmgccompanion.compat.hbm;

import com.hbm.tileentity.bomb.TileEntityLaunchPadBase;
import com.hbm.tileentity.bomb.TileEntityLaunchPadRusted;
import jammerbam.hbmgccompanion.compat.galacticraft.GalacticraftRocketFactory;
import jammerbam.hbmgccompanion.compat.galacticraft.RocketPadInventorySource;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntityAutoRocket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;

public final class HbmRocketInventoryLaunchService {
    private static final int ROCKET_SLOT = 0;
    private static final String ROCKET_FUEL_KEY = "RocketFuel";

    private HbmRocketInventoryLaunchService() {
    }

    public static boolean placeHeldRocketInSlot(TileEntity pad, ItemStack held, EntityPlayer player) {
        if (pad == null || pad.getWorld() == null || pad.getWorld().isRemote || !GalacticraftRocketFactory.isSupportedRocket(held)) {
            return false;
        }

        ItemStackHandler inventory = getInventory(pad);
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

    public static void syncFullPad(TileEntityLaunchPadBase pad) {
        if (!HbmGalacticraftEnergy.isBasicLaunchPad(pad)) {
            return;
        }

        syncPad(pad, pad.inventory, getRocketY(pad));
    }

    public static void syncRustedPad(TileEntityLaunchPadRusted pad) {
        syncPad(pad, pad.inventory, getRocketY(pad));
    }

    public static void clearSourceSlotOnLaunch(EntityAutoRocket rocket) {
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
        ItemStackHandler inventory = getInventory(tile);
        int slot = source.hbmgc$getPadInventorySourceSlot();
        if (inventory != null && slot >= 0 && slot < inventory.getSlots()) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (GalacticraftRocketFactory.isSupportedRocket(stack)) {
                inventory.setStackInSlot(slot, ItemStack.EMPTY);
                tile.markDirty();
            }
        }
    }

    public static boolean isInventoryRocketFromPad(EntityAutoRocket rocket, TileEntity pad) {
        if (!(rocket instanceof RocketPadInventorySource) || pad == null || pad.getWorld() == null) {
            return false;
        }

        RocketPadInventorySource source = (RocketPadInventorySource) rocket;
        return source.hbmgc$hasPadInventorySource()
                && source.hbmgc$getPadInventorySourceDimension() == pad.getWorld().provider.getDimension()
                && source.hbmgc$getPadInventorySourceSlot() == ROCKET_SLOT
                && pad.getPos().equals(source.hbmgc$getPadInventorySourcePos());
    }

    private static void syncPad(TileEntity pad, ItemStackHandler inventory, double rocketY) {
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

    private static void spawnRocketFromSlot(TileEntity pad, ItemStackHandler inventory, double rocketY) {
        ItemStack stack = inventory.getStackInSlot(ROCKET_SLOT);
        EntityAutoRocket existingRocket = findAnyRocket(pad);
        if (!GalacticraftRocketFactory.isSupportedRocket(stack) || existingRocket != null) {
            return;
        }

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

        ((RocketPadInventorySource) rocket).hbmgc$setPadInventorySource(
                pad.getWorld().provider.getDimension(),
                pad.getPos(),
                ROCKET_SLOT
        );
        pad.getWorld().spawnEntity(rocket);
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
        if (pad instanceof TileEntityLaunchPadBase) {
            return HbmRocketFuelRelay.findRocketOnFullLaunchPad(pad);
        }

        if (pad instanceof TileEntityLaunchPadRusted) {
            return HbmRocketFuelRelay.findRocket(pad);
        }

        return null;
    }

    private static ItemStackHandler getInventory(TileEntity pad) {
        if (pad instanceof TileEntityLaunchPadBase) {
            return ((TileEntityLaunchPadBase) pad).inventory;
        }

        if (pad instanceof TileEntityLaunchPadRusted) {
            return ((TileEntityLaunchPadRusted) pad).inventory;
        }

        return null;
    }

    private static double getRocketY(TileEntity pad) {
        if (pad instanceof TileEntityLaunchPadBase) {
            return pad.getPos().getY() + ((TileEntityLaunchPadBase) pad).getLaunchOffset();
        }

        return pad.getPos().getY() + 1.0D;
    }
}
