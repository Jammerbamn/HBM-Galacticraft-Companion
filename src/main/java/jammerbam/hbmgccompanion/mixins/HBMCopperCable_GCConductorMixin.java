package jammerbam.hbmgccompanion.mixins;

import com.hbm.tileentity.network.energy.TileEntityCableBaseNT;
import com.hbm.api.energymk2.Nodespace;
import com.hbm.api.energymk2.PowerNetMK2;
import com.hbm.config.GeneralConfig;
import micdoodle8.mods.galacticraft.api.transmission.NetworkType;
import micdoodle8.mods.galacticraft.api.transmission.grid.IElectricityNetwork;
import micdoodle8.mods.galacticraft.api.transmission.grid.IGridNetwork;
import micdoodle8.mods.galacticraft.api.transmission.tile.IConductor;
import micdoodle8.mods.galacticraft.api.transmission.tile.IConnector;
import micdoodle8.mods.galacticraft.api.transmission.tile.IElectrical;
import micdoodle8.mods.galacticraft.core.energy.EnergyConfigHandler;
import micdoodle8.mods.galacticraft.core.energy.grid.EnergyNetwork;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TileEntityCableBaseNT.class, remap = false)
abstract class HBMCopperCable_GCConductorMixin implements IConductor {

    @Unique
    private static final int hbmgc$HEAVY_ALUMINUM_WIRE_TIER = 2;

    @Shadow
    protected Nodespace.PowerNode node;

    @Unique
    private IElectricityNetwork hbmgc$network;

    @Unique
    private TileEntity[] hbmgc$adjacentConnections;

    @Override
    public int getTierGC() {
        return hbmgc$HEAVY_ALUMINUM_WIRE_TIER;
    }

    @Override
    public IElectricityNetwork getNetwork() {
        if (this.hbmgc$network == null) {
            EnergyNetwork energyNetwork = new EnergyNetwork();
            energyNetwork.getTransmitters().add((IConductor) (Object) this);
            this.setNetwork(energyNetwork);
        }

        return this.hbmgc$network;
    }

    @Override
    public boolean hasNetwork() {
        return this.hbmgc$network != null;
    }

    @Override
    public void setNetwork(IGridNetwork network) {
        this.hbmgc$network = network instanceof IElectricityNetwork ? (IElectricityNetwork) network : null;
    }

    @Override
    public TileEntity[] getAdjacentConnections() {
        if (this.hbmgc$adjacentConnections == null) {
            this.hbmgc$adjacentConnections = new TileEntity[6];

            if (this.canTransmit()) {
                TileEntity self = hbmgc$self();
                World world = self.getWorld();

                if (world != null) {
                    for (EnumFacing side : EnumFacing.VALUES) {
                        TileEntity adjacent = world.getTileEntity(self.getPos().offset(side));
                        if (adjacent instanceof IConnector
                                && ((IConnector) adjacent).canConnect(side.getOpposite(), NetworkType.POWER)) {
                            this.hbmgc$adjacentConnections[side.ordinal()] = adjacent;
                        }
                    }
                }
            }
        }

        return this.hbmgc$adjacentConnections;
    }

    @Override
    public void refresh() {
        TileEntity self = hbmgc$self();
        World world = self.getWorld();
        if (world == null || world.isRemote || !this.canTransmit()) {
            return;
        }

        this.hbmgc$adjacentConnections = null;
        this.getNetwork().refresh();

        for (EnumFacing side : EnumFacing.VALUES) {
            TileEntity adjacent = world.getTileEntity(self.getPos().offset(side));
            if (!(adjacent instanceof IConductor)
                    || !((IConductor) adjacent).canConnect(side.getOpposite(), NetworkType.POWER)) {
                continue;
            }

            IElectricityNetwork adjacentNetwork = ((IConductor) adjacent).getNetwork();
            if (!this.getNetwork().equals(adjacentNetwork) && !adjacentNetwork.getTransmitters().isEmpty()) {
                adjacentNetwork.merge(this.getNetwork());
            }
        }
    }

    @Override
    public void onNetworkChanged() {
        this.hbmgc$adjacentConnections = null;
    }

    @Override
    public boolean canConnect(EnumFacing direction, NetworkType type) {
        return type == NetworkType.POWER && hbmgc$isCopperCable();
    }

    @Override
    public NetworkType getNetworkType() {
        return NetworkType.POWER;
    }

    @Override
    public boolean canTransmit() {
        return hbmgc$isCopperCable();
    }

    @Inject(method = "onLoad", at = @At("RETURN"))
    private void hbmgc$refreshGCNetworkOnLoad(CallbackInfo ci) {
        hbmgc$refreshNetworkIfServer();
    }

    @Inject(method = "func_73660_a", at = @At("RETURN"))
    private void hbmgc$refreshGCNetworkOnTick(CallbackInfo ci) {
        TileEntity self = hbmgc$self();
        World world = self.getWorld();
        hbmgc$transferGCPowerToGCNetwork();
        hbmgc$transferHBMPowerToGCAcceptors();

        if (world != null && world.getTotalWorldTime() % 20L == 0L) {
            hbmgc$refreshNetworkIfServer();
        }
    }

