package jammerbam.hbmgccompanion.compat.galacticraft;

import micdoodle8.mods.galacticraft.api.prefab.entity.EntityAutoRocket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentTranslation;

public final class RocketLandingPadInventory implements IInventory {
    private final EntityAutoRocket rocket;
    private final RocketLandingPadStorage storage;

    public RocketLandingPadInventory(EntityAutoRocket rocket) {
        this.rocket = rocket;
        this.storage = (RocketLandingPadStorage) rocket;
    }

    @Override
    public int getSizeInventory() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return this.getStackInSlot(0).isEmpty();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return this.storage.hbmgc$getLandingPadStack();
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        NonNullList<ItemStack> stack = NonNullList.withSize(1, this.getStackInSlot(index));
        ItemStack removed = ItemStackHelper.getAndSplit(stack, 0, count);
        this.storage.hbmgc$setLandingPadStack(stack.get(0));
        return removed;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = this.getStackInSlot(index);
        this.storage.hbmgc$setLandingPadStack(ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
        }

        this.storage.hbmgc$setLandingPadStack(stack);
    }

    @Override
    public int getInventoryStackLimit() {
        return 9;
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return this.rocket.isUsableByPlayer(player);
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return LandingPadSlot.isGalacticraftLandingPad(stack);
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        this.storage.hbmgc$setLandingPadStack(ItemStack.EMPTY);
    }

    @Override
    public String getName() {
        return "container.hbmgc.rocket_landing_pad";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentTranslation(this.getName());
    }
}
