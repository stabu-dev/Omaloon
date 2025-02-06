package omaloon.core;

import arc.*;
import arc.struct.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.meta.*;
import omaloon.content.*;

public class SafeClearer implements ApplicationListener{
    public static final Seq<Building> invalidBuilds = new Seq<>();

    public SafeClearer(){
        if(Vars.platform instanceof ApplicationCore core) core.add(this);
    }

    public boolean enabled(){
        return Core.settings.getBool("omaloon-enable-soft-cleaner", true);
    }

    @Override
    public void update(){
        if(enabled()){
            invalidBuilds.clear();
            if(Vars.state.isCampaign() && Vars.state.getPlanet().solarSystem == OlPlanets.omaloon){
                Block out = Vars.content.blocks().find(block -> {
                    boolean omaloonOnly = block.minfo.mod != null && block.minfo.mod.name.equals("omaloon");
                    boolean sandboxOnly = block.buildVisibility == BuildVisibility.sandboxOnly || block.buildVisibility == BuildVisibility.editorOnly;
                    boolean empty = Vars.player.team().data().getBuildings(block).isEmpty();

                    return !omaloonOnly && !sandboxOnly && !empty && !(block instanceof ConstructBlock);
                });
                if(out != null) invalidBuilds.add(Vars.player.team().data().getBuildings(out));
                invalidBuilds.removeAll(b -> b instanceof ConstructBlock.ConstructBuild);
            }

            if(!invalidBuilds.isEmpty()){
                invalidBuilds.each(build -> {
                    OlFx.stealInvalid.at(build.x, build.y, 0, build.block);
                    build.tile.setAir();
                });
            }
        }
    }
}
