package omaloon.content.blocks;

import mindustry.type.*;
import mindustry.world.*;
import omaloon.world.blocks.power.*;

import static mindustry.type.ItemStack.*;

public class OlPowerBlocks {
    public static Block
            windTurbine,

    end;

    public static void load(){
        windTurbine = new WindGenerator("wind-turbine"){{
            requirements(Category.power, empty);
            size = 1;
            powerProduction = 0.2f;
        }};
    }
}
