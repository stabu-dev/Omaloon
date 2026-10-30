package omaloon.content.blocks;

import mindustry.world.*;
import omaloon.world.blocks.sandbox.*;

public class OlSandboxBlocks{
    public static Block pressureSource, nuke;

    public static void load(){
        pressureSource = new PressureSource("pressure-source");

        nuke = new NuclearBomb("nuke"){{
            size = 2;
            health = 400;
        }};
    }
}
