package omaloon.world.blocks.distribution;

import arc.graphics.g2d.*;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.util.*;
import mindustry.entities.units.*;
import mindustry.gen.Building;
import mindustry.world.blocks.distribution.*;
import mindustry.world.draw.*;
import omaloon.annotations.Load;

import static arc.Core.*;

public class TubeJunction extends Junction {
    public DrawBlock drawer = new DrawDefault();
    @Load("@-side1")
    public TextureRegion side1;
    @Load("@-side2")
    public TextureRegion side2;
    @Load("@-bottom")
    protected TextureAtlas.AtlasRegion bottomRegion;
    protected int tempBlend = 0;

    public TubeJunction(String name) {
        super(name);
    }

    @Override
    public void load(){
        super.load();
        drawer.load(this);
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        super.drawPlanRegion(plan, list);
        tempBlend = 0;

        //O(N^2), awful
        list.each(other -> {
            if(other.block != null && other.block.acceptsItems){
                for(int i = 0; i < 4; i++){
                    if(other.x == plan.x + Geometry.d4x(i) * size && other.y == plan.y + Geometry.d4y(i) * size){
                        tempBlend |= (1 << i);
                    }
                }
            }
        });

        int blending = tempBlend;

        float x = plan.drawx(), y = plan.drawy();


        Draw.rect(bottomRegion, x, y);
        Draw.rect(region, x, y);

        //code duplication, awful
        for(int i = 0; i < 4; i ++){
            if((blending & (1 << i)) == 0){
                Draw.rect(i >= 2 ? side2 : side1, x, y, i * 90);

                if((blending & (1 << ((i + 1) % 4))) != 0){
                    Draw.rect(i >= 2 ? side2 : side1, x, y, i * 90);
                }

                if((blending & (1 << (Mathf.mod(i - 1, 4)))) != 0){
                    Draw.yscl = -1f;
                    Draw.rect(i >= 2 ? side2 : side1, x, y, i * 90);
                    Draw.yscl = 1f;
                }
            }
        }
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
