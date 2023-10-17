package omaloon.world.blocks.distribution;

import arc.graphics.g2d.*;
import arc.util.*;
import mindustry.entities.units.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.draw.*;

public class TubeJunction extends Junction {
    public DrawBlock drawer = new DrawDefault();

    public TubeJunction(String name) {
        super(name);
    }

    @Override
    public TextureRegion[] icons(){
        return drawer.finalIcons(this);
    }

    @Override
    public void load(){
        super.load();
        drawer.load(this);
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        drawer.drawPlan(this, plan, list);
    }

    public class TubeJunctionBuild extends JunctionBuild{

        @Override
        public void draw(){
            drawer.draw(this);
        }

        @Override
        public void drawLight(){
            super.drawLight();
            drawer.drawLight(this);
        }
    }
}
