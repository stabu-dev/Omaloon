package ol.content;

import ol.content.blocks.*;

public class OlBlocks {
    public static void load() {
        OlPressureBlocks.load();
        OlEnvironmentBlocks.load();
        OlMiningBlocks.load();
        OlDistributionBlocks.load();
        OlStorageBlocks.load();
        OlPowerBlocks.load();
        OlProductionBlocks.load();
        OlDeffenceBlocks.load();
    }
}
