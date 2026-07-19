package jammerbam.hbmgccompanion.mixins;

import jammerbam.hbmgccompanion.compat.galacticraft.RocketLandingPadStorage;
import jammerbam.hbmgccompanion.compat.galacticraft.RocketPadInventorySource;
import jammerbam.hbmgccompanion.compat.hbm.HbmRocketInventoryLaunchService;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntityAutoRocket;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = EntityAutoRocket.class, remap = false)
abstract class GCEntityAutoRocket_LandingPadStorageMixin implements RocketLandingPadStorage, RocketPadInventorySource {
    @Unique
    private static final String hbmgc$LANDING_PAD_STACK_KEY = "HBMGCCompanionLandingPads";
    @Unique
    private static final String hbmgc$PAD_INVENTORY_SOURCE_KEY = "HBMGCCompanionPadInventorySource";

    @Unique
    private ItemStack hbmgc$landingPadStack = ItemStack.EMPTY;
    @Unique
    private boolean hbmgc$hasPadInventorySource;
    @Unique
    private int hbmgc$padInventorySourceDimension;
    @Unique
    private BlockPos hbmgc$padInventorySourcePos = BlockPos.ORIGIN;
    @Unique
    private int hbmgc$padInventorySourceSlot = -1;

    @Override
    public ItemStack hbmgc$getLandingPadStack() {
        return this.hbmgc$landingPadStack;
    }

    @Override
    public void hbmgc$setLandingPadStack(ItemStack stack) {
        this.hbmgc$landingPadStack = stack.isEmpty() ? ItemStack.EMPTY : stack;
    }

    @Override
    public void hbmgc$setPadInventorySource(int dimension, BlockPos pos, int slot) {
        this.hbmgc$hasPadInventorySource = true;
        this.hbmgc$padInventorySourceDimension = dimension;
        this.hbmgc$padInventorySourcePos = pos.toImmutable();
        this.hbmgc$padInventorySourceSlot = slot;
    }

    @Override
    public boolean hbmgc$hasPadInventorySource() {
        return this.hbmgc$hasPadInventorySource;
    }

    @Override
    public int hbmgc$getPadInventorySourceDimension() {
        return this.hbmgc$padInventorySourceDimension;
    }

    @Override
    public BlockPos hbmgc$getPadInventorySourcePos() {
        return this.hbmgc$padInventorySourcePos;
    }

    @Override
    public int hbmgc$getPadInventorySourceSlot() {
        return this.hbmgc$padInventorySourceSlot;
    }

    @Inject(method = "func_70014_b", at = @At("RETURN"))
    private void hbmgc$writeLandingPadStack(NBTTagCompound tag, CallbackInfo ci) {
        if (!this.hbmgc$landingPadStack.isEmpty()) {
            tag.setTag(hbmgc$LANDING_PAD_STACK_KEY, this.hbmgc$landingPadStack.writeToNBT(new NBTTagCompound()));
        }

        if (this.hbmgc$hasPadInventorySource) {
            NBTTagCompound source = new NBTTagCompound();
            source.setInteger("Dimension", this.hbmgc$padInventorySourceDimension);
            source.setInteger("X", this.hbmgc$padInventorySourcePos.getX());
            source.setInteger("Y", this.hbmgc$padInventorySourcePos.getY());
            source.setInteger("Z", this.hbmgc$padInventorySourcePos.getZ());
            source.setInteger("Slot", this.hbmgc$padInventorySourceSlot);
            tag.setTag(hbmgc$PAD_INVENTORY_SOURCE_KEY, source);
        }
    }

    @Inject(method = "func_70037_a", at = @At("RETURN"))
    private void hbmgc$readLandingPadStack(NBTTagCompound tag, CallbackInfo ci) {
        if (tag.hasKey(hbmgc$LANDING_PAD_STACK_KEY, 10)) {
            this.hbmgc$landingPadStack = new ItemStack(tag.getCompoundTag(hbmgc$LANDING_PAD_STACK_KEY));
        } else {
            this.hbmgc$landingPadStack = ItemStack.EMPTY;
        }

        if (tag.hasKey(hbmgc$PAD_INVENTORY_SOURCE_KEY, 10)) {
            NBTTagCompound source = tag.getCompoundTag(hbmgc$PAD_INVENTORY_SOURCE_KEY);
            this.hbmgc$hasPadInventorySource = true;
            this.hbmgc$padInventorySourceDimension = source.getInteger("Dimension");
            this.hbmgc$padInventorySourcePos = new BlockPos(
                    source.getInteger("X"),
                    source.getInteger("Y"),
                    source.getInteger("Z")
            );
            this.hbmgc$padInventorySourceSlot = source.getInteger("Slot");
        } else {
            this.hbmgc$hasPadInventorySource = false;
            this.hbmgc$padInventorySourcePos = BlockPos.ORIGIN;
            this.hbmgc$padInventorySourceSlot = -1;
        }
    }

    @Inject(method = "onLaunch", at = @At("HEAD"))
    private void hbmgc$clearPadInventoryRocketOnLaunch(CallbackInfo ci) {
        HbmRocketInventoryLaunchService.clearSourceSlotOnLaunch((EntityAutoRocket) (Object) this);
    }

    @Inject(method = "getItemsDropped", at = @At("RETURN"))
    private void hbmgc$dropLandingPadStack(List<ItemStack> drops, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (!this.hbmgc$landingPadStack.isEmpty()) {
            cir.getReturnValue().add(this.hbmgc$landingPadStack.copy());
        }
    }
}
