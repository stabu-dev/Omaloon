package omaloon.world.blocks.distribution;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.meta.*;
import omaloon.annotations.Annotations.*;

import static mindustry.Vars.itemSize;

public class TubeRouter extends Router{
    public @Load("@-bottom") TextureRegion bottomRegion;
    public @Load("@-rotator") TextureRegion rotatorRegion;
    public @Load(value = "@-side#0$", lengths = {2}) TextureRegion[] sideRegion;

    public TubeRouter(String name){
        super(name);
        rotate = true;
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        Draw.rect(bottomRegion, plan.drawx(), plan.drawy());
        Draw.rect(region, plan.drawx(), plan.drawy());
        Draw.rect(sideRegion[plan.rotation > 1 ? 1 : 0], plan.drawx(), plan.drawy(), plan.rotation * 90f);
    }

    @Override
    protected TextureRegion[] icons(){
        return new TextureRegion[]{
        Core.atlas.find(name + "-bottom"),
        Core.atlas.find(name + "-rotator"),
        region,
        Core.atlas.find(name + "-side0")
        };
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.itemsMoved, 60f/speed, StatUnit.itemsSecond);
    }

    public class TubeRouterBuild extends RouterBuild{
        public byte targetRot = (byte)rotation;

        @Override
        public boolean acceptItem(Building source, Item item){
            return super.acceptItem(source, item) && source != front();
        }

        @Override
        public boolean canControl(){
            return false;
        }

        @Override
        public void draw(){
            Draw.z(Layer.block - 0.2f);
            Draw.rect(bottomRegion, x, y);

            Draw.z(Layer.block - 0.1f);
            Building target = getTileTarget(lastItem, lastInput, false);
            float rot = 0f;

            if (target != null && lastItem != null) {
                int turn = Mathf.mod(relativeTo(target) + 1 - relativeTo(lastInput), 4) - 1;

                rot = turn * 90f * Mathf.clamp(time);

                // TODO make a proper Interp for this
                float h = 0.3f;
                float c = 2f;
                float d = (1f - h) * Mathf.pow(2f * Mathf.clamp(time) - 1f, c) + h;

                Draw.rect(
                    lastItem.uiIcon,
                    x + Angles.trnsx(rot + relativeTo(lastInput) * 90f, 4f * d),
                    y + Angles.trnsy(rot + relativeTo(lastInput) * 90f, 4f * d),
                    itemSize,
                    itemSize
                );
            }

            Draw.z(Layer.block);

            Drawf.spinSprite(rotatorRegion, x, y, rot + 45f);
            Draw.rect(region, x, y);
            Draw.rect(sideRegion[rotation > 1 ? 1 : 0], x, y, rotdeg());
        }

        @Override
        public @Nullable Building getTileTarget(@Nullable Item item, Tile from, boolean set){
            if (item == null) return null;
            int counter = targetRot;
            for(int i = 0; i < proximity.size; i++){
                Building other = proximity.get((i + counter) % proximity.size);
                if(set) targetRot = ((byte)((targetRot + 1) % proximity.size));
                if(other.tile == from && from.block() == Blocks.overflowGate) continue;
                if(other.acceptItem(this, item)){
                    return other;
                }
            }
            return null;
        }

        @Override
        public void updateTile(){
            if(lastItem == null && items.any()){
                lastItem = items.first();
            }

            Building target = getTileTarget(lastItem, lastInput, false);

            if(lastItem != null && target != null){
                time += 1f / speed * delta();

                if(time >= 1f || instantTransfer){
                    getTileTarget(lastItem, lastInput, true);
                    target.handleItem(this, lastItem);
                    items.remove(lastItem, 1);
                    lastItem = null;
                }
            }
        }
    }
}
