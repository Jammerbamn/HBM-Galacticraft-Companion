package jammerbam.hbmgccompanion.compat.hbm;

import micdoodle8.mods.galacticraft.api.prefab.entity.EntityAutoRocket;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;

public final class HbmRocketPadAdapters {
    private HbmRocketPadAdapters() {
    }

    public static HbmRocketPadAdapter find(World world, BlockPos clickedPos) {
        TileEntity padTile = findPadTile(world, clickedPos);
        if (padTile != null) {
            return fromTile(padTile);
        }

        return null;
    }

    public static TileEntity findPadTile(World world, BlockPos clickedPos) {
        if (world == null || clickedPos == null) {
            return null;
        }

        Block block = world.getBlockState(clickedPos).getBlock();
        ResourceLocation blockName = block.getRegistryName();
        if (blockName == null || !"hbm".equals(blockName.getNamespace())) {
            return null;
        }

        TileEntity reflectedCore = findCoreTileEntity(block, world, clickedPos);
        if (isUsableLaunchPadTile(reflectedCore)) {
            return reflectedCore;
        }

        TileEntity direct = world.getTileEntity(clickedPos);
        if (isUsableLaunchPadTile(direct)) {
            return direct;
        }

        return null;
    }

    public static TileEntity findPadTileNear(World world, BlockPos origin, int horizontalRadius, int verticalRadius) {
        TileEntity exact = findPadTile(world, origin);
        if (exact != null) {
            return exact;
        }

        if (world == null || origin == null) {
            return null;
        }

        int radius = Math.max(0, horizontalRadius);
        int yRadius = Math.max(0, verticalRadius);
        for (int dy = -yRadius; dy <= yRadius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue;
                    }

                    TileEntity candidate = findPadTile(world, origin.add(dx, dy, dz));
                    if (candidate != null) {
                        return candidate;
                    }
                }
            }
        }

        return null;
    }

    public static HbmRocketPadAdapter fromTile(TileEntity tile) {
        if (tile == null || tile.getWorld() == null || !isHbmLaunchPadTile(tile)) {
            return null;
        }

        ItemStackHandler inventory = getInventory(tile);
        return inventory == null ? null : new GenericLaunchPadAdapter(tile, inventory);
    }

    public static boolean isHbmLaunchPadTile(TileEntity tile) {
        if (tile == null || tile.getWorld() == null) {
            return false;
        }

        Block block = tile.getWorld().getBlockState(tile.getPos()).getBlock();
        return isHbmLaunchPadBlock(block);
    }

    public static ItemStackHandler getInventory(TileEntity tile) {
        if (tile == null) {
            return null;
        }

        try {
            Object inventory = tile.getClass().getField("inventory").get(tile);
            return inventory instanceof ItemStackHandler ? (ItemStackHandler) inventory : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static boolean isUsableLaunchPadTile(TileEntity tile) {
        return isHbmLaunchPadTile(tile) && getInventory(tile) != null;
    }

    private static boolean isHbmLaunchPadBlock(Block block) {
        if (block == null) {
            return false;
        }

        ResourceLocation registryName = block.getRegistryName();
        return registryName != null
                && "hbm".equals(registryName.getNamespace())
                && ("launch_pad".equals(registryName.getPath())
                || "launch_pad_rusted".equals(registryName.getPath()));
    }

    private static TileEntity findCoreTileEntity(Block block, World world, BlockPos pos) {
        TileEntity core = findCoreTileEntityByBlockPos(block, world, pos);
        if (core != null) {
            return core;
        }

        core = findCoreTileEntityByCoords(block, world, pos);
        if (core != null) {
            return core;
        }

        try {
            Object reflectedCore = block.getClass()
                    .getMethod("findCoreTE", World.class, BlockPos.class)
                    .invoke(block, world, pos);
            return reflectedCore instanceof TileEntity ? (TileEntity) reflectedCore : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static TileEntity findCoreTileEntityByBlockPos(Block block, World world, BlockPos pos) {
        try {
            Object corePos = block.getClass()
                    .getMethod("findCore", IBlockAccess.class, BlockPos.class)
                    .invoke(block, world, pos);
            return corePos instanceof BlockPos ? world.getTileEntity((BlockPos) corePos) : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static TileEntity findCoreTileEntityByCoords(Block block, World world, BlockPos pos) {
        try {
            Object coreCoords = block.getClass()
                    .getMethod("findCore", IBlockAccess.class, int.class, int.class, int.class)
                    .invoke(block, world, pos.getX(), pos.getY(), pos.getZ());
            if (!(coreCoords instanceof int[])) {
                return null;
            }

            int[] coords = (int[]) coreCoords;
            return coords.length >= 3 ? world.getTileEntity(new BlockPos(coords[0], coords[1], coords[2])) : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static final class GenericLaunchPadAdapter implements HbmRocketPadAdapter {
        private final TileEntity pad;
        private final ItemStackHandler inventory;

        private GenericLaunchPadAdapter(TileEntity pad, ItemStackHandler inventory) {
            this.pad = pad;
            this.inventory = inventory;
        }

        @Override
        public boolean canAccept(ItemStack rocketStack) {
            return this.inventory.getStackInSlot(0).isEmpty();
        }

        @Override
        public boolean hasRocket() {
            List<EntityAutoRocket> rockets = this.pad.getWorld().getEntitiesWithinAABB(EntityAutoRocket.class, this.getOccupancyBounds());
            return !rockets.isEmpty();
        }

        @Override
        public boolean placeRocketStack(ItemStack rocketStack, EntityPlayer player) {
            return HbmRocketInventoryLaunchService.placeHeldRocketInSlot(this.pad, this.inventory, rocketStack, player);
        }

        private AxisAlignedBB getOccupancyBounds() {
            BlockPos corePos = this.pad.getPos();
            if (getLaunchOffset(this.pad) != null) {
                return new AxisAlignedBB(
                        corePos.getX() - 3.0D,
                        corePos.getY() - 2.0D,
                        corePos.getZ() - 3.0D,
                        corePos.getX() + 4.0D,
                        corePos.getY() + 14.0D,
                        corePos.getZ() + 4.0D
                );
            }

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

    static Integer getLaunchOffset(TileEntity pad) {
        if (isSingleBlockLaunchPad(pad)) {
            return null;
        }

        try {
            Object offset = pad.getClass().getMethod("getLaunchOffset").invoke(pad);
            return offset instanceof Number ? ((Number) offset).intValue() : null;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }

    private static boolean isSingleBlockLaunchPad(TileEntity pad) {
        String className = pad.getClass().getName();
        return "com.hbm.tileentity.bomb.TileEntityLaunchPad".equals(className)
                || "com.hbm.tileentity.bomb.TileEntityLaunchPadRusted".equals(className);
    }
}
