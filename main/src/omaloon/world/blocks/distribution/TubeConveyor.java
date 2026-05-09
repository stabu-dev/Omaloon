package omaloon.world.blocks.distribution;

import arc.*;
import arc.func.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.sandbox.*;
import mindustry.world.blocks.storage.*;
import mindustry.world.meta.*;
import omaloon.annotations.Annotations.*;

public class TubeConveyor extends Conveyor{
    private static final float itemSpace = 0.4f;
    private static final int capacity = 3;

    public @Load(value = "@-#0$", lengths = {16}) TextureRegion[] topRegion;
    public @Load(value = "@-cap") TextureRegion capRegion;

    public TubeConveyor(String name){
        super(name);
        pushUnits = false;
    }

    @Override
    public boolean blends(Tile tile, int rotation, int otherx, int othery, int otherrot, Block otherblock){
        return super.blends(tile, rotation, otherx, othery, otherrot, otherblock) && validBlock(otherblock);
    }

    @Override
    public Block getReplacement(BuildPlan req, Seq<BuildPlan> plans){
        if(junctionReplacement == null) return this;

        Boolf<Point2> cont = p -> plans.contains(o -> o.x == req.x + p.x && o.y == req.y + p.y
        && (req.block instanceof TubeConveyor || req.block instanceof Junction));
        return cont.get(Geometry.d4(req.rotation))
        && cont.get(Geometry.d4(req.rotation - 2))
        && req.tile() != null
        && req.tile().block() instanceof TubeConveyor
        && Mathf.mod(req.tile().build.rotation - req.rotation, 2) == 1
        ? junctionReplacement
        : this;
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{
        Core.atlas.find(name + "-0-0"),
        Core.atlas.find(name + "-0")
        };
    }

    public boolean validBlock(Block otherblock){
        return
        (otherblock.group == BlockGroup.transportation || (otherblock instanceof CoreBlock) || (otherblock instanceof ItemSource) || (otherblock instanceof ItemVoid));
    }

    @Override
    public void drawPlanRegion(BuildPlan req, Eachable<BuildPlan> list){
        super.drawPlanRegion(req, list);
        BuildPlan[] directionals = new BuildPlan[4];
        Building[] builds = new Building[4];
        boolean[] connections = new boolean[4];

        Block.findPlan(list, req.x, req.y, req.block.size + 2, other -> {
            if(other.breaking || other == req) return false;

            int i = 0;
            for(Point2 point : Geometry.d4){
                int x = req.x + point.x, y = req.y + point.y;
                if(x >= other.x - (other.block.size - 1) / 2 && x <= other.x + (other.block.size / 2) && y >= other.y - (other.block.size - 1) / 2 && y <= other.y + (other.block.size / 2)){
                    directionals[i] = other;
                    connections[i] = planConnects(req, other, i);
                }
                i++;
            }

            return false;
        });

        int mask = 0;
        for(int i = 0; i < directionals.length; i++){
            if(directionals[i] == null){
                Tile otherTile = Vars.world.tile(req.x + Geometry.d4x(i), req.y + Geometry.d4y(i));
                builds[i] = otherTile == null ? null : otherTile.build;
                connections[i] = buildConnects(req, builds[i], i);
            }

            if(connections[i]){
                mask += (1 << i);
            }
        }
        mask |= (1 << req.rotation);
        Draw.rect(topRegion[mask], req.drawx(), req.drawy(), 0);

        if(planFrontEnd(req, directionals[req.rotation], builds[req.rotation])){
            if(req.rotation > 0 && req.rotation < 3) Draw.yscl = -1f;
            Draw.rect(capRegion, req.drawx(), req.drawy(), req.rotation * 90f);
            Draw.scl();
        }

    }

    public boolean planConnects(BuildPlan req, BuildPlan other, int direction){
        return validBlock(other.block) && (other.block instanceof Conveyor ?
        (req.rotation == direction || (other.rotation + 2) % 4 == direction) :
        ((req.rotation == direction && other.block.acceptsItems) || (req.rotation != direction && other.block.outputsItems())));
    }

    public boolean buildConnects(BuildPlan req, Building other, int direction){
        return other != null && validBlock(other.block) && (other instanceof Conveyor.ConveyorBuild ?
        (req.rotation == direction || (other.rotation + 2) % 4 == direction) :
        ((req.rotation == direction && other.block.hasItems) || (req.rotation != direction && other.block.outputsItems())));
    }

    public boolean planFrontEnd(BuildPlan req, BuildPlan plan, Building build){
        if(plan != null){
            return !validBlock(plan.block) || (plan.block instanceof Conveyor ?
            (plan.rotation + 2) % 4 == req.rotation :
            !plan.block.acceptsItems);
        }

        return build == null || !validBlock(build.block) || (build instanceof Conveyor.ConveyorBuild ?
        (build.rotation + 2) % 4 == req.rotation :
        !build.block.hasItems);
    }

    public class TubeConveyorBuild extends ConveyorBuild{
        public int tiling = 0;
        public int lastRotation = -1;
        public float rotationGrace = 0f;

        public int maxItems(){
            if(isEnd(rotation) || isEnd(rotation + 2)) return 2;
            return capacity;
        }

        public void normalizeItemPositions(){
            float min = isEnd(rotation + 2) ? itemSpace : 0f;
            float maxPos = isEnd(rotation) ? 1f - itemSpace : 1f;

            for(int i = 0; i < len; i++){
                ys[i] = Mathf.clamp(ys[i], min, maxPos);
            }
        }

