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

    public static final Interp itemInterp = t -> {
        float s = 2f * t - 1f;
        return 0.7f * s * s + 0.3f;
    };

    public TubeRouter(String name){
        super(name);
        rotate = true;
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        Draw.rect(bottomRegion, plan.drawx(), plan.drawy());
        Draw.rect(rotatorRegion, plan.drawx(), plan.drawy());
        Draw.rect(region, plan.drawx(), plan.drawy());
        if(sideRegion[plan.rotation > 1 ? 1 : 0].found()) Draw.rect(sideRegion[plan.rotation > 1 ? 1 : 0], plan.drawx(), plan.drawy(), plan.rotation * 90f);
    }

    @Override
    protected TextureRegion[] icons(){
        return new TextureRegion[]{
        Core.atlas.find(name + "-bottom"),
        Core.atlas.find(name + "-rotator"),
        region,
        Core.atlas.find(name + "-side0", name)
        };
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.itemsMoved, 60f / speed, StatUnit.itemsSecond);
    }

    public class TubeRouterBuild extends RouterBuild{
        public byte targetRot = (byte)rotation;
        public arc.struct.IntSeq buffer = new arc.struct.IntSeq();

        @Override
        public boolean acceptItem(Building source, Item item){
            return super.acceptItem(source, item) && source != front() && items.total() < itemCapacity;
        }

        @Override
        public void handleItem(Building source, Item item){
            buffer.add(item.id, source == null ? 0 : source.tile.pos());
            items.add(item, 1);
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
            int turn = 0;

            if(target != null && lastItem != null && lastInput != null){
                turn = Mathf.mod(relativeTo(target) + 1 - relativeTo(lastInput), 4) - 1;

                rot = turn * 90f * Mathf.clamp(time);
                float d = itemInterp.apply(Mathf.clamp(time));

                Draw.rect(
                lastItem.uiIcon,
                x + Angles.trnsx(rot + relativeTo(lastInput) * 90f, 4f * d),
                y + Angles.trnsy(rot + relativeTo(lastInput) * 90f, 4f * d),
                itemSize,
                itemSize
                );
            }

            Draw.z(Layer.block);

            float pushOffset = (turn == 1 ? 30f : (turn == -1 ? -30f : 0f));
            Drawf.spinSprite(rotatorRegion, x, y, rot + 45f + pushOffset);
            Draw.rect(region, x, y);
            if(sideRegion[rotation > 1 ? 1 : 0].found()) Draw.rect(sideRegion[rotation > 1 ? 1 : 0], x, y, rotdeg());
        }

        @Override
        public @Nullable Building getTileTarget(@Nullable Item item, Tile from, boolean set){
            if(item == null) return null;
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
            if(lastItem == null && buffer.size > 0){
                int id = buffer.removeIndex(0);
                int pos = buffer.removeIndex(0);
                lastItem = mindustry.Vars.content.item(id);
                lastInput = mindustry.Vars.world.tile(pos);
                time = 0f;
            }

            Building target = getTileTarget(lastItem, lastInput, false);

            if(lastItem != null && target != null){
                float speedMultiplier = 1f + (buffer.size / 2f);
                time += 1f / speed * delta() * speedMultiplier;

                if(time >= 1f || instantTransfer){
                    getTileTarget(lastItem, lastInput, true);
                    target.handleItem(this, lastItem);
                    items.remove(lastItem, 1);
                    lastItem = null;
                    if(time > 1f) time -= 1f; else time = 0f;
                }
            }
        }
    }
}
