package omaloon.world.blocks.distribution;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.blocks.distribution.*;
import omaloon.annotations.Annotations.*;

public class TubeJunction extends Junction{
    public @Load(value = "@-side#0$", lengths = {2}) TextureRegion[] sideRegion;

    protected int tempBlend = 0;

    public TubeJunction(String name){
        super(name);
        squareSprite = false;
        drawCached = false;
        drawDynamic = true;
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

        Draw.rect(region, plan.drawx(), plan.drawy());

        for(int i = 0; i < 4; i++)
            if(((1 << i) | tempBlend) != 0){
                Draw.rect(i > 1 ? sideRegion[1] : sideRegion[0], plan.drawx(), plan.drawy(), i * 90f);
            }
    }

    @Override
    protected TextureRegion[] icons(){
        return new TextureRegion[]{region};
    }

    public class TubeJunctionBuild extends JunctionBuild{
        @Override
        public void draw(){
            Draw.z(Layer.block);
            Draw.rect(region, x, y);
            for(int i = 0; i < 4; i++){
                Building b = nearby(i);
                if(b == null || (!b.block.acceptsItems && !b.block.outputsItems())){
                    Draw.rect(i > 1 ? sideRegion[1] : sideRegion[0], x, y, i * 90f);
                }
            }
            drawItems();
        }

        public void drawItems(){
            for(int i = 0; i < 4; i++){
                for(int j = 0; j < buffer.indexes[i]; j++){
                    long l = buffer.buffers[i][j];
                    Item item = Vars.content.item(BufferItem.item(l));
                    if(item == null) continue;

                    float progress = Mathf.clamp((Time.time - BufferItem.time(l)) / speed, 0f, 1f);
                    float offset = Mathf.lerp(-Vars.tilesize / 2f, Vars.tilesize / 2f, progress);
                    float ix = x + Geometry.d4x(i) * offset, iy = y + Geometry.d4y(i) * offset;
                    Draw.z(Layer.block - 0.1f + (ix / Vars.world.unitWidth() + iy / Vars.world.unitHeight()) * 0.01f);
                    Draw.rect(item.fullIcon, ix, iy, Vars.itemSize, Vars.itemSize);
                }
            }
        }
    }
}