package jammerbam.hbmgccompanion.mixins;

import com.hbm.inventory.container.ContainerLaunchPadRusted;
import jammerbam.hbmgccompanion.compat.galacticraft.GalacticraftRocketFactory;
import jammerbam.hbmgccompanion.compat.hbm.HbmGCRocketSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ContainerLaunchPadRusted.class, remap = false)
abstract class HBMRustedLaunchPadContainer_GCRocketSlotMixin extends Container {
    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/hbm/inventory/container/ContainerLaunchPadRusted;addSlotToContainer(Lnet/minecraft/inventory/Slot;)Lnet/minecraft/inventory/Slot;",
                    ordinal = 0
            ),
            index = 0
    )
    private Slot hbmgc$allowGCRocketInRocketSlot(Slot original) {
        if (original instanceof SlotItemHandler) {
            SlotItemHandler slot = (SlotItemHandler) original;
            return new HbmGCRocketSlot(slot.getItemHandler(), slot.getSlotIndex(), original.xPos, original.yPos);
        }

        return original;
    }

    @Inject(method = "transferStackInSlot", at = @At("HEAD"), cancellable = true)
    private void hbmgc$shiftClickGCRocket(EntityPlayer player, int index, CallbackInfoReturnable<ItemStack> cir) {
        if (index <= 3 || index >= this.inventorySlots.size()) {
            return;
        }

        Slot slot = this.inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) {
            return;
        }

        ItemStack stack = slot.getStack();
        if (!GalacticraftRocketFactory.isSupportedRocket(stack)) {
            return;
        }

        ItemStack original = stack.copy();
        if (!this.mergeItemStack(stack, 0, 1, false)) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }

        if (stack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }

        cir.setReturnValue(original);
    }
}
