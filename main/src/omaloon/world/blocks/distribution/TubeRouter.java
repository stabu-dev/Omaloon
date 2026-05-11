package omaloon.world.blocks.distribution;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.*;
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

    public static final Interp itemInterp = t -> (float)Math.sqrt(t * t + (1f - t) * (1f - t));

    public TubeRouter(String name){
        super(name);
        rotate = true;
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        Draw.rect(bottomRegion, plan.drawx(), plan.drawy());
        Draw.rect(rotatorRegion, plan.drawx(), plan.drawy());
        Draw.rect(region, plan.drawx(), plan.drawy());

        TextureRegion side = sideRegion[plan.rotation > 1 ? 1 : 0];
        if(side.found()) Draw.rect(side, plan.drawx(), plan.drawy(), plan.rotation * 90f);
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
        public @Nullable Item lastItem;
        public @Nullable Tile lastInput;
        public float time;
        public byte targetRot = (byte)rotation;
        public float[] lastFlow = new float[4];

        public @Nullable Building visualTarget;
        public float visualTurn;

        @Override
        public void updateTile(){
            if(lastItem == null && items.any()) setupItem(items.first());

            if(lastItem != null){
                Building target = getTileTarget(lastItem, lastInput, false);

                if(target != null && target != visualTarget){
                    visualTarget = target;
                    visualTurn = turnTo(relativeTo(visualTarget));
                }

                time = Math.min(time + 1f / speed * delta(), visualTarget != null ? 1f : 0.5f);

                if(target != null && time >= 1f){
                    getTileTarget(lastItem, lastInput, true);
                    target.handleItem(this, lastItem);

                    int rel = relativeTo(target);
                    if(rel >= 0) lastFlow[rel] = Time.time;

                    items.remove(lastItem, 1);
                    lastItem = null;
                    visualTarget = null;
                    time = 0f;
                }
            }
        }

        protected void setupItem(Item item){
            lastItem = item;
            time = 0f;
            visualTarget = getPredictedTarget(item);
            visualTurn = turnTo(visualTarget != null ? relativeTo(visualTarget) : rotation);
        }

        protected float turnTo(int direction){
            return lastInput == null ? 0f : Mathf.mod(direction - relativeTo(lastInput) + 1, 4) - 1;
        }

        public @Nullable Building getPredictedTarget(Item item){
            Building firstTransport = null;

            for(int i = 0; i < proximity.size; i++){
                Building other = proximity.get((i + targetRot) % proximity.size);
                if(lastInput != null && other.tile == lastInput) continue;

                if(other.acceptItem(this, item)) return other;

                if(other.team == team && other.block.group == BlockGroup.transportation){
                    int rel = relativeTo(other);
                    if(rel >= 0 && Time.time - lastFlow[rel] <= 15f) return other;
                    if(firstTransport == null) firstTransport = other;
                }
            }
            return firstTransport;
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            return super.acceptItem(source, item) && front() != source;
        }

        @Override
        public void handleItem(Building source, Item item){
            items.add(item, 1);
            lastInput = source.tile;
            setupItem(item);
        }

        @Override
        public int removeStack(Item item, int amount){
            int result = super.removeStack(item, amount);
            if(result != 0 && item == lastItem) lastItem = null;
            return result;
        }

        public void drawRotator(float rotation){
            Drawf.spinSprite(rotatorRegion, x, y, rotation + 45f);
        }

        @Override
        public void draw(){
            Draw.z(Layer.block - 0.2f);
            Draw.rect(bottomRegion, x, y);

            Draw.z(Layer.block - 0.1f);
            if(lastInput != null && lastItem != null){
                float ctime = Mathf.clamp(time);
                float drawAngle = visualTurn * 90f * ctime + relativeTo(lastInput) * 90f;
                Tmp.v1.trns(drawAngle, size * 4f * Mathf.lerp(1f, itemInterp.apply(ctime), Math.abs(visualTurn)));
                Draw.rect(lastItem.uiIcon, x + Tmp.v1.x, y + Tmp.v1.y, itemSize, itemSize);
            }

            Draw.z(Layer.block);
            float rot = visualTurn * 90f * Mathf.clamp(time);
            drawRotator(rot);
            Draw.rect(region, x, y);

            TextureRegion side = sideRegion[rotation > 1 ? 1 : 0];
            if(side.found()) Draw.rect(side, x, y, rotdeg());
        }

        @Override
        public @Nullable Building getTileTarget(@Nullable Item item, Tile from, boolean set){
            if(item == null) return null;

            for(int i = 0; i < proximity.size; i++){
                Building other = proximity.get((i + targetRot) % proximity.size);

                if(other.tile == from && from.block() == Blocks.overflowGate) continue;
                if(other.acceptItem(this, item)){
                    if(set) targetRot = (byte)((targetRot + i + 1) % proximity.size);
                    return other;
                }
            }
            return null;
        }
    }
}