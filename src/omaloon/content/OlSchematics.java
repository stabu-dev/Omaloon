package omaloon.content;

import arc.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.world.blocks.storage.*;
import omaloon.content.blocks.*;

public class OlSchematics{
    public static Schematic

        landingCapsule,
        coreFloe;

    public static void load(){
        landingCapsule = Schematics.readBase64("bXNjaAF4nGNgYmBiZmDJS8xNZeD3ScxLycxLV3BOLCguzUll4E5JLU4uyiwoyczPY2BgYMtJTErNKWZgio5lZBDPz03Myc/P082BaNJNhmpiYGBkAANGAJEyGHs=");
        coreFloe = Schematics.readBase64("bXNjaAF4nGNgZmBmZmDJS8xNZeB0zi9KVXDLyU9l4E5JLU4uyiwoyczPY2BgYMtJTErNKWZgio5lZBDMz03Myc/P000GKtdNAylnYGAEISAEADBpE30=");
        Events.run(EventType.Trigger.update, () -> {
            if(!Vars.schematics.getLoadouts((CoreBlock)OlStorageBlocks.coreFloe).contains(coreFloe)){
                Vars.schematics.getLoadouts((CoreBlock)OlStorageBlocks.coreFloe).clear();
                Vars.schematics.getLoadouts((CoreBlock)OlStorageBlocks.coreFloe).add(coreFloe);
            }
            if(!Vars.schematics.getLoadouts((CoreBlock)OlStorageBlocks.landingCapsule).contains(landingCapsule)){
                Vars.schematics.getLoadouts((CoreBlock)OlStorageBlocks.landingCapsule).clear();
                Vars.schematics.getLoadouts((CoreBlock)OlStorageBlocks.landingCapsule).add(landingCapsule);
            }
            if(Vars.ui.planet.state.planet.solarSystem == OlPlanets.omaloon){
                if(Vars.ui.planet.selected == OlSectorPresets.theCrater.sector){
                    OlPlanets.glasmore.defaultCore = OlStorageBlocks.landingCapsule;
                    OlPlanets.glasmore.generator.defaultLoadout = landingCapsule;
                }else{
                    OlPlanets.glasmore.defaultCore = OlStorageBlocks.coreFloe;
                    OlPlanets.glasmore.generator.defaultLoadout = coreFloe;
                }
            }
        });
    }
}
