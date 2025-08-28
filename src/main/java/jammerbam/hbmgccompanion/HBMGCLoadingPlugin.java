package jammerbam.hbmgccompanion;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.Collections;
import java.util.List;


@Mod(
        modid = HBMGCLoadingPlugin.MODID,
        acceptedMinecraftVersions = "1.12.2"
)

public final class HBMGCLoadingPlugin implements ILateMixinLoader {
    public static final String MODID = "hbmgccompanion";
    private static final Logger LOG = LogManager.getLogger("hbmgccompanion");

    // Cover common mod IDs across the 1.12.2 forks/variants.
    private static final String[] GC_IDS  = { "galacticraftcore", "galacticraftlegacy" };
    private static final String[] HBM_IDS = { "hbm", "ntm", "hbmreloaded", "ntm_extended" };

    @Override
    public List<String> getMixinConfigs() {
        // Path is relative to resources root (see mixin JSON below)
        return Collections.singletonList("mixins.hbmgccompanion.json");
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
        LOG.info("Queuing {} (both HBM and Galacticraft detected).", mixinConfig);
        return true;
    }

    private static boolean isAnyLoaded(String[] ids) {
        for (String id : ids) {
            try {
                if (Loader.isModLoaded(id)) return true;
            } catch (Throwable ignored) { }
        }
        return false;
    }
}
