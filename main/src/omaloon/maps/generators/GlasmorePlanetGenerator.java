package omaloon.maps.generators;

import mindustry.content.*;
import mindustry.game.*;
import mindustry.maps.generators.*;

public class GlasmorePlanetGenerator extends BlankPlanetGenerator{
    @Override
    protected void generate(){
        pass((x, y) -> {
            floor = Blocks.grass;
            block = ore = Blocks.air;
        });

        Schematics.place(Loadouts.basicShard, width/2, height/2, Team.sharded);
    }
}