    @Inject(method = "func_145843_s", at = @At("HEAD"))
    private void hbmgc$splitGCNetworkOnInvalidate(CallbackInfo ci) {
        TileEntity self = hbmgc$self();
        World world = self.getWorld();
        if (world != null && !world.isRemote && this.hbmgc$network != null) {
            this.hbmgc$network.split((IConductor) (Object) this);
            this.hbmgc$network = null;
            this.hbmgc$adjacentConnections = null;
        }
    }

    @Unique
    private void hbmgc$refreshNetworkIfServer() {
        TileEntity self = hbmgc$self();
        World world = self.getWorld();
        if (world != null && !world.isRemote && this.canTransmit()) {
            this.refresh();
        }
    }

    @Unique
    private void hbmgc$transferGCPowerToGCNetwork() {
        TileEntity self = hbmgc$self();
        World world = self.getWorld();
        if (world == null || world.isRemote || !this.canTransmit()) {
            return;
        }

        for (EnumFacing side : EnumFacing.VALUES) {
            TileEntity adjacent = world.getTileEntity(self.getPos().offset(side));
            if (!(adjacent instanceof IElectrical)) {
                continue;
            }

            IElectrical electrical = (IElectrical) adjacent;
            EnumFacing outputSide = side.getOpposite();
            float availableGC = electrical.getProvide(outputSide);
            if (availableGC <= 0.0F) {
                continue;
            }

            float extractableGC = electrical.provideElectricity(outputSide, availableGC, false);
            if (extractableGC <= 0.0F) {
                continue;
            }

            float rejectedGC = this.getNetwork().produce(
                    extractableGC,
                    false,
                    electrical.getTierGC(),
                    adjacent,
                    self
            );
            float acceptedGC = extractableGC - rejectedGC;
            if (acceptedGC <= 0.0F) {
                continue;
            }

            float extractedGC = electrical.provideElectricity(outputSide, acceptedGC, true);
            if (extractedGC > 0.0F) {
                this.getNetwork().produce(extractedGC, true, electrical.getTierGC(), adjacent, self);
            }
        }
    }

    @Unique
    private void hbmgc$transferHBMPowerToGCAcceptors() {
        TileEntity self = hbmgc$self();
        World world = self.getWorld();
        if (world == null || world.isRemote || !this.canTransmit() || GeneralConfig.conversionRateHeToRF <= 0.0D) {
            return;
        }

        PowerNetMK2 powerNet = this.node != null && this.node.hasValidNet() ? this.node.net : null;
        if (powerNet == null) {
            return;
        }

        for (EnumFacing side : EnumFacing.VALUES) {
            TileEntity adjacent = world.getTileEntity(self.getPos().offset(side));
            if (!(adjacent instanceof IElectrical)) {
                continue;
            }

            IElectrical electrical = (IElectrical) adjacent;
            EnumFacing inputSide = side.getOpposite();
            float requestedGC = electrical.getRequest(inputSide);
            if (requestedGC <= 0.0F) {
                continue;
            }

            float acceptableGC = electrical.receiveElectricity(inputSide, requestedGC, this.getTierGC(), false);
            if (acceptableGC <= 0.0F) {
                continue;
            }

            long requestedHE = hbmgc$gcToHBMEnergy(acceptableGC);
            if (requestedHE <= 0L) {
                continue;
            }

            long extractedHE = powerNet.extractPowerDiode(requestedHE, false);
            if (extractedHE <= 0L) {
                continue;
            }

            float availableGC = Math.min(acceptableGC, hbmgc$hbmToGCEnergy(extractedHE));
            electrical.receiveElectricity(inputSide, availableGC, this.getTierGC(), true);
        }
    }

    @Unique
    private float hbmgc$hbmToGCEnergy(long hbmEnergy) {
        return (float) (hbmEnergy * GeneralConfig.conversionRateHeToRF * EnergyConfigHandler.RF_RATIO);
    }

    @Unique
    private long hbmgc$gcToHBMEnergy(float gcEnergy) {
        double gcPerHE = GeneralConfig.conversionRateHeToRF * EnergyConfigHandler.RF_RATIO;
        if (gcPerHE <= 0.0D) {
            return 0L;
        }

        return (long) Math.ceil(gcEnergy / gcPerHE);
    }

    @Unique
    private boolean hbmgc$isCopperCable() {
        TileEntity self = hbmgc$self();
        World world = self.getWorld();
        if (world == null) {
            return false;
        }

        ResourceLocation registryName = world.getBlockState(self.getPos()).getBlock().getRegistryName();
        return registryName != null
                && "hbm".equals(registryName.getNamespace())
                && "red_cable".equals(registryName.getPath());
    }

    @Unique
    private TileEntity hbmgc$self() {
        return (TileEntity) (Object) this;
    }
}
