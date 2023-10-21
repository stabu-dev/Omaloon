package omaloon.world.blocks.distribution;

import arc.graphics.g2d.*;
import arc.util.*;
import mindustry.entities.units.*;
import mindustry.gen.Building;
import mindustry.world.blocks.distribution.*;
import mindustry.world.draw.*;

import static arc.Core.atlas;

public class TubeJunction extends Junction {
    public DrawBlock drawer = new DrawDefault();
    public TextureRegion side1, side2;

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
        side1 = atlas.find(name + "-side1");
        side2 = atlas.find(name + "-side2");
        uiIcon = atlas.find(name + "-icon");
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        drawer.drawPlan(this, plan, list);
    }

    public class TubeJunctionBuild extends JunctionBuild{

        public Building buildAt(int i) {
            return nearby(i);
        }

        public boolean valid(int i) {
            Building b = buildAt(i);
            return b != null && b.block.acceptsItems;
        }

        @Override
        public void draw() {
            super.draw();
            drawer.draw(this);
            for(int i = 0; i < 4; i++) {
                if(!valid(i)){
                    Draw.rect(i >= 2 ? side2 : side1, x, y, i * 90);
                }
            }
        }

        @Override
        public void drawLight(){
            super.drawLight();
            drawer.drawLight(this);
        }
    }
}
