package jammerbam.hbmgccompanion;

import jammerbam.hbmgccompanion.client.GalacticraftCameraRestoreHandler;
import java.util.Arrays;
import java.util.List;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zone.rong.mixinbooter.ILateMixinLoader;

@Mod(modid="hbmgccompanion", acceptedMinecraftVersions="1.12.2")
public final class HBMGCLoadingPlugin
implements ILateMixinLoader {
    public static final String MODID = "hbmgccompanion";
    private static final Logger LOG = LogManager.getLogger((String)"hbmgccompanion");
    private static final String REFINERY_MIXINS = "mixins.hbmgccompanion.refinery.json";
    private static final String CE_MIXINS = "mixins.hbmgccompanion.ce.json";
    private static final String[] GC_IDS = new String[]{"galacticraftcore", "galacticraftlegacy"};
    private static final String[] HBM_IDS = new String[]{"hbm", "ntm", "hbmreloaded", "ntm_extended"};
    private static final String[] CE_LAUNCH_PAD_CLASSES = new String[]{"com.hbm.tileentity.bomb.TileEntityLaunchPadBase", "com.hbm.tileentity.bomb.TileEntityLaunchPadRusted"};

    public List<String> getMixinConfigs() {
        return Arrays.asList(REFINERY_MIXINS, CE_MIXINS);
    }

    public boolean shouldMixinConfigQueue(String mixinConfig) {
        boolean gcPresent = HBMGCLoadingPlugin.isAnyLoaded(GC_IDS);
        boolean hbmPresent = HBMGCLoadingPlugin.isAnyLoaded(HBM_IDS);
        if (!gcPresent || !hbmPresent) {
            LOG.debug("Skipping {} (Galacticraft present? {}, HBM present? {})", (Object)mixinConfig, (Object)gcPresent, (Object)hbmPresent);
            return false;
        }
        if (REFINERY_MIXINS.equals(mixinConfig)) {
            LOG.info("Queuing {} (HBM and Galacticraft detected).", (Object)mixinConfig);
            return true;
        }
        if (CE_MIXINS.equals(mixinConfig)) {
            boolean ceLaunchPadsPresent = HBMGCLoadingPlugin.areClassesPresent(CE_LAUNCH_PAD_CLASSES);
            if (!ceLaunchPadsPresent) {
                LOG.info("Skipping {} (HBM CE launch pad classes not detected; refinery compatibility only).", (Object)mixinConfig);
                return false;
            }
            LOG.info("Queuing {} (HBM CE launch pad classes detected).", (Object)mixinConfig);
            return true;
        }
        return false;
    }

    private static boolean isAnyLoaded(String[] ids) {
        for (String id : ids) {
            try {
                if (!Loader.isModLoaded((String)id)) continue;
                return true;
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return false;
    }

    private static boolean areClassesPresent(String[] classNames) {
        ClassLoader cl = HBMGCLoadingPlugin.class.getClassLoader();
        for (String className : classNames) {
            try {
                String path = className.replace('.', '/') + ".class";
                if (cl.getResource(path) == null) {
                    return false;
                }
            }
            catch (Throwable ignored) {
                return false;
            }
        }
        return true;
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        if (event.getSide() == Side.CLIENT && HBMGCLoadingPlugin.isAnyLoaded(GC_IDS) && HBMGCLoadingPlugin.isAnyLoaded(HBM_IDS)) {
            MinecraftForge.EVENT_BUS.register((Object)new GalacticraftCameraRestoreHandler());
        }
    }
}
