package omaloon.world.blocks.distribution;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.blocks.distribution.*;
import omaloon.annotations.Annotations.*;

public class TubeJunction extends Junction{
    public @Load("@-bottom") TextureRegion bottomRegion;
    public @Load(value = "@-side#0$", lengths = {2}) TextureRegion[] sideRegion;

    protected int tempBlend = 0;

    public TubeJunction(String name){
        super(name);
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        super.drawPlanRegion(plan, list);
        tempBlend = 0;

        //O(N^2), awful
        list.each(other -> {
            if(other.block == null || (!other.block.acceptsItems && !other.block.outputsItems())){
                for(int i = 0; i < 4; i++){
                    int x = plan.x + Geometry.d4x(i), y = plan.y + Geometry.d4y(i);
                    if(x >= other.x - (other.block.size - 1) / 2 && x <= other.x + (other.block.size / 2) && y >= other.y - (other.block.size - 1) / 2 && y <= other.y + (other.block.size / 2)){
                        tempBlend |= (1 << i);
                    }
                }
            }
        });

        Draw.rect(bottomRegion, plan.drawx(), plan.drawy());
        Draw.rect(region, plan.drawx(), plan.drawy());

        for(int i = 0; i < 4; i++)
            if(((1 << i) | tempBlend) != 0){
                Draw.rect(i > 1 ? sideRegion[1] : sideRegion[0], plan.drawx(), plan.drawy(), i * 90f);
            }
    }

    @Override
    protected TextureRegion[] icons(){
        return new TextureRegion[]{
        Core.atlas.find(name + "-bottom"),
        region
        };
    }

    public class TubeJunctionBuild extends JunctionBuild{
        @Override
        public void draw(){
            Draw.z(Layer.block - 0.2f);
            Draw.rect(bottomRegion, x, y);
            Draw.z(Layer.block);
            Draw.rect(region, x, y);
            for(int i = 0; i < 4; i++){
                Building b = nearby(i);
                if(b == null || (!b.block.acceptsItems && !b.block.outputsItems())){
                    Draw.rect(i > 1 ? sideRegion[1] : sideRegion[0], x, y, i * 90f);
                }
            }
        }
    }
}