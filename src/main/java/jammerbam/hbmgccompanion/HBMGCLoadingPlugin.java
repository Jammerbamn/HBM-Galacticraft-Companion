package jammerbam.hbmgccompanion;

import jammerbam.hbmgccompanion.client.GalacticraftCameraRestoreHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.Arrays;
import java.util.List;


@Mod(
        modid = HBMGCLoadingPlugin.MODID,
        acceptedMinecraftVersions = "1.12.2"
)

public final class HBMGCLoadingPlugin implements ILateMixinLoader {
    public static final String MODID = "hbmgccompanion";
    private static final Logger LOG = LogManager.getLogger("hbmgccompanion");
    private static final String REFINERY_MIXINS = "mixins.hbmgccompanion.refinery.json";
    private static final String CE_MIXINS = "mixins.hbmgccompanion.ce.json";

    // Cover common mod IDs across the 1.12.2 forks/variants.
    private static final String[] GC_IDS  = { "galacticraftcore", "galacticraftlegacy" };
    private static final String[] HBM_IDS = { "hbm", "ntm", "hbmreloaded", "ntm_extended" };
    private static final String[] CE_LAUNCH_PAD_CLASSES = {
            "com.hbm.tileentity.bomb.TileEntityLaunchPadBase",
            "com.hbm.tileentity.bomb.TileEntityLaunchPadRusted"
    };

    @Override
    public List<String> getMixinConfigs() {
        // Paths are relative to the resources root.
        return Arrays.asList(REFINERY_MIXINS, CE_MIXINS);
    }

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        boolean gcPresent  = isAnyLoaded(GC_IDS);
        boolean hbmPresent = isAnyLoaded(HBM_IDS);
        if (!gcPresent || !hbmPresent) {
            LOG.debug("Skipping {} (Galacticraft present? {}, HBM present? {})",
                    mixinConfig, gcPresent, hbmPresent);
            return false;
        }

        if (REFINERY_MIXINS.equals(mixinConfig)) {
            LOG.info("Queuing {} (HBM and Galacticraft detected).", mixinConfig);
            return true;
        }

        if (CE_MIXINS.equals(mixinConfig)) {
            boolean ceLaunchPadsPresent = areClassesPresent(CE_LAUNCH_PAD_CLASSES);
            if (!ceLaunchPadsPresent) {
                LOG.info("Skipping {} (HBM CE launch pad classes not detected; refinery compatibility only).", mixinConfig);
                return false;
            }

            LOG.info("Queuing {} (HBM CE launch pad classes detected).", mixinConfig);
            return true;
        }

        return false;
    }

    private static boolean isAnyLoaded(String[] ids) {
        for (String id : ids) {
            try {
                if (Loader.isModLoaded(id)) return true;
            } catch (Throwable ignored) { }
        }
        return false;
    }

    private static boolean areClassesPresent(String[] classNames) {
        for (String className : classNames) {
            try {
                Class.forName(className, false, HBMGCLoadingPlugin.class.getClassLoader());
            } catch (Throwable ignored) {
                return false;
            }
        }

        return true;
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        if (event.getSide() == Side.CLIENT && areClassesPresent(CE_LAUNCH_PAD_CLASSES)) {
            MinecraftForge.EVENT_BUS.register(new GalacticraftCameraRestoreHandler());
        }
    }
}
