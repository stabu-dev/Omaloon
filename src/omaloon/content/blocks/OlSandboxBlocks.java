package omaloon.content.blocks;

import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import omaloon.world.blocks.sandbox.*;

public class OlSandboxBlocks{
    public static Block pressureSource/*, pressureVoid*/;

    public static void load(){
        pressureSource = new PressureLiquidSource("pressure-source"){{
            buildVisibility = BuildVisibility.sandboxOnly;
            category = Category.liquid;
        }};
		/*
		pressureVoid = new PressureLiquidVoid("pressure-void") {{
			buildVisibility = BuildVisibility.sandboxOnly;
			category = Category.liquid;
		}};
		*/
    }
}
