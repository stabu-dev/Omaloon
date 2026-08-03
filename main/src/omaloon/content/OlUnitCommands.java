package omaloon.content;

import mindustry.ai.*;
import omaloon.ai.*;

public class OlUnitCommands{
    public static UnitCommand healCommand;

    public static void load(){
        healCommand = new UnitCommand("heal", "modeSurvival", u -> new HealAI());
    }
}