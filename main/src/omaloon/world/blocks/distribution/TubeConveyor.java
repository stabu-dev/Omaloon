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
//    public static final int[][] tiles = new int[][]{
//    {},
//    {0, 2}, {1, 3}, {0, 1},
//    {0, 2}, {0, 2}, {1, 2},
//    {0, 1, 2}, {1, 3}, {0, 3},
//    {1, 3}, {0, 1, 3}, {2, 3},
//    {0, 2, 3}, {1, 2, 3}, {0, 1, 2, 3}
//    };

    public @Load(value = "@-#0$", lengths = {16}) TextureRegion[] topRegion;
    public @Load(value = "@-cap") TextureRegion capRegion;

    public Block junctionReplacement, bridgeReplacement;

    public TubeConveyor(String name){
        super(name);
    }

    @Override
    public boolean blends(Tile tile, int rotation, int otherx, int othery, int otherrot, Block otherblock){
//        return (otherblock instanceof TubeDistributor) || (otherblock.outputsItems() || (lookingAt(tile, rotation, otherx, othery, otherblock) && otherblock.hasItems))
//        && lookingAtEither(tile, rotation, otherx, othery, otherrot, otherblock) && validBlock(otherblock);
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

    @Override
    public void init(){
        super.init();
//        if(junctionReplacement == null) junctionReplacement = OlDistributionBlocks.tubeJunction;
//        if(bridgeReplacement == null) bridgeReplacement = OlDistributionBlocks.tubeBridge;
    }

    public boolean validBlock(Block otherblock){
        return
        (
//        (otherblock instanceof TubeConveyor) ||
        otherblock.group == BlockGroup.transportation ||
//        (otherblock instanceof TubeDistributor) ||
//        (otherblock instanceof TubeSorter) || (otherblock instanceof TubeJunction) ||
//        (otherblock instanceof TubeGate) || otherblock instanceof TubeItemBridge ||
        (otherblock instanceof CoreBlock) || (otherblock instanceof ItemSource) || (otherblock instanceof ItemVoid));
    }

//    @Override
//    public void handlePlacementLine(Seq<BuildPlan> plans){
//        if(bridgeReplacement == null) return;
//
//        Placement.calculateBridges(plans, (TubeItemBridge)bridgeReplacement);
//    }

    @Override
    public void drawPlanRegion(BuildPlan req, Eachable<BuildPlan> list){
        super.drawPlanRegion(req, list);
        BuildPlan[] directionals = new BuildPlan[4];
        list.each(other -> {
            if(other.breaking || other == req) return;

            int i = 0;
            for(Point2 point : Geometry.d4){
                int x = req.x + point.x, y = req.y + point.y;
                if(x >= other.x - (other.block.size - 1) / 2 && x <= other.x + (other.block.size / 2) && y >= other.y - (other.block.size - 1) / 2 && y <= other.y + (other.block.size / 2)){
                    if(
                    (other.block instanceof Conveyor ?
                        (req.rotation == i || (other.rotation + 2) % 4 == i) :
                        (
                            (req.rotation == i && other.block.acceptsItems) ||
                            (req.rotation != i && other.block.outputsItems())
                        )
                    ) && validBlock(other.block)){
                        directionals[i] = other;
                    }
                }
                i++;
            }
        });

        int mask = 0;
        for(int i = 0; i < directionals.length; i++){
            if(directionals[i] != null){
                mask += (1 << i);
            }
        }
        mask |= (1 << req.rotation);
        Draw.rect(topRegion[mask], req.drawx(), req.drawy(), 0);

        if(
            directionals[req.rotation] == null ||
            (directionals[req.rotation].block instanceof Conveyor ?
                ((directionals[req.rotation].rotation + 2) % 4 == req.rotation) :
                !directionals[req.rotation].block.acceptsItems
            ) ||
            !validBlock(directionals[req.rotation].block)
        ){
            if (req.rotation > 0 && req.rotation < 3) Draw.yscl = -1f;
            Draw.rect(capRegion, req.drawx(), req.drawy(), req.rotation * 90f);
            Draw.scl();
        }

    }

    public class TubeConveyorBuild extends ConveyorBuild{
        public int tiling = 0;

        @Override
        public boolean acceptItem(Building source, Item item){
//            if(len >= capacity) return false;
//            Tile facing = Edges.getFacingEdge(source.tile, tile);
//            if(facing == null) return false;
//            int direction = Math.abs(facing.relativeTo(tile.x, tile.y) - rotation);
//            return (((direction == 0) && minitem >= itemSpace) || ((direction % 2 == 1) && minitem > 0.7f)) && !(source.block.rotate && next == source) &&
//            validBlock(source.block);
            return super.acceptItem(source, item) && validBlock(source.block);
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source){
//            if(isEnd(reverse(rotation)) && items.total() >= 2) return 0;
//            if(isEnd(reverse(rotation)) && isEnd(rotation) && items.total() >= 1) return 0;
//            return Math.min((int)(minitem / itemSpace), amount);
            if (source instanceof Building b && !validBlock(b.block)) return 0;

            return super.acceptStack(item, amount, source);
        }

//        @Override
//        public void updateTile(){
//            minitem = 1f;
//            mid = 0;
//
//            //skip updates if possible
//            if(len == 0){
//                clogHeat = 0f;
//                sleep();
//                return;
//            }
//
//            float nextMax = aligned ? 1f - Math.max(itemSpace - nextc.minitem, 0) : 1f;
//
//            if(isEnd(rotation)){
//                nextMax = Math.min(nextMax, 1f - itemSpace);
//            }
//
//            if(isEnd(reverse(rotation)) && blendbits == 0){
//                float nextMaxReverse = aligned ? (items.total() > 2 ? (0.5f - Math.max(itemSpace - nextc.minitem, 0))
//                : Math.max(itemSpace - nextc.minitem, 0)) : 0f;
//
//                float movedReverse = speed * edelta();
//
//                for(int i = 0; i < len; i++){
//                    float nextposReverse = (i == 0 ? 0f : ys[i - 1]) + itemSpace;
//                    float maxmoveReverse = Mathf.clamp(ys[i] - nextposReverse, 0, movedReverse);
//
//                    ys[i] += maxmoveReverse;
//
//                    if(ys[i] < nextMaxReverse) ys[i] = nextMaxReverse;
//                    if(ys[i] < minitem) minitem = ys[i];
//                }
//            }
//
//            float moved = speed * edelta();
//
//            for(int i = len - 1; i >= 0; i--){
//                float nextpos = (i == len - 1 ? 100f : ys[i + 1]) - itemSpace;
//                float maxmove = Mathf.clamp(nextpos - ys[i], 0, moved);
//
//                ys[i] += maxmove;
//
//                if(ys[i] > nextMax) ys[i] = nextMax;
//                if(ys[i] > 0.5 && i > 0) mid = i - 1;
//                xs[i] = Mathf.approach(xs[i], 0, moved * 2);
//
//                if(isEnd(rotation) && isEnd(reverse(rotation)) && items.total() > 1 && calls > 0){
//                    //remove last item
//                    items.remove(ids[i], len - i);
//                    len = Math.min(i, len);
//                }
//
//                if(ys[i] >= 1f && pass(ids[i])){
//                    // align X position if passing forwards
//                    if(aligned){
//                        nextc.xs[nextc.lastInserted] = xs[i];
//                    }
//                    //remove last item
//                    items.remove(ids[i], len - i);
//                    len = Math.min(i, len);
//                }else if(ys[i] < minitem){
//                    minitem = ys[i];
//                }
//            }
//
//            if(minitem < itemSpace + (blendbits == 1 ? 0.3f : 0f)
//            || isEnd(reverse(rotation)) && items.total() >= 2
//            || isEnd(reverse(rotation)) && isEnd(rotation) && items.total() >= 1){
//                clogHeat = Mathf.approachDelta(clogHeat, 1f, 1f / 60f);
//            }else{
//                clogHeat = 0f;
//            }
//
//            noSleep();
//        }

//        public void updateProximity(){
//            super.updateProximity();
//            calls++;
//        }

//        public boolean valid(int i){
//            Building b = nearby(i);
//            return b != null && validBlock(b.block);
//        }
//
//        public boolean isEnd(int i){
//            var b = nearby(i);
//            return (!valid(i) && (b == null ? null : b.block) != this.block) ||
//            (b instanceof ConveyorBuild && ((b.rotation + 2) % 4 == rotation || (b.front() != this && back() == b)));
//        }

        @Override
        public void draw(){
            super.draw();

            Draw.z(Layer.block);
            Draw.rect(topRegion[tiling], x, y, 0);

            if (
            next == null ||
            next.team != team ||
            (next instanceof ConveyorBuild && next.front() == this) ||
            !validBlock(next.block)
            ) {
                if (rotation > 0 && rotation < 3) Draw.yscl = -1f;
                Draw.rect(capRegion, x, y, rotdeg());
                Draw.scl();
            }
        }

        @Override
        public void drawCracks() {
            if (this.block.drawCracks && this.damaged() && this.block.size <= 7) {
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
            nextc = next instanceof TubeConveyorBuild d ? d : null;

            tiling = 0;
            for(int i = 0; i < 4; i++){
                Building otherBlock = nearby(i);
                if (
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
        }

        @Override
        public boolean pass(Item item){
            if(item != null && next != null && next.team == team && next.acceptItem(this, item) && validBlock(next.block)){
                next.handleItem(this, item);
                return true;
            }
            return false;
        }

//        @Override
//        public void unitOn(Unit unit){
//        }
    }
}