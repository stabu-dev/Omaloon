package omaloon.content;

import omaloon.content.blocks.OlDistributionBlocks;
import omaloon.content.blocks.OlEnvironmentBlocks;
import omaloon.content.blocks.OlPowerBlocks;
import omaloon.content.blocks.OlStorageBlocks;

public class OlBlocks {
    public static void load(){
        OlEnvironmentBlocks.load();
        OlStorageBlocks.load();
        OlDistributionBlocks.load();
        OlPowerBlocks.load();
    }

}
