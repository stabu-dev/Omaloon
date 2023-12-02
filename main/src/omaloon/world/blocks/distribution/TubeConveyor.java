package omaloon.world.blocks.distribution;

import arc.func.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;

import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.Item;
import mindustry.world.*;
import mindustry.world.blocks.distribution.*;

import omaloon.content.blocks.*;
import omaloon.utils.*;

import static arc.Core.*;
import static omaloon.utils.OlUtils.reverse;

//TODO topRegion for planing
public class TubeConveyor extends Conveyor {
    private static final float itemSpace = 0.4f;
    public static final int[][] tiles = new int[][] { new int[] {},
            new int[] {0, 2}, new int[] {1, 3}, new int[] {0, 1},
            new int[] {0, 2}, new int[] {0, 2}, new int[] {1, 2},
            new int[] {0, 1, 2}, new int[] {1, 3}, new int[] {0, 3},
            new int[] {1, 3}, new int[] {0, 1, 3}, new int[] {2, 3},
            new int[] {0, 2, 3}, new int[] {1, 2, 3}, new int[] {0, 1, 2, 3}
    };

    public TextureRegion[][] topRegion;
    public TextureRegion[] capRegion;
    public Block junctionReplacement;

    public TubeConveyor(String name) {
        super(name);
    }

    @Override
    public void init() {
        super.init();

        if(junctionReplacement == null) junctionReplacement = OlDistributionBlocks.tubeJunction;
    }

    @Override
    public Block getReplacement(BuildPlan req, Seq<BuildPlan> plans) {
        if (junctionReplacement == null) return this;

        Boolf<Point2> cont = p -> plans.contains(o -> o.x == req.x + p.x && o.y == req.y + p.y && (req.block instanceof TubeConveyor || req.block instanceof Junction));
        return cont.get(Geometry.d4(req.rotation)) &&
                cont.get(Geometry.d4(req.rotation - 2)) &&
                req.tile() != null &&
                req.tile().block() instanceof TubeConveyor &&
                Mathf.mod(req.tile().build.rotation - req.rotation, 2) == 1 ? junctionReplacement : this;
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        super.drawPlanRegion(plan, list);
    }

    @Override
    public void load() {
        super.load();
        topRegion = OlUtils.splitLayers(name + "-sheet", 32, 2);
        capRegion = new TextureRegion[] {topRegion[1][0], topRegion[1][1]};
        uiIcon = atlas.find(name + "-icon");
    }

    public class TubeConveyorBuild extends ConveyorBuild {
        public int tiling = 0;

        @Override
        public void updateTile(){
            minitem = 1f;
            mid = 0;

            //skip updates if possible
            if(len == 0){
                clogHeat = 0f;
                sleep();
                return;
            }

            float nextMax = aligned ? 1f - Math.max(itemSpace - nextc.minitem, 0) : 1f;

            if(isEnd(rotation)){
                nextMax = Math.min(nextMax, 1f - itemSpace);
            }

            if(isEnd(rotation)){
                nextMax = Math.min(nextMax, 1f - itemSpace);

                if(isEnd(reverse(rotation))){
                    nextMax = Math.min(nextMax, 0.5f);
                }
            }

            float moved = speed * edelta();

            for(int i = len - 1; i >= 0; i--){
                float nextpos = (i == len - 1 ? 100f : ys[i + 1]) - itemSpace;
                float maxmove = Mathf.clamp(nextpos - ys[i], 0, moved);

                ys[i] += maxmove;

                if(ys[i] > nextMax) ys[i] = nextMax;
                if(ys[i] > 0.5 && i > 0) mid = i - 1;
                xs[i] = Mathf.approach(xs[i], 0, moved*2);

                if(ys[i] >= 1f && pass(ids[i])){
                    //align X position if passing forwards
                    if(aligned){
                        nextc.xs[nextc.lastInserted] = xs[i];
                    }
                    //remove last item
                    items.remove(ids[i], len - i);
                    len = Math.min(i, len);
                }else if(ys[i] < minitem){
                    minitem = ys[i];
                }

                if(isEnd(reverse(rotation)) && items.total() > 2) {
                    //remove last item
                    items.remove(ids[i], len - i);
                    len = Math.min(i, len);
                }
            }

            if(minitem < itemSpace + (blendbits == 1 ? 0.3f : 0f) || (isEnd(reverse(rotation)) && items.total() == 2)){
                clogHeat = Mathf.approachDelta(clogHeat, 1f, 1f / 60f);
            }else{
                clogHeat = 0f;
            }

            noSleep();
        }

        public Building buildAt(int i) {
            return nearby(i);
        }

        public boolean valid(int i) {
            Building b = buildAt(i);
            return b != null && (b instanceof TubeConveyorBuild ? (b.front() != null && b.front() == this ) : b.block.acceptsItems);
        }

        public boolean isEnd(int i) {
            var b = buildAt(i);
            return !valid(i) && (b == null ? null : b.block) != this.block;
        }

        @Override
        public void draw() {
            super.draw();
            Draw.z(Layer.blockAdditive);
            Draw.rect(topRegion[0][tiling], x, y, 0);
            int[] placementID = tiles[tiling];
            for(int i : placementID) {
                if(isEnd(i)) {
                    int id = i == 0 || i == 3 ? 1 : 0;
                    Draw.rect(capRegion[id], x, y, i == 0 || i == 2 ? 0 : -90);
                }
            }
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source) {
            if (isEnd(reverse(rotation)) && items.total() >= 2) return 0;
            return Math.min((int)(minitem / itemSpace), amount);
        }


        @Override
        public void unitOn(Unit unit) {
        }

        @Override
        public void onProximityUpdate(){
            super.onProximityUpdate();
            noSleep();
            next = front();
            nextc = next instanceof TubeConveyorBuild d ? d : null;

            tiling = 0;
            for(int i = 0; i < 4; i++){
                if(i == rotation || valid(i)) {
                    tiling |= (1 << i);
                }
            }
        }
    }
}