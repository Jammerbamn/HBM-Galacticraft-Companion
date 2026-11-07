package jammerbam.hbmgccompanion.mixins;

import api.hbm.energy.IEnergyConductor;
import api.hbm.energy.IEnergyConnector;
import api.hbm.energy.IPowerNet;
import com.hbm.config.GeneralConfig;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.network.energy.TileEntityCableBaseNT;
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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TileEntityCableBaseNT.class, remap = false)
abstract class EEHBMCopperCable_GCConductorMixin implements IConductor {

    @Unique
    private static final int hbmgc$HEAVY_ALUMINUM_WIRE_TIER = 2;

    @Unique
    private IElectricityNetwork hbmgc$network;

    @Unique
    private TileEntity[] hbmgc$adjacentConnections;

    @Unique
    private final IEnergyConnector hbmgc$gcAcceptorBridge = new GCNetworkAcceptorBridge();

    @Unique
    private IPowerNet hbmgc$subscribedPowerNet;

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
                                && !(adjacent instanceof IConductor && !(adjacent instanceof TileEntityCableBaseNT))
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
            if (!(adjacent instanceof TileEntityCableBaseNT)
                    || !(adjacent instanceof IConductor)
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
        if (type != NetworkType.POWER || !hbmgc$isCopperCable()) {
            return false;
        }

        TileEntity self = hbmgc$self();
        TileEntity adjacent = self.getWorld().getTileEntity(self.getPos().offset(direction));
        return !(adjacent instanceof IConductor && !(adjacent instanceof TileEntityCableBaseNT));
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

    @Inject(method = { "update", "func_73660_a" }, at = @At("RETURN"))
    private void hbmgc$bridgeGCPower(CallbackInfo ci) {
        TileEntity self = hbmgc$self();
        World world = self.getWorld();
        hbmgc$syncHBMBridgeSubscription();
        hbmgc$transferGCPowerToGCNetwork();
        hbmgc$transferGCPowerToHBMNetwork();

        if (world != null && world.getTotalWorldTime() % 20L == 0L) {
            hbmgc$refreshNetworkIfServer();
        }
    }

    @Inject(method = { "invalidate", "func_145843_s" }, at = @At("HEAD"))
    private void hbmgc$splitGCNetworkOnInvalidate(CallbackInfo ci) {
        TileEntity self = hbmgc$self();
        World world = self.getWorld();
        if (world != null && !world.isRemote && this.hbmgc$network != null) {
            this.hbmgc$network.split((IConductor) (Object) this);
            this.hbmgc$network = null;
            this.hbmgc$adjacentConnections = null;
        }

        hbmgc$unsubscribeHBMBridge();
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
            if (!electrical.canConnect(outputSide, NetworkType.POWER)) {
                continue;
            }
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
    private void hbmgc$transferGCPowerToHBMNetwork() {
        TileEntity self = hbmgc$self();
        World world = self.getWorld();
        IPowerNet powerNet = hbmgc$getValidPowerNet();
        if (world == null || world.isRemote || !this.canTransmit()
                || powerNet == null || GeneralConfig.conversionRateHeToRF <= 0.0D) {
            return;
        }

        for (EnumFacing side : EnumFacing.VALUES) {
            TileEntity adjacent = world.getTileEntity(self.getPos().offset(side));
            if (!(adjacent instanceof IElectrical)) {
                continue;
            }

            IElectrical electrical = (IElectrical) adjacent;
            EnumFacing outputSide = side.getOpposite();
            if (!electrical.canConnect(outputSide, NetworkType.POWER)) {
                continue;
            }
            float availableGC = electrical.getProvide(outputSide);
            if (availableGC <= 0.0F) {
                continue;
            }

            float extractableGC = electrical.provideElectricity(outputSide, availableGC, false);
            if (extractableGC <= 0.0F) {
                continue;
            }

            long offeredHE = hbmgc$gcToHBMEnergy(extractableGC);
            if (offeredHE <= 0L) {
                continue;
            }

            long rejectedHE = powerNet.transferPower(offeredHE);
            long acceptedHE = offeredHE - rejectedHE;
            if (acceptedHE <= 0L) {
                continue;
            }

            float acceptedGC = Math.min(extractableGC, hbmgc$hbmToGCEnergy(acceptedHE));
            electrical.provideElectricity(outputSide, acceptedGC, true);
        }
    }

    @Unique
    private void hbmgc$syncHBMBridgeSubscription() {
        IPowerNet powerNet = hbmgc$getValidPowerNet();
        if (!this.canTransmit() || powerNet == null) {
            hbmgc$unsubscribeHBMBridge();
            return;
        }

        if (this.hbmgc$subscribedPowerNet != null && this.hbmgc$subscribedPowerNet != powerNet) {
            this.hbmgc$subscribedPowerNet.unsubscribe(this.hbmgc$gcAcceptorBridge);
            this.hbmgc$subscribedPowerNet = null;
        }

        if (!powerNet.isSubscribed(this.hbmgc$gcAcceptorBridge)) {
            powerNet.subscribe(this.hbmgc$gcAcceptorBridge);
        }

        this.hbmgc$subscribedPowerNet = powerNet;
    }

    @Unique
    private void hbmgc$unsubscribeHBMBridge() {
        if (this.hbmgc$subscribedPowerNet != null
                && this.hbmgc$subscribedPowerNet.isSubscribed(this.hbmgc$gcAcceptorBridge)) {
            this.hbmgc$subscribedPowerNet.unsubscribe(this.hbmgc$gcAcceptorBridge);
        }

        this.hbmgc$subscribedPowerNet = null;
    }

    @Unique
    private IPowerNet hbmgc$getValidPowerNet() {
        IPowerNet powerNet = ((IEnergyConductor) (Object) this).getPowerNet();
        return powerNet != null && powerNet.isValid() ? powerNet : null;
    }

    @Unique
    private long hbmgc$getAdjacentGCRequestHE() {
        TileEntity self = hbmgc$self();
        World world = self.getWorld();
        if (world == null || world.isRemote || !this.canTransmit() || GeneralConfig.conversionRateHeToRF <= 0.0D) {
            return 0L;
        }

        long requestedHE = 0L;
        for (EnumFacing side : EnumFacing.VALUES) {
            TileEntity adjacent = world.getTileEntity(self.getPos().offset(side));
            if (!(adjacent instanceof IElectrical)) {
                continue;
            }

            IElectrical electrical = (IElectrical) adjacent;
            EnumFacing inputSide = side.getOpposite();
            if (!electrical.canConnect(inputSide, NetworkType.POWER)) {
                continue;
            }
            float requestedGC = electrical.getRequest(inputSide);
            if (requestedGC <= 0.0F) {
                continue;
            }

            float acceptableGC = electrical.receiveElectricity(inputSide, requestedGC, this.getTierGC(), false);
            if (acceptableGC > 0.0F) {
                requestedHE += hbmgc$gcToHBMEnergy(acceptableGC);
            }
        }

        return requestedHE;
    }

    @Unique
    private long hbmgc$transferHBMPowerToGCAcceptors(long offeredHE) {
        TileEntity self = hbmgc$self();
        World world = self.getWorld();
        if (offeredHE <= 0L || world == null || world.isRemote || !this.canTransmit()
                || GeneralConfig.conversionRateHeToRF <= 0.0D) {
            return offeredHE;
        }

        long remainingHE = offeredHE;
        for (EnumFacing side : EnumFacing.VALUES) {
            if (remainingHE <= 0L) {
                break;
            }

            TileEntity adjacent = world.getTileEntity(self.getPos().offset(side));
            if (!(adjacent instanceof IElectrical)) {
                continue;
            }

            IElectrical electrical = (IElectrical) adjacent;
            EnumFacing inputSide = side.getOpposite();
            if (!electrical.canConnect(inputSide, NetworkType.POWER)) {
                continue;
            }
            float offeredGC = hbmgc$hbmToGCEnergy(remainingHE);
            float requestedGC = electrical.getRequest(inputSide);
            if (offeredGC <= 0.0F || requestedGC <= 0.0F) {
                continue;
            }

            float acceptableGC = electrical.receiveElectricity(
                    inputSide,
                    Math.min(offeredGC, requestedGC),
                    this.getTierGC(),
                    false
            );
            if (acceptableGC <= 0.0F) {
                continue;
            }

            long transferHE = Math.min(remainingHE, hbmgc$gcToHBMEnergy(acceptableGC));
            if (transferHE <= 0L) {
                continue;
            }

            float transferredGC = electrical.receiveElectricity(
                    inputSide,
                    Math.min(acceptableGC, hbmgc$hbmToGCEnergy(transferHE)),
                    this.getTierGC(),
                    true
            );
            if (transferredGC > 0.0F) {
                remainingHE -= Math.min(transferHE, hbmgc$gcToHBMEnergy(transferredGC));
            }
        }

        return Math.max(0L, remainingHE);
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

    @Unique
    private final class GCNetworkAcceptorBridge implements IEnergyConnector {

        @Override
        public long transferPower(long power) {
            return EEHBMCopperCable_GCConductorMixin.this.hbmgc$transferHBMPowerToGCAcceptors(power);
        }

        @Override
        public boolean canConnect(ForgeDirection dir) {
            return dir != ForgeDirection.UNKNOWN;
        }

        @Override
        public long getPower() {
            return 0L;
        }

        @Override
        public long getMaxPower() {
            return EEHBMCopperCable_GCConductorMixin.this.hbmgc$getAdjacentGCRequestHE();
        }

        @Override
        public boolean isLoaded() {
            TileEntity self = EEHBMCopperCable_GCConductorMixin.this.hbmgc$self();
            World world = self.getWorld();
            return world != null && !self.isInvalid()
                    && EEHBMCopperCable_GCConductorMixin.this.hbmgc$isCopperCable();
        }
    }
}
