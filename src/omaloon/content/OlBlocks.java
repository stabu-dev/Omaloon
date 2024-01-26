package omaloon.content;

import omaloon.content.blocks.*;

public class OlBlocks {

    public static void load(){
        OlEnvironmentBlocks.load();
        OlStorageBlocks.load();
        OlDistributionBlocks.load();
        OlPowerBlocks.load();
    }

}
