package omaloon.content;

import omaloon.content.blocks.*;

public class OlBlocks{
    public static void load(){
        OlEnvironmentBlocks.load();
        OlDistributionBlocks.load();
        OlCraftingBlocks.load();
        OlPowerBlocks.load();
        OlProductionBlocks.load();
        OlDefenceBlocks.load();
        OlSandboxBlocks.load();
        OlStorageBlocks.load();
    }
}
