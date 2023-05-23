package ol.content;

import ol.content.blocks.*;

public class OlBlocks {
    public static void load() {
        OlDistributionBlocks.load();
        OlEnvironmentBlocks.load();
        OlStorageBlocks.load();
        OlPowerBlocks.load();
        OlProductionBlocks.load();
        OlDeffenceBlocks.load();
    }
}
