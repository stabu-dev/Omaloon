package ol.content;

import ol.content.blocks.*;

public class OlBlocks {
    public static void load(){
        OlEnvironmentBlocks.load();
        OlPowerBlocks.load();
        OlProductionBlocks.load();
        OlDeffenceBlocks.load();
    }
}