        public void syncState(){
            if(lastRotation != -1 && lastRotation != rotation) rotationGrace = 60f;
            lastRotation = rotation;
            normalizeItemPositions();
        }

        public void ensureState(){
            if(lastRotation != rotation){
                int[] bits = buildBlending(tile, rotation, null, true);
                blendbits = bits[0];
                blendsclx = bits[1];
                blendscly = bits[2];
                blending = bits[4];

                next = front();
                nextc = next instanceof ConveyorBuild && next.team == team ? (ConveyorBuild)next : null;
                aligned = nextc != null && rotation == next.rotation;
            }
            syncState();
        }

        public boolean valid(int i){
            Building b = nearby(i);
            return b != null && b.team == team && validBlock(b.block);
        }

        public boolean isEnd(int i){
            i = Mathf.mod(i, 4);
            Building b = nearby(i);

            if(!valid(i)) return true;

            if(i == rotation){
                return !b.block.hasItems || (b instanceof ConveyorBuild && b.front() == this);
            }

            if(i == Mathf.mod(rotation + 2, 4)){
                return !b.block.outputsItems() || (b instanceof ConveyorBuild && b.front() != this);
            }

            return false;
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            ensureState();
            if(rotationGrace <= 0f && len >= maxItems()) return false;
            return super.acceptItem(source, item) && validBlock(source.block);
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source){
            ensureState();
            if(source instanceof Building b && !validBlock(b.block)) return 0;

            int capacityLeft = rotationGrace > 0f ? capacity - len : maxItems() - len;
            return Math.max(Math.min(super.acceptStack(item, amount, source), capacityLeft), 0);
        }

        @Override
        public void updateTile(){
            minitem = 1f;
            mid = 0;
            ensureState();
            rotationGrace = Math.max(rotationGrace - edelta(), 0f);

            if(len == 0 && Mathf.equal(timeScale, 1f)){
                clogHeat = 0f;
                sleep();
                return;
            }

            float nextMax = aligned ? 1f - Math.max(itemSpace - nextc.minitem, 0) : 1f;
            if(isEnd(rotation)) nextMax = Math.min(nextMax, 1f - itemSpace);

            float moved = speed * edelta();

            for(int i = len - 1; i >= 0; i--){
                float nextpos = (i == len - 1 ? 100f : ys[i + 1]) - itemSpace;
                float maxmove = Mathf.clamp(nextpos - ys[i], 0, moved);

                ys[i] += maxmove;

                if(ys[i] > nextMax) ys[i] = nextMax;
                if(ys[i] > 0.5 && i > 0) mid = i - 1;
                xs[i] = Mathf.approach(xs[i], 0, moved * 2);

                if(ys[i] >= 1f && pass(ids[i])){
                    if(aligned){
                        nextc.xs[nextc.lastInserted] = xs[i];
                    }
                    items.remove(ids[i], len - i);
                    len = Math.min(i, len);
                }else if(ys[i] < minitem){
                    minitem = ys[i];
                }
            }

            if(len >= maxItems() && maxItems() < capacity ||
            minitem < itemSpace + (blendbits == 1 ? 0.3f : 0f)){
                clogHeat = Mathf.approachDelta(clogHeat, 1f, 1f / 60f);
            }else{
                clogHeat = 0f;
            }

            noSleep();
        }

        @Override
        public void handleItem(Building source, Item item){
            ensureState();
            if(rotationGrace <= 0f && len >= maxItems()) return;

            super.handleItem(source, item);
        }

        @Override
        public void handleStack(Item item, int amount, Teamc source){
            ensureState();
            int capacityLeft = rotationGrace > 0f ? capacity - len : maxItems() - len;
            super.handleStack(item, Math.max(Math.min(amount, capacityLeft), 0), source);
        }

        @Override
        public void draw(){
            ensureState();
            super.draw();

            Draw.z(Layer.block);
            Draw.rect(topRegion[tiling], x, y, 0);

            if(
            next == null ||
            next.team != team ||
            (next instanceof ConveyorBuild && next.front() == this) ||
            !validBlock(next.block)
            ){
                if(rotation > 0 && rotation < 3) Draw.yscl = -1f;
                Draw.rect(capRegion, x, y, rotdeg());
                Draw.scl();
            }
        }

        @Override
        public void drawCracks(){
            if(this.block.drawCracks && this.damaged() && this.block.size <= 7){
                int id = this.pos();
                TextureRegion region = Vars.renderer.blocks.cracks[this.block.size - 1][Mathf.clamp((int)((1.0F - this.healthf()) * 8.0F), 0, 7)];
                Draw.colorl(0.2f, 0.1f + (1f - this.healthf()) * 0.6f);
                Draw.rect(region, this.x, this.y, (float)(id % 4 * 90));
                Draw.color();
            }
        }

        @Override
        public void onProximityUpdate(){
            super.onProximityUpdate();
            noSleep();

            tiling = 0;
            for(int i = 0; i < 4; i++){
                Building otherBlock = nearby(i);
                if(
                otherBlock != null &&
                otherBlock.team == team &&
                (!(otherBlock instanceof ConveyorBuild) || otherBlock.front() == this) &&
                (i == rotation || otherBlock.block.outputsItems()) &&
                (i != rotation || otherBlock.block.hasItems) &&
                validBlock(otherBlock.block)
                ){
                    tiling |= (1 << i);
                }
            }
            tiling |= 1 << rotation;
            syncState();
        }

        @Override
        public boolean pass(Item item){
            ensureState();
            if(item != null && next != null && next.team == team && next.acceptItem(this, item) && validBlock(next.block)){
                next.handleItem(this, item);
                return true;
            }
            return false;
        }
    }
}