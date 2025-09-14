package omaloon.content.blocks;

import mindustry.world.*;
import omaloon.world.blocks.sandbox.*;

public class OlSandboxBlocks{
    public static Block pressureSource;

    public static void load() {
        pressureSource = new PressureSource("pressure-source");
    }
}
