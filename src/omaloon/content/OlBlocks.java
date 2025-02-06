package omaloon.content;

import omaloon.content.blocks.*;

public class OlBlocks{
    public static void load(){
        OlEnvironmentBlocks.load();
        OlDefenceBlocks.load();
        OlStorageBlocks.load();
        OlProductionBlocks.load();
        OlDistributionBlocks.load();
        OlPowerBlocks.load();
        OlCraftingBlocks.load();
        OlSandboxBlocks.load();
    }
}
